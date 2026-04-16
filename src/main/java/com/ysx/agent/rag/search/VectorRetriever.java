package com.ysx.agent.rag.search;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 向量检索器接口
 * 用于从向量数据库中检索相关文档
 */
public interface VectorRetriever {

    /**
     * 执行向量检索
     *
     * @param query               查询文本
     * @param collectionName      集合名称
     * @param topK                返回数量
     * @param similarityThreshold 相似度阈值
     * @return 向量检索结果列表
     */
    List<Document> retrieve(String query, String collectionName, int topK, double similarityThreshold);
}
