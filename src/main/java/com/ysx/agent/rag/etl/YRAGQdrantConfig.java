package com.ysx.agent.rag.etl;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YRAGQdrantConfig {

    @Resource
    private QdrantProperties qdrantProperties;

    @Bean
    public QdrantClient qdrantClient() {
        boolean isSecure = "https".equalsIgnoreCase(qdrantProperties.getScheme());
        QdrantGrpcClient.Builder grpcClientBuilder =
                QdrantGrpcClient.newBuilder(qdrantProperties.getHost(), qdrantProperties.getPort(), isSecure);
        if (qdrantProperties.getApiKey() != null && !qdrantProperties.getApiKey().isEmpty()) {
            grpcClientBuilder.withApiKey(qdrantProperties.getApiKey());
        }
        return new QdrantClient(grpcClientBuilder.build());
    }

    @Bean
    public VectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel) {
        return QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(qdrantProperties.getCollectionNamePrefix())
                .initializeSchema(qdrantProperties.isInitializeSchema())
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }

}