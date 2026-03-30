package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName rag_document
 */
@TableName(value ="rag_document")
public class RagDocument implements Serializable {
    private Long id;

    private Long kb_id;

    private Long note_id;

    private Long source_version_id;

    private String source_type;

    private String parse_status;

    private Integer chunk_count;

    private Integer token_count;

    private String content_hash;

    private Date created_at;

    private Date updated_at;

    private static final long serialVersionUID = 1L;

    public RagDocument() {
    }

    public RagDocument(Long id, Long kb_id, Long note_id, Long source_version_id, String source_type,
                       String parse_status, Integer chunk_count, Integer token_count, String content_hash,
                       Date created_at, Date updated_at) {
        this.id = id;
        this.kb_id = kb_id;
        this.note_id = note_id;
        this.source_version_id = source_version_id;
        this.source_type = source_type;
        this.parse_status = parse_status;
        this.chunk_count = chunk_count;
        this.token_count = token_count;
        this.content_hash = content_hash;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKb_id() {
        return kb_id;
    }

    public void setKb_id(Long kb_id) {
        this.kb_id = kb_id;
    }

    public Long getNote_id() {
        return note_id;
    }

    public void setNote_id(Long note_id) {
        this.note_id = note_id;
    }

    public Long getSource_version_id() {
        return source_version_id;
    }

    public void setSource_version_id(Long source_version_id) {
        this.source_version_id = source_version_id;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public String getParse_status() {
        return parse_status;
    }

    public void setParse_status(String parse_status) {
        this.parse_status = parse_status;
    }

    public Integer getChunk_count() {
        return chunk_count;
    }

    public void setChunk_count(Integer chunk_count) {
        this.chunk_count = chunk_count;
    }

    public Integer getToken_count() {
        return token_count;
    }

    public void setToken_count(Integer token_count) {
        this.token_count = token_count;
    }

    public String getContent_hash() {
        return content_hash;
    }

    public void setContent_hash(String content_hash) {
        this.content_hash = content_hash;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
