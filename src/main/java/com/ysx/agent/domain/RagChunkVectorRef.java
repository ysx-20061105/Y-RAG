package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName rag_chunk_vector_ref
 */
@TableName(value ="rag_chunk_vector_ref")
public class RagChunkVectorRef implements Serializable {
    private Long id;

    private Long chunk_id;

    private String embedding_model;

    private Integer embedding_dim;

    private String vector_store;

    private String vector_id;

    private String index_status;

    private Date created_at;

    private Date updated_at;

    private static final long serialVersionUID = 1L;

    public RagChunkVectorRef() {
    }

    public RagChunkVectorRef(Long id, Long chunk_id, String embedding_model, Integer embedding_dim, String vector_store,
                             String vector_id, String index_status, Date created_at, Date updated_at) {
        this.id = id;
        this.chunk_id = chunk_id;
        this.embedding_model = embedding_model;
        this.embedding_dim = embedding_dim;
        this.vector_store = vector_store;
        this.vector_id = vector_id;
        this.index_status = index_status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChunk_id() {
        return chunk_id;
    }

    public void setChunk_id(Long chunk_id) {
        this.chunk_id = chunk_id;
    }

    public String getEmbedding_model() {
        return embedding_model;
    }

    public void setEmbedding_model(String embedding_model) {
        this.embedding_model = embedding_model;
    }

    public Integer getEmbedding_dim() {
        return embedding_dim;
    }

    public void setEmbedding_dim(Integer embedding_dim) {
        this.embedding_dim = embedding_dim;
    }

    public String getVector_store() {
        return vector_store;
    }

    public void setVector_store(String vector_store) {
        this.vector_store = vector_store;
    }

    public String getVector_id() {
        return vector_id;
    }

    public void setVector_id(String vector_id) {
        this.vector_id = vector_id;
    }

    public String getIndex_status() {
        return index_status;
    }

    public void setIndex_status(String index_status) {
        this.index_status = index_status;
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
