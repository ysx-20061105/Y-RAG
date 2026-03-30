package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体，对应表 note
 */
@TableName(value = "note")
public class Note implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("kb_id")
    private Long kbId;

    private String title;

    private String content;

    @TableField("content_bytes")
    private Integer contentBytes;

    private String summary;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private static final long serialVersionUID = 1L;

    public Note() {
    }

    public Note(Long id, Long kbId, String title, String content, Integer contentBytes, String summary,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.kbId = kbId;
        this.title = title;
        this.content = content;
        this.contentBytes = contentBytes;
        this.summary = summary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Integer getContentBytes() {
        return contentBytes;
    }

    public void setContentBytes(Integer contentBytes) {
        this.contentBytes = contentBytes;
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
