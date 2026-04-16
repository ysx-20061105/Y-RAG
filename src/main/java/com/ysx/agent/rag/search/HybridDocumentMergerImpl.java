package com.ysx.agent.rag.search;

import com.ysx.agent.rag.config.HybridSearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合文档融合器实现类
 * 使用RRF (Reciprocal Rank Fusion) 算法融合多个检索通道的结果
 */
@Component
public class HybridDocumentMergerImpl implements HybridDocumentMerger {

    private static final Logger log = LoggerFactory.getLogger(HybridDocumentMergerImpl.class);

    /**
     * RRF算法中的常数k，用于平滑排名
     */
    private static final int RRF_K = 60;

    @Override
    public List<Document> merge(List<Document> vectorResults,
                               List<Document> keywordResults,
                               HybridSearchConfig config) {
        log.debug("Merging documents: vectorCount={}, keywordCount={}, config={}",
                vectorResults.size(), keywordResults.size(), config);

        if (vectorResults.isEmpty() && keywordResults.isEmpty()) {
            return Collections.emptyList();
        }

        if (vectorResults.isEmpty()) {
            return keywordResults;
        }

        if (keywordResults.isEmpty()) {
            return vectorResults;
        }

        double vectorWeight = config.getVectorWeight();
        double keywordWeight = config.getKeywordWeight();

        // 计算每个文档的RRF融合分数
        Map<String, Double> fusedScores = new HashMap<>();

        // 添加向量检索结果
        for (int i = 0; i < vectorResults.size(); i++) {
            String docId = vectorResults.get(i).getId();
            double rrfScore = vectorWeight * (1.0 / (RRF_K + i + 1));
            fusedScores.merge(docId, rrfScore, Double::sum);
        }

        // 添加关键词检索结果
        for (int i = 0; i < keywordResults.size(); i++) {
            String docId = keywordResults.get(i).getId();
            double rrfScore = keywordWeight * (1.0 / (RRF_K + i + 1));
            fusedScores.merge(docId, rrfScore, Double::sum);
        }

        // 按分数排序并去重
        List<String> sortedDocIds = fusedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 构建最终结果，按原始顺序保持
        Map<String, Document> allDocuments = new HashMap<>();
        vectorResults.forEach(doc -> allDocuments.put(doc.getId(), doc));
        keywordResults.forEach(doc -> allDocuments.put(doc.getId(), doc));

        List<Document> merged = new ArrayList<>();
        for (String docId : sortedDocIds) {
            allDocuments.get(docId).getMetadata().put("fusedScore", fusedScores.get(docId));
            merged.add(allDocuments.get(docId));
        }

        // 限制返回数量
        int topK = config.getTopK();
        if (merged.size() > topK) {
            merged = merged.subList(0, topK);
        }

        log.debug("Merged result count: {}", merged.size());
        return merged;
    }
}
