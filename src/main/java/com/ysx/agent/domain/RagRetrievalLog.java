package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName rag_retrieval_log
 */
@TableName(value ="rag_retrieval_log")
public class RagRetrievalLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long kb_id;

    private Long actor_user_id;

    private String query_text;

    private String rewritten_query;

    private Integer top_k;

    private Object retrieved_chunk_ids;

    private Integer latency_ms;

    private Date created_at;

    private static final long serialVersionUID = 1L;

    public RagRetrievalLog() {
    }

    public RagRetrievalLog(Long id, Long kb_id, Long actor_user_id, String query_text, String rewritten_query,
                           Integer top_k, Object retrieved_chunk_ids, Integer latency_ms, Date created_at) {
        this.id = id;
        this.kb_id = kb_id;
        this.actor_user_id = actor_user_id;
        this.query_text = query_text;
        this.rewritten_query = rewritten_query;
        this.top_k = top_k;
        this.retrieved_chunk_ids = retrieved_chunk_ids;
        this.latency_ms = latency_ms;
        this.created_at = created_at;
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

    public Long getActor_user_id() {
        return actor_user_id;
    }

    public void setActor_user_id(Long actor_user_id) {
        this.actor_user_id = actor_user_id;
    }

    public String getQuery_text() {
        return query_text;
    }

    public void setQuery_text(String query_text) {
        this.query_text = query_text;
    }

    public String getRewritten_query() {
        return rewritten_query;
    }

    public void setRewritten_query(String rewritten_query) {
        this.rewritten_query = rewritten_query;
    }

    public Integer getTop_k() {
        return top_k;
    }

    public void setTop_k(Integer top_k) {
        this.top_k = top_k;
    }

    public Object getRetrieved_chunk_ids() {
        return retrieved_chunk_ids;
    }

    public void setRetrieved_chunk_ids(Object retrieved_chunk_ids) {
        this.retrieved_chunk_ids = retrieved_chunk_ids;
    }

    public Integer getLatency_ms() {
        return latency_ms;
    }

    public void setLatency_ms(Integer latency_ms) {
        this.latency_ms = latency_ms;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
