package com.ysx.agent.rag.search;

/**
 * 预设检索模式
 */
public enum PresetMode {
    /**
     * 均衡模式 - 向量权重0.5，关键词权重0.5
     */
    BALANCED,

    /**
     * 语义优先模式 - 向量权重0.8，关键词权重0.2
     */
    SEMANTIC_FIRST,

    /**
     * 关键词优先模式 - 向量权重0.2，关键词权重0.8
     */
    KEYWORD_FIRST
}
