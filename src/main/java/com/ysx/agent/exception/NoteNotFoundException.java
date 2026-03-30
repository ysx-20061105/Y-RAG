package com.ysx.agent.exception;

public class NoteNotFoundException extends RuntimeException {

    private final Long noteId;

    public NoteNotFoundException(Long noteId) {
        super("笔记不存在: " + noteId);
        this.noteId = noteId;
    }

    public Long getNoteId() {
        return noteId;
    }
}
