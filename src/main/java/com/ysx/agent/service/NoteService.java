package com.ysx.agent.service;

import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteListResponse;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;

public interface NoteService {

    NoteResponse createNote(CreateNoteRequest request, Long userId);

    NoteResponse updateNote(Long noteId, UpdateNoteRequest request, Long userId);

    void deleteNote(Long noteId, Long userId);

    NoteResponse getNoteById(Long noteId, Long userId);

    NoteListResponse listNotes(Long kbId, Integer page, Integer size, Long userId);
}
