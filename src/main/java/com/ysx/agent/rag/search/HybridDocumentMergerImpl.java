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
 * <p>
 * 使用 RRF (Reciprocal Rank Fusion) 倒数排名融合算法，将多个检索通道的结果进行融合。
 * RRF 算法是一种无参数、可解释的融合方法，不依赖检索得分的具体值，只依赖排名顺序。
 * <p>
 * 算法原理：
 * 对于每个检索通道中的文档，根据其排名计算 RRF 分数：score = 1 / (k + rank)
 * 其中 k 是平滑常数（通常取 60），rank 是文档在该通道中的排名（从 1 开始）。
 * 最终分数 = Σ(weight_i * rrf_score_i)，即各通道加权 RRF 分数之和。
 *
 * @author ysx
 * @see <a href="https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf">RRF Paper</a>
 */
@Component
public class HybridDocumentMergerImpl implements HybridDocumentMerger {

    private static final Logger log = LoggerFactory.getLogger(HybridDocumentMergerImpl.class);

    /**
     * RRF 算法中的常数 k，用于平滑排名
     * <p>
     * 值越大，不同排名之间的分数差异越小。
     * 经验值通常在 60 左右，适用于中等规模的检索结果集。
     */
    private static final int RRF_K = 60;

    /**
     * 融合多个检索通道的结果
     * <p>
     * 完整流程：
     * <ol>
     *   <li>边界检查：若任一通道为空，直接返回另一通道结果</li>
     *   <li>分数计算：基于 RRF 算法计算每个文档的融合分数</li>
     *   <li>排序去重：按融合分数降序排序，去除重复文档</li>
     *   <li>截取 TopK：根据配置截取前 topK 个结果</li>
     * </ol>
     *
     * @param vectorResults  向量检索结果列表
     * @param keywordResults 关键词检索结果列表
     * @param config         混合检索配置，包含权重和 topK 参数
     * @return 融合排序后的文档列表
     */
    @Override
    public List<Document> merge(List<Document> vectorResults,
                               List<Document> keywordResults,
                               HybridSearchConfig config) {
        log.debug("Merging documents: vectorCount={}, keywordCount={}, config={}",
                vectorResults.size(), keywordResults.size(), config);

        // 边界情况：两个通道都为空
        if (vectorResults.isEmpty() && keywordResults.isEmpty()) {
            return Collections.emptyList();
        }

        // 边界情况：只有一个通道有结果
        if (vectorResults.isEmpty()) {
            return keywordResults;
        }
        if (keywordResults.isEmpty()) {
            return vectorResults;
        }

        // 获取配置中的权重
        double vectorWeight = config.getVectorWeight();
        double keywordWeight = config.getKeywordWeight();

        // Step 1: 计算每个文档的 RRF 融合分数
        // 使用 HashMap 存储 docId -> fusedScore 的映射
        Map<String, Double> fusedScores = new HashMap<>();

        // 处理向量检索结果：按排名计算加权 RRF 分数
        for (int i = 0; i < vectorResults.size(); i++) {
            String docId = vectorResults.get(i).getId();
            // RRF 分数 = 权重 * (1 / (k + rank))，rank = i + 1（从 1 开始）
            double rrfScore = vectorWeight * (1.0 / (RRF_K + i + 1));
            // 合并分数：若文档已在另一通道出现，则累加分数
            fusedScores.merge(docId, rrfScore, Double::sum);
        }

        // 处理关键词检索结果：同上
        for (int i = 0; i < keywordResults.size(); i++) {
            String docId = keywordResults.get(i).getId();
            double rrfScore = keywordWeight * (1.0 / (RRF_K + i + 1));
            fusedScores.merge(docId, rrfScore, Double::sum);
        }

        // Step 2: 按融合分数降序排序，获取排序后的文档 ID 列表
        List<String> sortedDocIds = fusedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Step 3: 构建文档映射，用于去重和按序获取
        Map<String, Document> allDocuments = new HashMap<>();
        vectorResults.forEach(doc -> allDocuments.put(doc.getId(), doc));
        keywordResults.forEach(doc -> allDocuments.put(doc.getId(), doc));

        // Step 4: 组装最终结果，同时保存融合分数到文档元数据
        List<Document> merged = new ArrayList<>();
        for (String docId : sortedDocIds) {
            // 将融合分数存入文档元数据，便于后续分析和调试
            allDocuments.get(docId).getMetadata().put("fusedScore", fusedScores.get(docId));
            merged.add(allDocuments.get(docId));
        }

        // Step 5: 截取 TopK
        int topK = config.getTopK();
        if (merged.size() > topK) {
            merged = merged.subList(0, topK);
        }

        log.debug("Merged result count: {}", merged.size());
        return merged;
    }
}
