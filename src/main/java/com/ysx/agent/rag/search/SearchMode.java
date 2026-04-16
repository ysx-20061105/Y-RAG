package com.ysx.agent.rag.search;

/**
 * 检索模式枚举
 */
public enum SearchMode {
    /**
     * 混合检索 - 双通道并行
     */
    HYBRID,

    /**
     * 仅向量检索
     */
    VECTOR_ONLY,

    /**
     * 仅关键词检索
     */
    KEYWORD_ONLY
}
