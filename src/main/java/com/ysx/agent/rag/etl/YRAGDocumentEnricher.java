package com.ysx.agent.rag.etl;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元数据增强器
 * KeywordMetadataEnricher:使用 Al 提取关键词并添加到元数据。
 * SummaryMetadataEnricher:使用 AI生成文档摘要并添加到元数据。
 */
@Component
public class YRAGDocumentEnricher {

    private final ChatModel openAiChatModel;

    YRAGDocumentEnricher(ChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    // 关键词元信息增强器
    public List<Document> enrichDocumentsByKeyword(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.openAiChatModel, 5);
        return enricher.apply(documents);
    }
  
    // 摘要元信息增强器
    public List<Document> enrichDocumentsBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(openAiChatModel,
            List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
        return enricher.apply(documents);
    }
}
