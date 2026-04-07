package com.ysx.agent.rag.search;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

/**
 * RAG 检索增强顾问的工厂
 */
public class YRAGRagAdvisorFactory {

    /**
     * RAG 检索增强顾问
     *
     * @param documentRetriever  文档检索器，用于从知识库中检索相关文档
     * @return 自定义的 RAG 检索增强顾问
     */
    public static Advisor createYRAGRagAdvisor(DocumentRetriever documentRetriever) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(YRAGContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}