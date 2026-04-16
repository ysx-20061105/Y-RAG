package com.ysx.agent.rag.search;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 关键词检索器接口
 * 用于从支持全文索引的存储中检索相关文档
 */
public interface KeywordRetriever {

    /**
     * 执行关键词检索
     *
     * @param query          查询文本
     * @param collectionName 集合名称
     * @param topK           返回数量
     * @param tokenizer      分词器类型
     * @return 关键词检索结果列表
     */
    List<Document> retrieve(String query, String collectionName, int topK, TokenizerType tokenizer);
}
