package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName rag_chunk
 */
@TableName(value ="rag_chunk")
public class RagChunk implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long rag_document_id;

    private Integer chunk_no;

    private String chunk_text;

    private Integer token_count;

    private Integer start_offset;

    private Integer end_offset;

    private Object metadata_json;

    private Date created_at;

    private static final long serialVersionUID = 1L;

    public RagChunk() {
    }

    public RagChunk(Long id, Long rag_document_id, Integer chunk_no, String chunk_text, Integer token_count,
                    Integer start_offset, Integer end_offset, Object metadata_json, Date created_at) {
        this.id = id;
        this.rag_document_id = rag_document_id;
        this.chunk_no = chunk_no;
        this.chunk_text = chunk_text;
        this.token_count = token_count;
        this.start_offset = start_offset;
        this.end_offset = end_offset;
        this.metadata_json = metadata_json;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRag_document_id() {
        return rag_document_id;
    }

    public void setRag_document_id(Long rag_document_id) {
        this.rag_document_id = rag_document_id;
    }

    public Integer getChunk_no() {
        return chunk_no;
    }

    public void setChunk_no(Integer chunk_no) {
        this.chunk_no = chunk_no;
    }

    public String getChunk_text() {
        return chunk_text;
    }

    public void setChunk_text(String chunk_text) {
        this.chunk_text = chunk_text;
    }

    public Integer getToken_count() {
        return token_count;
    }

    public void setToken_count(Integer token_count) {
        this.token_count = token_count;
    }

    public Integer getStart_offset() {
        return start_offset;
    }

    public void setStart_offset(Integer start_offset) {
        this.start_offset = start_offset;
    }

    public Integer getEnd_offset() {
        return end_offset;
    }

    public void setEnd_offset(Integer end_offset) {
        this.end_offset = end_offset;
    }

    public Object getMetadata_json() {
        return metadata_json;
    }

    public void setMetadata_json(Object metadata_json) {
        this.metadata_json = metadata_json;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
