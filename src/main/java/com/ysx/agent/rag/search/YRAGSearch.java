package com.ysx.agent.rag.search;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class YRAGSearch {
    private final QueryTransformer queryTransformer;

    private final ChatClient.Builder chatClientBuilder;

    public YRAGSearch(ChatModel openAiChatModel) {
        ChatClient.Builder builder = ChatClient.builder(openAiChatModel);
        // 创建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
        this.chatClientBuilder = ChatClient.builder(openAiChatModel);
    }

    /**
     * 执行查询重写
     *
     * @param prompt
     * @return
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        return transformedQuery.text();
    }

    /**
     * 扩展查询字符串，生成多个相关的查询
     * @param query 原始查询字符串
     * @return 扩展后的查询列表，包含多个相关的查询对象
    */
    public List<Query> expand(String query) {
        // 创建多查询扩展器构建器
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                // 设置聊天客户端构建器
                .chatClientBuilder(chatClientBuilder)
                // 设置要生成的查询数量为3
                .numberOfQueries(3)
                // 构建多查询扩展器实例
                .build();
        // 使用原始查询和构建的扩展器生成并返回查询列表
        return queryExpander.expand(new Query(query));
    }

    /**
     * 静态方法：用于连接合并多个查询结果中的文档
     * @param documentsForQuery 一个映射，键为查询对象(Query)，值为对应的文档列表(List<List<Document>>)
     * @return 返回合并后的文档列表(List<Document>)
     */
    public List<Document> concatenationDocument(Map<Query, List<List<Document>>> documentsForQuery){
        // 创建一个连接文档连接器实例
        DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
        // 使用文档连接器合并传入的文档映射，并返回合并结果
        return documentJoiner.join(documentsForQuery);
    }
}
