package com.ysx.agent.rag.llm;

import com.ysx.agent.rag.advisor.MyLoggerAdvisor;
import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.rag.search.RetrievalResult;
import com.ysx.agent.rag.search.YRAGRagAdvisorFactory;
import com.ysx.agent.rag.search.YRAGSearch;
import com.ysx.agent.service.HybridSearchService;
import io.qdrant.client.QdrantClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.util.List;

@Component
public class YRAGAgent {

    private static final Logger log = LoggerFactory.getLogger(YRAGAgent.class);

    private final ChatClient chatClient;
    private final YRAGSearch yragSearch;
    private final QdrantClient qdrantClient;
    private final EmbeddingModel ollamaEmbeddingModel;
    private final HybridSearchService hybridSearchService;
    private final String SYSTEM_PROMPT;

    public YRAGAgent(ChatModel openAiChatModel,
                     YRAGSearch yragSearch,
                     QdrantClient qdrantClient,
                     EmbeddingModel ollamaEmbeddingModel,
                     HybridSearchService hybridSearchService) throws Exception {
        this.SYSTEM_PROMPT = loadSystemPrompt();
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.yragSearch = yragSearch;
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
        this.hybridSearchService = hybridSearchService;
    }

    private String loadSystemPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompt/SYSTEM_PROMPT.md");
        return StreamUtils.copyToString(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 和 RAG 知识库进行对话（使用混合检索）
     */
    public ChatWithRagResult doChatWithRag(String collectionName, String message, String chatId) {
        return doChatWithRag(collectionName, message, chatId, null);
    }

    /**
     * 和 RAG 知识库进行对话（使用混合检索，支持自定义配置）
     * 返回包含回答和检索内容的完整结果
     */
    public ChatWithRagResult doChatWithRag(String collectionName, String message, String chatId, HybridSearchConfig config) {
        long startTime = System.currentTimeMillis();

        // 查询重写
        String rewrittenMessage = yragSearch.doQueryRewrite(message);

        // 使用混合检索获取文档
        RetrievalResult retrievalResult = hybridSearchService.search(rewrittenMessage, collectionName, config);

        if (!retrievalResult.hasResults()) {
            log.warn("No documents found for query: {}", message);
            return new ChatWithRagResult(
                    "抱歉，未找到与您问题相关的知识库内容。请尝试调整问题的描述方式。",
                    List.of(),
                    retrievalResult.getSearchType(),
                    System.currentTimeMillis() - startTime
            );
        }

        // 将检索结果构建为上下文
        List<Document> documents = retrievalResult.getDocuments();
        String context = documents.stream()
                .map(doc -> "【文档 " + doc.getId() + "】\n" + doc.getText())
                .reduce("", (a, b) -> a + "\n\n" + b);

        String promptWithContext = String.format(
                "请根据以下知识库内容回答用户的问题。\n\n知识库内容：\n%s\n\n用户问题：%s",
                context,
                message
        );

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(promptWithContext)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();
        long latencyMs = System.currentTimeMillis() - startTime;

        log.info("Hybrid search result: {} documents, latency: {}ms",
                documents.size(), retrievalResult.getLatencyMs());

        return new ChatWithRagResult(answer, documents, retrievalResult.getSearchType(), latencyMs);
    }

    /**
     * 使用原有方式的RAG对话（向量检索）
     */
    public String doChatWithRagVector(String collectionName, String message, String chatId) {
        String rewrittenMessage = yragSearch.doQueryRewrite(message);
        VectorStore vectorStore = getVectorStore(qdrantClient, ollamaEmbeddingModel, collectionName, true);
        DocumentRetriever documentRetriever = getDocumentRetriever(vectorStore);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(new MyLoggerAdvisor())
                .advisors(YRAGRagAdvisorFactory.createYRAGRagAdvisor(documentRetriever))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    private VectorStore getVectorStore(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel,
                                        String collectionName, boolean initializeSchema) {
        return QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(collectionName)
                .initializeSchema(initializeSchema)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }

    private DocumentRetriever getDocumentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5)
                .build();
    }
}
