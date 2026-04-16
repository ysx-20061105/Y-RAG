package com.ysx.agent.rag.search;

import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class YRAGContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
    }


}
