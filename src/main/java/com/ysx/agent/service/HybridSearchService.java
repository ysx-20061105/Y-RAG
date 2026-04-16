package com.ysx.agent.service;

import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.rag.search.RetrievalResult;

/**
 * 混合检索服务接口
 * 同时支持向量检索和关键词检索两种检索方式
 */
public interface HybridSearchService {

    /**
     * 执行混合检索（使用默认配置）
     *
     * @param query          查询文本
     * @param collectionName 集合名称
     * @return 检索结果
     */
    default RetrievalResult search(String query, String collectionName) {
        return search(query, collectionName, HybridSearchConfig.balanced());
    }

    /**
     * 执行混合检索
     *
     * @param query          查询文本
     * @param collectionName 集合名称
     * @param config         检索配置（可为null，使用默认配置）
     * @return 检索结果
     */
    RetrievalResult search(String query, String collectionName, HybridSearchConfig config);
}
