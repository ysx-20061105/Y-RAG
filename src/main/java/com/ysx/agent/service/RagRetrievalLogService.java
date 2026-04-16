package com.ysx.agent.service;

import com.ysx.agent.domain.RagRetrievalLog;
import com.ysx.agent.rag.config.HybridSearchConfig;

import java.util.List;

/**
 * 检索日志服务接口
 */
public interface RagRetrievalLogService {

    /**
     * 异步记录检索日志
     */
    void logRetrievalAsync(Long kbId, Long userId, String queryText,
                          String rewrittenQuery, HybridSearchConfig config,
                          int resultCount, long latencyMs);

    /**
     * 查询最近的检索日志
     */
    List<RagRetrievalLog> getRecentLogs(int limit);

    /**
     * 根据知识库ID查询检索日志
     */
    List<RagRetrievalLog> getLogsByKbId(Long kbId, int limit);
}
