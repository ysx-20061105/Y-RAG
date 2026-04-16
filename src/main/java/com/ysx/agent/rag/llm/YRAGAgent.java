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
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 智能代理
 * <p>
 * 核心功能：整合混合检索与 LLM 对话能力，提供基于知识库的智能问答服务。
 * <p>
 * 主要能力：
 * <ul>
 *   <li>混合检索：结合向量检索与关键词检索，使用 RRF 算法融合结果</li>
 *   <li>查询重写：对用户问题进行改写，提升检索效果</li>
 *   <li>多轮对话记忆：基于 FileBasedChatMemory 实现持久化会话记忆</li>
 *   <li>流式输出：支持 SSE 流式响应，提升用户体验</li>
 * </ul>
 *
 * @author ysx
 * @since 2024-01-01
 */
@Component
public class YRAGAgent {

    private static final Logger log = LoggerFactory.getLogger(YRAGAgent.class);

    /**
     * Spring AI ChatClient，用于与 LLM 交互
     */
    private final ChatClient chatClient;

    /**
     * RAG 搜索组件，负责查询重写
     */
    private final YRAGSearch yragSearch;

    /**
     * Qdrant 向量数据库客户端
     */
    private final QdrantClient qdrantClient;

    /**
     * Ollama 嵌入模型，用于文本向量化
     */
    private final EmbeddingModel ollamaEmbeddingModel;

    /**
     * 混合检索服务，整合向量检索与关键词检索
     */
    private final HybridSearchService hybridSearchService;

    /**
     * 对话记忆管理器，支持多轮对话上下文
     */
    private final ChatMemory chatMemory;

    /**
     * 系统提示词，从 classpath 加载
     */
    private final String SYSTEM_PROMPT;

    /**
     * 构造函数
     *
     * @param openAiChatModel      聊天模型（Ollama/OpenAI 等）
     * @param yragSearch           RAG 搜索组件
     * @param qdrantClient         Qdrant 客户端
     * @param ollamaEmbeddingModel 嵌入模型
     * @param hybridSearchService  混合检索服务
     * @param chatMemory           对话记忆管理器
     * @throws Exception 加载系统提示词失败时抛出
     */
    public YRAGAgent(ChatModel openAiChatModel,
                     YRAGSearch yragSearch,
                     QdrantClient qdrantClient,
                     EmbeddingModel ollamaEmbeddingModel,
                     HybridSearchService hybridSearchService,
                     ChatMemory chatMemory) throws Exception {
        // 加载系统提示词
        this.SYSTEM_PROMPT = loadSystemPrompt();
        // 构建 ChatClient，配置系统提示词和日志 Advisor
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.yragSearch = yragSearch;
        this.qdrantClient = qdrantClient;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
        this.hybridSearchService = hybridSearchService;
        this.chatMemory = chatMemory;
    }

