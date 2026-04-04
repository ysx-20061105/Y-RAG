package com.ysx.agent.rag.etl;

import com.ysx.agent.domain.Note;
import com.ysx.agent.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 文档加载器
 * 读取markdown文件转化为Document对象
 */
@Component
public class YRAGDocumentLoader {
    private final Logger logger = Logger.getLogger(YRAGDocumentLoader.class.getName());
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * markdown文档加载
     * @param kb_id 知识库id
     * @return
     */
    public List<Document> loadMarkdowns(Long kb_id) {
        ArrayList<Document> allDocuments = new ArrayList<>();
        // 获取所有markdown文件
        List<Note> notes = knowledgeBaseService.getNotsByKnowledgeId(kb_id);
        for (Note note : notes) {
            if (note.getContent() == null || note.getContent().trim().isEmpty()) {
                continue;
            }
            byte[] contentBytes = note.getContent().getBytes(StandardCharsets.UTF_8);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(contentBytes));
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true) // 开启分割
                    .withIncludeCodeBlock(false)            // 排除代码块
                    .withIncludeBlockquote(false)           // 排除引用
                    // 添加元数据，方便以后知道这段话出自哪篇笔记
                    .withAdditionalMetadata("note_id", note.getId() != null ? note.getId().toString() : "unknown")
                    .withAdditionalMetadata("note_title", note.getTitle() != null ? note.getTitle() : "无标题")
                    .build();
            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
            allDocuments.addAll(reader.get());
        }
        return allDocuments;
    }
}