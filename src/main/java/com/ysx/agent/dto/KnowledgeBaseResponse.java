package com.ysx.agent.dto;

import com.ysx.agent.domain.KnowledgeBase;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class KnowledgeBaseResponse {

    private Long id;

    private Long ownerUserId;

    private String name;

    private String description;

    private String category;

    private List<String> tags;

    private Integer status;

    private Date createdAt;

    private Date updatedAt;

    public KnowledgeBaseResponse() {
    }

    public static KnowledgeBaseResponse fromEntity(KnowledgeBase kb, List<String> tags) {
        KnowledgeBaseResponse response = new KnowledgeBaseResponse();
        response.setId(kb.getId());
        response.setOwnerUserId(kb.getUserId());
        response.setName(kb.getName());
        response.setDescription(kb.getDescription());
        response.setCategory(kb.getCategory());
        response.setTags(tags == null ? Collections.emptyList() : tags);
        response.setStatus(kb.getStatus());
        response.setCreatedAt(kb.getCreated_at());
        response.setUpdatedAt(kb.getUpdated_at());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
