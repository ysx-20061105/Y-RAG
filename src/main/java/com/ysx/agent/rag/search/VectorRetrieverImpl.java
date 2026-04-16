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
 * 使用Qdrant向量数据库进行相似度检索
 */
@Component
public class VectorRetrieverImpl implements VectorRetriever {

    private static final Logger log = LoggerFactory.getLogger(VectorRetrieverImpl.class);

    private final QdrantClient qdrantClient;
    private final EmbeddingModel ollamaEmbeddingModel;

    public VectorRetrieverImpl(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel) {
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
    }

    @Override
    public List<Document> retrieve(String query, String collectionName, int topK, double similarityThreshold) {
        log.debug("Executing vector retrieval: query={}, collection={}, topK={}, threshold={}",
                query, collectionName, topK, similarityThreshold);

        try {
            VectorStore vectorStore = createVectorStore(collectionName);

            VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();

            return retriever.retrieve(new Query(query));
        } catch (Exception e) {
            log.error("Vector retrieval failed: {}", e.getMessage(), e);
            throw new RuntimeException("Vector retrieval failed: " + e.getMessage(), e);
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
