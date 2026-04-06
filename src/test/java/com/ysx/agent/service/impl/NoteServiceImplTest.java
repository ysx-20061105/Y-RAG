package com.ysx.agent.service.impl;

import com.ysx.agent.domain.Note;
import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;
import com.ysx.agent.rag.etl.YRAGDocumentLoader;
import com.ysx.agent.rag.etl.YRAGMarkdownSplitter;
import com.ysx.agent.service.NoteService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoteServiceImplTest {
    @Resource
    private NoteService noteService;
    @Resource
    private YRAGDocumentLoader yragDocumentLoader;

    @Test
    void create() {
        Note note1 = noteService.getById(10);
        CreateNoteRequest note = new CreateNoteRequest();
        note.setKbId(1L);
        note.setTitle("java");
        note.setContent(note1.getContent());
        NoteResponse note2 = noteService.createNote(note, 1L);
        assertNotNull(note2);

    }

    @Test
    void update() {
        Note note1 = noteService.getById(1);
//        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest();
//        updateNoteRequest.setTitle("包装类型与基本类型");
//        updateNoteRequest.setContent(note1.getContent());
//        NoteResponse note2 = noteService.updateNote(1L,updateNoteRequest, 1L);
        List<Document> documents = yragDocumentLoader.loadMarkdownsByNote(note1);
        YRAGMarkdownSplitter yragMarkdownSplitter = new YRAGMarkdownSplitter();

        documents = yragMarkdownSplitter.splitDocuments(documents);
        assertNotNull(documents);
    }
}