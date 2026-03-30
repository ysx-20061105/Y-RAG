package com.ysx.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateNoteRequest {

    @NotNull
    private Long kbId;

    private String title;

    @NotNull
    @Size(max = 10_485_760)
    private String content;

    public CreateNoteRequest() {
    }

    public CreateNoteRequest(Long kbId, String title, String content) {
        this.kbId = kbId;
        this.title = title;
        this.content = content;
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
}