    /**
     * 从 classpath 加载系统提示词
     *
     * @return 系统提示词文本
     * @throws Exception 文件读取失败时抛出
     */
    private String loadSystemPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompt/SYSTEM_PROMPT.md");
        return StreamUtils.copyToString(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * <p>
     * 简单的 LLM 对话，使用 ChatMemory 实现会话上下文记忆。
     *
     * @param message 用户消息
     * @param chatId  会话 ID，用于关联对话历史
     * @return LLM 回复文本
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
     * <p>
     * 使用混合检索（向量+关键词）从知识库获取相关文档，
     * 并将文档内容作为上下文提供给 LLM 生成回答。
     *
     * @param collectionName 知识库集合名称
     * @param message        用户问题
     * @param chatId         会话 ID
     * @return 包含回答、检索文档、搜索类型和延迟的完整结果
     */
    public ChatWithRagResult doChatWithRag(String collectionName, String message, String chatId) {
        return doChatWithRag(collectionName, message, chatId, null);
    }

    /**
     * 和 RAG 知识库进行对话（使用混合检索，支持自定义配置）
     * <p>
     * 完整的 RAG 对话流程：
     * <ol>
     *   <li>查询重写：对用户问题进行改写，提升检索效果</li>
     *   <li>混合检索：使用配置中的权重融合向量和关键词检索结果</li>
     *   <li>上下文构建：将检索文档格式化为上下文</li>
     *   <li>LLM 生成：基于上下文生成回答</li>
     * </ol>
     *
     * @param collectionName 知识库集合名称
     * @param message        用户问题
     * @param chatId         会话 ID
     * @param config         混合检索配置，传 null 使用默认配置
     * @return 包含回答和检索内容的完整结果
     */
    public ChatWithRagResult doChatWithRag(String collectionName, String message, String chatId, HybridSearchConfig config) {
        long startTime = System.currentTimeMillis();

        // Step 1: 查询重写
        String rewrittenMessage = yragSearch.doQueryRewrite(message);

        // Step 2: 混合检索获取文档
        RetrievalResult retrievalResult = hybridSearchService.search(rewrittenMessage, collectionName, config);

        // 无检索结果时返回友好提示
        if (!retrievalResult.hasResults()) {
            log.warn("No documents found for query: {}", message);
            return new ChatWithRagResult(
                    "抱歉，未找到与您问题相关的知识库内容。请尝试调整问题的描述方式。",
                    List.of(),
                    retrievalResult.getSearchType(),
                    System.currentTimeMillis() - startTime
            );
        }

        // Step 3: 构建上下文
        List<Document> documents = retrievalResult.getDocuments();
        String context = documents.stream()
                .map(doc -> "【文档 " + doc.getId() + "】\n" + doc.getText())
                .reduce("", (a, b) -> a + "\n\n" + b);

        String promptWithContext = String.format(
                "请根据以下知识库内容回答用户的问题。\n\n知识库内容：\n%s\n\n用户问题：%s",
                context,
                message
        );

        // Step 4: LLM 生成回答
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
     * 和 RAG 知识库进行对话（使用混合检索）- 流式输出
     * <p>
     * 与 {@link #doChatWithRag(String, String, String)} 相同，但使用 SSE 流式返回 AI 生成的内容，
     * 提升长回答场景下的用户体验。
     *
     * @param collectionName 知识库集合名称
     * @param message        用户问题
     * @param chatId         会话 ID
     * @return 流式文本片段
     */
    public Flux<String> doChatWithRagStream(String collectionName, String message, String chatId) {
        return doChatWithRagStream(collectionName, message, chatId, null);
    }

    /**
     * 和 RAG 知识库进行对话（使用混合检索，支持自定义配置）- 流式输出
     * <p>
     * 完整的流式 RAG 对话流程，实时返回 AI 生成的文本片段。
     *
     * @param collectionName 知识库集合名称
     * @param message        用户问题
     * @param chatId         会话 ID
     * @param config         混合检索配置，传 null 使用默认配置
     * @return 流式文本片段
     */
    public Flux<String> doChatWithRagStream(String collectionName, String message, String chatId, HybridSearchConfig config) {
        // Step 1: 查询重写
        String rewrittenMessage = yragSearch.doQueryRewrite(message);

        // Step 2: 混合检索获取文档
        RetrievalResult retrievalResult = hybridSearchService.search(rewrittenMessage, collectionName, config);

        // 无检索结果时返回友好提示
        if (!retrievalResult.hasResults()) {
            log.warn("No documents found for query: {}", message);
            return Flux.just("抱歉，未找到与您问题相关的知识库内容。请尝试调整问题的描述方式。");
        }

        // Step 3: 构建上下文
        List<Document> documents = retrievalResult.getDocuments();
        String context = documents.stream()
                .map(doc -> "【文档 " + doc.getId() + "】\n" + doc.getText())
                .reduce("", (a, b) -> a + "\n\n" + b);

        String promptWithContext = String.format(
                "请根据以下知识库内容回答用户的问题。\n\n知识库内容：\n%s\n\n用户问题：%s",
                context,
                message
        );

        log.info("Hybrid search result: {} documents, latency: {}ms",
                documents.size(), retrievalResult.getLatencyMs());

        // Step 4: 流式调用 LLM
        return chatClient
                .prompt()
                .user(promptWithContext)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .chatResponse()
                .map(response -> {
                    String content = response.getResult().getOutput().getText();
                    return content != null ? content : "";
                });
    }

    /**
     * 使用原有方式的 RAG 对话（纯向量检索）
     * <p>
     * 区别于混合检索，该方法仅使用向量检索通过 Spring AI 的 RagAdvisor 实现。
     * 保留用于兼容性对比。
     *
     * @param collectionName 知识库集合名称
     * @param message        用户问题
     * @param chatId         会话 ID
     * @return LLM 回复文本
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

    /**
     * 构建 Qdrant 向量存储
     *
     * @param qdrantClient         Qdrant 客户端
     * @param ollamaEmbeddingModel 嵌入模型
     * @param collectionName      集合名称
     * @param initializeSchema    是否初始化 schema
     * @return 向量存储实例
     */
    private VectorStore getVectorStore(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel,
                                        String collectionName, boolean initializeSchema) {
        return QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(collectionName)
                .initializeSchema(initializeSchema)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }

    /**
     * 构建文档检索器
     *
     * @param vectorStore 向量存储
     * @return 文档检索器
     */
    private DocumentRetriever getDocumentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5)
                .build();
    }
}
