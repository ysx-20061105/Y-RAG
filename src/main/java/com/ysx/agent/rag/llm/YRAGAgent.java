package com.ysx.agent.rag.llm;

import com.ysx.agent.rag.advisor.MyLoggerAdvisor;
import com.ysx.agent.rag.search.YRAGRagAdvisorFactory;
import com.ysx.agent.rag.search.YRAGSearch;
import com.ysx.agent.service.impl.NoteServiceImpl;
import io.qdrant.client.QdrantClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class YRAGAgent {

    private static final Logger log = LoggerFactory.getLogger(YRAGAgent.class);

    private final ChatClient chatClient;


    private final YRAGSearch yragSearch;

    private final QdrantClient qdrantClient;

    private final EmbeddingModel ollamaEmbeddingModel;

    private final String SYSTEM_PROMPT;

    public YRAGAgent(ChatModel openAiChatModel,
                     YRAGSearch yragSearch,
                     QdrantClient qdrantClient,
                     EmbeddingModel ollamaEmbeddingModel) throws Exception {
        this.SYSTEM_PROMPT = loadSystemPrompt();
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
        this.yragSearch=yragSearch;
        this.qdrantClient=qdrantClient;
        this.ollamaEmbeddingModel=ollamaEmbeddingModel;
    }

    private String loadSystemPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompt/SYSTEM_PROMPT.md");
        return StreamUtils.copyToString(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 和 RAG 知识库进行对话
     *
     * @param collectionName 集合名词
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String collectionName,String message, String chatId) {
        // 查询重写
        String rewrittenMessage = yragSearch.doQueryRewrite(message);
        VectorStore vectorStore = getVectorStore(qdrantClient,ollamaEmbeddingModel,collectionName,true);
        DocumentRetriever documentRetriever = getDocumentRetriever(vectorStore);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 检索增强服务（基于 Qdrant 向量存储）
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
                .advisors(
                        YRAGRagAdvisorFactory.createYRAGRagAdvisor(documentRetriever)
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    private VectorStore getVectorStore(QdrantClient qdrantClient, EmbeddingModel ollamaEmbeddingModel,String collectionName,boolean initializeSchema){
        VectorStore vectorStore = QdrantVectorStore.builder(qdrantClient, ollamaEmbeddingModel)
                .collectionName(collectionName)
                .initializeSchema(initializeSchema)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
        return vectorStore;
    }

    /**
     * 获取文档检索器的方法
     * @param vectorStore 向量存储对象，用于存储和检索文档向量
     * @return DocumentRetriever 配置好的文档检索器实例，用于基于相似度检索文档
    */
    private DocumentRetriever getDocumentRetriever(VectorStore vectorStore){
    // 创建并配置一个基于向量存储的文档检索器
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
            // 设置使用的向量存储
                .vectorStore(vectorStore)
            // 设置相似度阈值为0.5，低于此值的文档将被过滤掉
                .similarityThreshold(0.5)
            // 设置返回最相关的5个文档
                .topK(5)
            // 构建并返回检索器实例
                .build();
        return retriever;
    }
}
