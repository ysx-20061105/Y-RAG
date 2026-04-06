package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysx.agent.domain.KnowledgeBase;
import com.ysx.agent.domain.Note;
import com.ysx.agent.domain.NoteVersion;
import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteListResponse;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;
import com.ysx.agent.exception.NoteAccessDeniedException;
import com.ysx.agent.exception.NoteNotFoundException;
import com.ysx.agent.mapper.KnowledgeBaseMapper;
import com.ysx.agent.mapper.NoteMapper;
import com.ysx.agent.mapper.NoteVersionMapper;
import com.ysx.agent.rag.etl.QdrantProperties;
import com.ysx.agent.rag.etl.YRAGDocumentEnricher;
import com.ysx.agent.rag.etl.YRAGDocumentLoader;
import com.ysx.agent.rag.etl.YRAGMarkdownSplitter;
import com.ysx.agent.service.NoteService;
import com.ysx.agent.utils.QdrantUtils;
import io.qdrant.client.QdrantClient;
import jakarta.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 笔记服务实现类
 * 处理笔记的 CRUD 操作，包含内容大小校验、标题自动解析等逻辑
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    /**
     * 最大笔记内容大小：10MB
     */
    private static final int MAX_BYTES = 10_485_760;
    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteMapper noteMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final QdrantProperties qdrantProperties;

    private final QdrantClient qdrantClient;

    private final EmbeddingModel ollamaEmbeddingModel;

    private final YRAGDocumentLoader yragDocumentLoader;

    private final YRAGDocumentEnricher documentEnricher;

    private final Executor noteTaskExecutor;

    public NoteServiceImpl(NoteMapper noteMapper,
                           KnowledgeBaseMapper knowledgeBaseMapper,
                           QdrantClient qdrantClient,
                           EmbeddingModel ollamaEmbeddingModel,
                           QdrantProperties qdrantProperties,
                           YRAGDocumentLoader yragDocumentLoader,
                           YRAGDocumentEnricher documentEnricher,
                           @Qualifier("noteTaskExecutor") Executor noteTaskExecutor) {
        this.noteMapper = noteMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.qdrantProperties = qdrantProperties;
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
        this.yragDocumentLoader = yragDocumentLoader;
        this.documentEnricher = documentEnricher;
        this.noteTaskExecutor = noteTaskExecutor;
    }

    /**
     * 创建笔记
     * 验证知识库归属权、内容大小限制，并自动从内容中提取标题
     */
    @Override
    @Transactional
    public NoteResponse createNote(CreateNoteRequest request, Long userId) {
        KnowledgeBase kb = loadOwnedKnowledgeBase(request.getKbId(), userId);
        validateContentSize(request.getContent());
        Note note = new Note();
        note.setKbId(kb.getId());
        String title = resolveTitle(request.getTitle(), request.getContent());
        note.setTitle(title);
        note.setContent(request.getContent());
        int bytes = request.getContent().getBytes(StandardCharsets.UTF_8).length;
        note.setContentBytes(bytes);
        noteMapper.insert(note);

        return convert(note);
    }

    /**
     * 添加笔记 ETL
     *
     * @param note 笔记
     */
    private void addQdrantVector(Note note) {
        String name = qdrantProperties.getCollectionNamePrefix() + note.getKbId();
        // ETL
        // 加载文档
        List<Document> documents = yragDocumentLoader.loadMarkdownsByNote(note);
        // 转换 分割文本并添加摘要
//        TokenTextSplitter splitter = new TokenTextSplitter();
//        documents = splitter.apply(documents);
        YRAGMarkdownSplitter yragMarkdownSplitter = new YRAGMarkdownSplitter();
        documents = yragMarkdownSplitter.splitDocuments(documents);
        //关键词元数据增强器
        documents = documentEnricher.enrichDocumentsByKeyword(documents);
        documents = documentEnricher.enrichDocumentsBySummary(documents);
        try {
            boolean collectionInQdrant = QdrantUtils.isCollectionInQdrant(qdrantClient, name);
            if (!collectionInQdrant) {
                QdrantUtils.getCollectionName(qdrantClient, name, ollamaEmbeddingModel.dimensions());
            }
            VectorStore vectorStore = QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                    .collectionName(name)
                    .initializeSchema(qdrantProperties.isInitializeSchema())
                    .batchingStrategy(new TokenCountBatchingStrategy())
                    .build();
            vectorStore.add(documents);
        } catch (Exception e) {
            log.error("addQdrantVector error", e);
        }
    }

    @Override
    @Transactional
    public NoteResponse updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
        Note existing = loadOwnedNote(noteId, userId);
        String newContent = request.getContent() != null ? request.getContent() : existing.getContent();
        validateContentSize(newContent);
        existing.setContent(newContent);
        String newTitle = request.getTitle() != null
                ? request.getTitle()
                : resolveTitle(existing.getTitle(), newContent);
        existing.setTitle(newTitle);
        int bytes = newContent.getBytes(StandardCharsets.UTF_8).length;
        existing.setContentBytes(bytes);
        noteMapper.updateById(existing);
        // 异步处理任务 更新向量数据库
        noteTaskExecutor.execute(() -> this.updateQdrantVector(noteId));
        return convert(existing);
    }

    /**
     * 更新笔记向量
     * @param noteId
     */
    private void updateQdrantVector(Long noteId) {
        // 先删除Qdrant中note_id为noteId的数据
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getKbId() == null) {
            log.error("note is null");
            return;
        }
        String name = qdrantProperties.getCollectionNamePrefix() + note.getKbId();
        try {
            boolean collectionInQdrant = QdrantUtils.isCollectionInQdrant(qdrantClient, name);
            if (!collectionInQdrant) {
                QdrantUtils.getCollectionName(qdrantClient, name, ollamaEmbeddingModel.dimensions());
            }else{
                // 删除原来的
                QdrantUtils.deleteByNoteId(qdrantClient, name, noteId);
            }
            // 添加新的
            this.addQdrantVector(note);
            log.info("updateQdrantVector success");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除笔记
     */
    @Override
    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        Note existing = loadOwnedNote(noteId, userId);
        noteMapper.deleteById(existing.getId());
        // 异步处理任务 删除向量数据库
        noteTaskExecutor.execute(() -> this.deleteQdrantVector(existing));
    }

    private void deleteQdrantVector(Note note){
        try {
            String name = qdrantProperties.getCollectionNamePrefix() + note.getKbId();
            QdrantUtils.deleteByNoteId(qdrantClient, name, note.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取笔记详情
     */
    @Override
    public NoteResponse getNoteById(Long noteId, Long userId) {
        Note note = loadOwnedNote(noteId, userId);
        return convert(note);
    }

    /**
     * 分页查询笔记列表
     * 必须指定知识库ID，验证用户对该知识库的所有权
     */
    @Override
    public NoteListResponse listNotes(Long kbId, Integer page, Integer size, Long userId) {
        loadOwnedKnowledgeBase(kbId, userId);
        int pageNum = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : Math.min(size, 100);
        Page<Note> mpPage = new Page<>(pageNum, pageSize);
        Page<Note> resultPage = noteMapper.selectByKbId(kbId, mpPage);
        List<NoteResponse> list = resultPage.getRecords().stream()
                .map(this::convert)
                .collect(Collectors.toList());
        NoteListResponse resp = new NoteListResponse();
        resp.setList(list);
        resp.setTotal(resultPage.getTotal());
        resp.setPage((int) resultPage.getCurrent());
        resp.setSize((int) resultPage.getSize());
        return resp;
    }

    /**
     * 加载用户拥有的知识库，若不存在或无权限则抛出异常
     */
    private KnowledgeBase loadOwnedKnowledgeBase(Long kbId, Long userId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || !Objects.equals(kb.getUserId(), userId)) {
            throw new NoteAccessDeniedException("无权限访问该知识库");
        }
        return kb;
    }

    /**
     * 加载用户拥有的笔记，包含知识库归属权验证
     */
    private Note loadOwnedNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new NoteNotFoundException(noteId);
        }
        KnowledgeBase kb = loadOwnedKnowledgeBase(note.getKbId(), userId);
        if (kb == null) {
            throw new NoteAccessDeniedException("无权限访问该笔记");
        }
        return note;
    }

    /**
     * 校验笔记内容大小是否超过10MB限制
     */
    private void validateContentSize(String content) {
        if (content == null) {
            return;
        }
        int bytes = content.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_BYTES) {
            throw new ValidationException("内容超过10MB限制");
        }
    }

    /**
     * 解析笔记标题
     * 优先级：指定标题 > 内容第一行H1标题（# 开头）> "无标题笔记"
     */
    private String resolveTitle(String title, String content) {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        if (content == null || content.isEmpty()) {
            return "无标题笔记";
        }
        String[] lines = content.split("\n", -1);
        if (lines.length == 0) {
            return "无标题笔记";
        }
        String firstLine = lines[0].trim();
        if (firstLine.startsWith("# ")) {
            String h1 = firstLine.substring(2).trim();
            return h1.isEmpty() ? "无标题笔记" : h1;
        }
        return "无标题笔记";
    }

    /**
     * 将 Note 实体转换为 NoteResponse DTO
     */
    private NoteResponse convert(Note note) {
        NoteResponse resp = new NoteResponse();
        resp.setId(note.getId());
        resp.setKbId(note.getKbId());
        resp.setTitle(note.getTitle());
        resp.setContent(note.getContent());
        resp.setSummary(note.getSummary());
        resp.setCreatedAt(note.getCreatedAt());
        resp.setUpdatedAt(note.getUpdatedAt());
        return resp;
    }
}
