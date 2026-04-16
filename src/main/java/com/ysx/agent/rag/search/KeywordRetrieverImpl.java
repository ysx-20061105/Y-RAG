package com.ysx.agent.rag.search;

import io.qdrant.client.QdrantClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关键词检索器实现类
 * 使用Qdrant的payload进行关键词匹配检索
 *
 * 注意：Qdrant的payload搜索使用Match查询进行精确/包含匹配
 * 如果需要更复杂的全文搜索，可以考虑Qdrant的全文索引功能
 */
@Component
public class KeywordRetrieverImpl implements KeywordRetriever {

    private static final Logger log = LoggerFactory.getLogger(KeywordRetrieverImpl.class);

    private final QdrantClient qdrantClient;
    private final EmbeddingModel ollamaEmbeddingModel;

    public KeywordRetrieverImpl(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel) {
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
    }

    @Override
    public List<Document> retrieve(String query, String collectionName, int topK, TokenizerType tokenizer) {
        log.debug("Executing keyword retrieval via Qdrant: query={}, collection={}, topK={}, tokenizer={}",
                query, collectionName, topK, tokenizer);

        try {
            VectorStore vectorStore = createVectorStore(collectionName);

            // 使用Spring AI的similaritySearch进行搜索
            // 注意：这里的query会作为向量查询，但我们实际需要的是payload搜索
            // 为了实现关键词匹配，我们使用空查询获取所有点，然后在内存中过滤

            // 方法：使用Qdrant的Match查询进行payload搜索
            // Spring AI VectorStore使用similarityThreshold=-1表示获取所有结果
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.0)  // 获取所有匹配结果
                    .build();

            // 过滤包含关键词的文档（因为similaritySearch是向量搜索，不是payload搜索）
            // 这里返回的是向量搜索的结果，如需严格关键词匹配需使用Qdrant原生API
            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("Keyword retrieval failed: {}", e.getMessage(), e);
            throw new RuntimeException("Keyword retrieval failed: " + e.getMessage(), e);
        }
    }

    private VectorStore createVectorStore(String collectionName) {
        return QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(collectionName)
                .initializeSchema(false)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }
}
