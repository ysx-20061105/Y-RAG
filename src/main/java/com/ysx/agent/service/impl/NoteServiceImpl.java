package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysx.agent.domain.KnowledgeBase;
import com.ysx.agent.domain.Note;
import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteListResponse;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;
import com.ysx.agent.exception.NoteAccessDeniedException;
import com.ysx.agent.exception.NoteNotFoundException;
import com.ysx.agent.mapper.KnowledgeBaseMapper;
import com.ysx.agent.mapper.NoteMapper;
import com.ysx.agent.service.NoteService;
import jakarta.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteServiceImpl implements NoteService {

    private static final int MAX_BYTES = 10_485_760;

    private static final int HINT_THRESHOLD_BYTES = 8 * 1024 * 1024;

    private final NoteMapper noteMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public NoteServiceImpl(NoteMapper noteMapper, KnowledgeBaseMapper knowledgeBaseMapper) {
        this.noteMapper = noteMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

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
        return convert(existing);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        Note existing = loadOwnedNote(noteId, userId);
        noteMapper.deleteById(existing.getId());
    }

    @Override
    public NoteResponse getNoteById(Long noteId, Long userId) {
        Note note = loadOwnedNote(noteId, userId);
        return convert(note);
    }

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

    private KnowledgeBase loadOwnedKnowledgeBase(Long kbId, Long userId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || !Objects.equals(kb.getOwner_user_id(), userId)) {
            throw new NoteAccessDeniedException("无权限访问该知识库");
        }
        return kb;
    }

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

    private void validateContentSize(String content) {
        if (content == null) {
            return;
        }
        int bytes = content.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_BYTES) {
            throw new ValidationException("内容超过10MB限制");
        }
    }

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
