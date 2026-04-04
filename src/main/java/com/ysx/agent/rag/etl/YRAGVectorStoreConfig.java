/*
package com.ysx.agent.rag.etl;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YRAGVectorStoreConfig {

    @Resource
    private YRAGDocumentLoader yRAGAppDocumentLoader;

    @Resource
    private EmbeddingModel dashscopeEmbeddingModel;

    @Resource
    private VectorStoreFactory vectorStoreFactory;

    public VectorStore loveAppVectorStore(Long kb_id) {
        VectorStore vs = vectorStoreFactory.createVectorStore(kb_id);
        // 加载文档
        List<Document> documents = yRAGAppDocumentLoader.loadMarkdowns(kb_id);
        vs.add(documents);
        return vs;
    }
}
*/
