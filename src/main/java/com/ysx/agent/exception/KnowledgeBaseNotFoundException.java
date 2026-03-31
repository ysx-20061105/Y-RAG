package com.ysx.agent.exception;

public class KnowledgeBaseNotFoundException extends RuntimeException {

    private final Long knowledgeBaseId;

    public KnowledgeBaseNotFoundException(Long knowledgeBaseId) {
        super("知识库不存在");
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }
}
