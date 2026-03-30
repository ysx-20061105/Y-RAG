package com.ysx.agent.dto;

import cn.hutool.core.bean.BeanUtil;
import com.ysx.agent.domain.Note;
import java.time.LocalDateTime;

public class NoteResponse {

    private Long id;

    private Long kbId;

    private String title;

    private String content;

    private String summary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public NoteResponse() {
    }

    public NoteResponse(Long id, Long kbId, String title, String content, String summary, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.kbId = kbId;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoteResponse fromEntity(Note note) {
        if (note == null) {
            return null;
        }
        return BeanUtil.copyProperties(note, NoteResponse.class);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKbId() {
        return kbId;
    }

    public void setKbId(Long kbId) {
        this.kbId = kbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
