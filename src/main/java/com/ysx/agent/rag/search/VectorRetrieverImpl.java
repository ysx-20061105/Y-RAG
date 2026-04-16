package com.ysx.agent.rag.search;

import io.qdrant.client.QdrantClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量检索器实现类
 * <p>
 * 基于 Qdrant 向量数据库的语义检索实现。
 * 通过将查询文本向量化，然后在向量空间中进行相似度搜索，
 * 返回与查询语义最相关的文档。
 * <p>
 * 工作流程：
 * <ol>
 *   <li>接收用户查询文本</li>
 *   <li>使用 EmbeddingModel 将文本转换为向量</li>
 *   <li>在 Qdrant 中执行相似度检索</li>
 *   <li>返回相似度超过阈值的 TopK 个文档</li>
 * </ol>
 *
 * @author ysx
 * @see VectorRetriever
 * @see QdrantVectorStore
 */
@Component
public class VectorRetrieverImpl implements VectorRetriever {

    private static final Logger log = LoggerFactory.getLogger(VectorRetrieverImpl.class);

    /**
     * Qdrant 向量数据库客户端
     */
    private final QdrantClient qdrantClient;

    /**
     * Ollama 嵌入模型，用于将文本转换为向量
     */
    private final EmbeddingModel ollamaEmbeddingModel;

    /**
     * 构造函数
     *
     * @param qdrantClient         Qdrant 客户端
     * @param ollamaEmbeddingModel 嵌入模型
     */
    public VectorRetrieverImpl(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel) {
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
    }

    /**
     * 执行向量语义检索
     * <p>
     * 将查询文本转换为向量，在 Qdrant 集合中搜索相似文档。
     *
     * @param query              查询文本
     * @param collectionName     Qdrant 集合名称
     * @param topK               返回的最大文档数量
     * @param similarityThreshold 相似度阈值，低于此值的文档将被过滤
     * @return 检索到的文档列表，按相似度降序排列
     * @throws RuntimeException 检索失败时抛出
     */
    @Override
    public List<Document> retrieve(String query, String collectionName, int topK, double similarityThreshold) {
        log.debug("Executing vector retrieval: query={}, collection={}, topK={}, threshold={}",
                query, collectionName, topK, similarityThreshold);

        try {
            // 创建 VectorStore 实例
            VectorStore vectorStore = createVectorStore(collectionName);

            // 构建文档检索器，配置 topK 和相似度阈值
            VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();

            // 执行检索
            return retriever.retrieve(new Query(query));
        } catch (Exception e) {
            log.error("Vector retrieval failed: {}", e.getMessage(), e);
            throw new RuntimeException("Vector retrieval failed: " + e.getMessage(), e);
        }
    }

    /**
     * 创建 Qdrant 向量存储实例
     *
     * @param collectionName 集合名称
     * @return VectorStore 实例
     */
    private VectorStore createVectorStore(String collectionName) {
        return QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(collectionName)
                // 不初始化 schema，假设集合已存在
                .initializeSchema(false)
                // 使用 TokenCountBatchingStrategy 优化批量向量化性能
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }
}
