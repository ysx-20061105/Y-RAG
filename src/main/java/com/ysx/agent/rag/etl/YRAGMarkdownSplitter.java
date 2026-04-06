package com.ysx.agent.rag.etl;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * markdown文档切割，根据二级标题切分，并写入标题元信息
 */
@Component
public class YRAGMarkdownSplitter {

    private static final Pattern H2_PATTERN = Pattern.compile("(?m)^##\\s+(.+?)\\s*$");
    private static final String H2_METADATA_KEY = "h2_title";

    public List<Document> splitDocuments(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document document : documents) {
            result.addAll(splitSingleDocument(document));
        }
        return result;
    }

    private List<Document> splitSingleDocument(Document document) {
        String content = document.getText();
        if (content == null || content.isBlank()) {
            return List.of(document);
        }

        Matcher matcher = H2_PATTERN.matcher(content);
        List<Document> result = new ArrayList<>();
        String currentTitle = null;
        int lastStart = 0;
        boolean foundH2 = false;

        while (matcher.find()) {
            foundH2 = true;
            if (matcher.start() > lastStart) {
                String section = content.substring(lastStart, matcher.start()).trim();
                if (!section.isBlank()) {
                    result.add(buildDocument(section, document, currentTitle));
                }
            }
            currentTitle = matcher.group(1).trim();
            lastStart = matcher.start();
        }

        if (!foundH2) {
            return List.of(document);
        }

        String section = content.substring(lastStart).trim();
        if (!section.isBlank()) {
            result.add(buildDocument(section, document, currentTitle));
        }

        return result;
    }

    private Document buildDocument(String content, Document source, String h2Title) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (source.getMetadata() != null) {
            metadata.putAll(source.getMetadata());
        }
        if (h2Title != null && !h2Title.isBlank()) {
            metadata.put(H2_METADATA_KEY, h2Title);
        }
        return new Document(content, metadata);
    }
}
