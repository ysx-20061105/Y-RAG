package com.ysx.agent.rag.search;

import com.ysx.agent.rag.etl.QdrantProperties;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YRAGSearchTest {
    @Resource
    private YRAGSearch yragSearch;
    @Resource
    private QdrantClient qdrantClient;
    @Resource
    private EmbeddingModel ollamaEmbeddingModel;
    @Resource
    private QdrantProperties qdrantProperties;

    @Test
    void testSearch() {
        String name="kb_"+1;
        VectorStore vectorStore = QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(name)
                .initializeSchema(qdrantProperties.isInitializeSchema())
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();

//        List<Document> documents1 = vectorStore.similaritySearch("基本类型");

        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5)
                .build();
        // 直接用扩展后的查询来获取文档
//        Query query = Query.builder()
//                .text("基本类型")
//                .build();

//        List<Document> documents = retriever.retrieve(query);
        String queryText = yragSearch.doQueryRewrite("java中什么是包装类型？？？？");
        List<Query> querys = yragSearch.expand(queryText);
        Map<Query, List<List<Document>>> documentsForQuery=new HashMap<>();
        querys.forEach(query -> {
            List<Document> retrievedDocuments = retriever.retrieve(query);
            ArrayList<List<Document>> documents = new ArrayList<>();
            documents.add(retrievedDocuments);
            documentsForQuery.put(query,documents);
        });
        List<Document> documents = yragSearch.concatenationDocument(documentsForQuery);
        // 输出扩展后的查询文本
        Assertions.assertNotNull(documents);

    }
}