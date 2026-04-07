package com.ysx.agent.rag.search;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;

import java.util.List;
import java.util.Map;

public class YRAGContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
    }


}
