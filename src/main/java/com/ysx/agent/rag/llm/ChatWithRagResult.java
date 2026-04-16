package com.ysx.agent.rag.llm;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * RAG对话结果
 * 包含AI回答、检索到的文档列表和检索元数据
 */
public class ChatWithRagResult {

    private final String answer;
    private final List<Document> documents;
    private final String searchType;
    private final long latencyMs;

    public ChatWithRagResult(String answer, List<Document> documents,
                            String searchType, long latencyMs) {
        this.answer = answer;
        this.documents = documents;
        this.searchType = searchType;
        this.latencyMs = latencyMs;
    }

    public String getAnswer() {
        return answer;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getSearchType() {
        return searchType;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * 获取检索到的文档数量
     */
    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }

    /**
     * 判断是否有检索结果
     */
    public boolean hasDocuments() {
        return documents != null && !documents.isEmpty();
    }

    /**
     * 获取检索来源信息（文档ID列表）
     */
    public List<String> getDocumentIds() {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
                .map(Document::getId)
                .toList();
    }

    /**
     * 获取文档内容摘要
     */
    public List<String> getDocumentSummaries() {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
                .map(doc -> {
                    String text = doc.getText();
                    if (text == null) return "";
                    return text.length() > 100 ? text.substring(0, 100) + "..." : text;
                })
                .toList();
    }
}
