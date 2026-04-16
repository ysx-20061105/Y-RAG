package com.ysx.agent.rag.search;

import com.ysx.agent.rag.config.HybridSearchConfig;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 混合文档融合器接口
 * 用于将多个检索通道的结果进行加权融合
 */
public interface HybridDocumentMerger {

    /**
     * 融合多个检索通道的结果
     * 使用RRF (Reciprocal Rank Fusion) 算法
     *
     * @param vectorResults  向量检索结果
     * @param keywordResults 关键词检索结果
     * @param config         融合配置
     * @return 融合后的结果列表
     */
    List<Document> merge(List<Document> vectorResults,
                         List<Document> keywordResults,
                         HybridSearchConfig config);
}
