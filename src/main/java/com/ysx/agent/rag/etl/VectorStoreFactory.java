package com.ysx.agent.rag.etl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import java.lang.reflect.Method;

/**
 * Factory to create a VectorStore instance with optional Qdrant backend via reflection.
 * Falls back to SimpleVectorStore if Qdrant classes are not present or on error.
 */
@Component
public class VectorStoreFactory {
    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private com.ysx.agent.rag.etl.QdrantProperties qdrantProperties;
    

    public VectorStore createVectorStore(Long kb_id) {
        // If YAML config disables Qdrant, fallback to SimpleVectorStore
        if (qdrantProperties != null && !qdrantProperties.isEnabled()) {
            return SimpleVectorStore.builder(embeddingModel).build();
        }
        String collectionNameForKb = sanitizeKbId(kb_id);
        try {
            // Attempt to use QdrantVectorStore via reflection
            Class<?> vecStoreClass = Class.forName("org.springframework.ai.vectorstore.qdrant.QdrantVectorStore");
            // Try to obtain a builder with (QdrantClient, EmbeddingModel)
            Class<?> clientClass = Class.forName("org.springframework.ai.vectorstore.qdrant.QdrantClient");
            Object qdrantClient = null;
            // Try a static factory: QdrantClient.newClient()
            try {
                Method newClient = clientClass.getMethod("newClient");
                qdrantClient = newClient.invoke(null);
            } catch (Exception e) {
                // If not available, keep null to trigger fallback
            }
            Method builderMethod = null;
            // Try common signatures first
            try {
                builderMethod = vecStoreClass.getMethod("builder", Object.class, EmbeddingModel.class);
            } catch (NoSuchMethodException nsm) {
                try {
                    builderMethod = vecStoreClass.getMethod("builder", Class.forName("org.springframework.ai.vectorstore.qdrant.QdrantClient"), EmbeddingModel.class);
                } catch (Exception e2) {
                    builderMethod = null;
                }
            }
            if (builderMethod == null) {
                throw new NoSuchMethodException("No suitable builder() signature found for QdrantVectorStore");
            }
            Object builder = builderMethod.invoke(null, qdrantClient, embeddingModel);
            Method collectionNameMethod = builder.getClass().getMethod("collectionName", String.class);
            Object b2 = collectionNameMethod.invoke(builder, collectionNameForKb);
            Method initSchema = b2.getClass().getMethod("initializeSchema", boolean.class);
            Object b3 = initSchema.invoke(b2, true);
            Method build = b3.getClass().getMethod("build");
            Object vectorStore = build.invoke(b3);
            return (VectorStore) vectorStore;
        } catch (Exception ignored) {
            // Fallback to in-memory SimpleVectorStore
            return SimpleVectorStore.builder(embeddingModel).build();
        }
    }

    private String sanitizeKbId(Long kbId) {
        String s = kbId == null ? "" : kbId.toString();
        if (s.isEmpty()) return "kb_";
        return "kb_" + s.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
