package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysx.agent.domain.RagRetrievalLog;
import com.ysx.agent.mapper.RagRetrievalLogMapper;
import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.service.RagRetrievalLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检索日志服务实现类
 */
@Service
public class RagRetrievalLogServiceImpl implements RagRetrievalLogService {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalLogServiceImpl.class);

    private final RagRetrievalLogMapper ragRetrievalLogMapper;

    public RagRetrievalLogServiceImpl(RagRetrievalLogMapper ragRetrievalLogMapper) {
        this.ragRetrievalLogMapper = ragRetrievalLogMapper;
    }

    @Override
    @Async
    public void logRetrievalAsync(Long kbId, Long userId, String queryText,
                                 String rewrittenQuery, HybridSearchConfig config,
                                 int resultCount, long latencyMs) {
        try {
            RagRetrievalLog retrievalLog = new RagRetrievalLog();
            retrievalLog.setKb_id(kbId);
            retrievalLog.setActor_user_id(userId);
            retrievalLog.setQuery_text(queryText);
            retrievalLog.setRewritten_query(rewrittenQuery);
            retrievalLog.setTop_k(config.getTopK());
            retrievalLog.setLatency_ms((int) latencyMs);
            retrievalLog.setCreated_at(new Date());

            // 将检索到的chunk IDs转换为字符串列表
            retrievalLog.setRetrieved_chunk_ids(resultCount);

            ragRetrievalLogMapper.insert(retrievalLog);
            log.debug("Retrieval log saved: query={}, resultCount={}, latency={}ms",
                    queryText, resultCount, latencyMs);
        } catch (Exception e) {
            log.error("Failed to save retrieval log: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<RagRetrievalLog> getRecentLogs(int limit) {
        QueryWrapper<RagRetrievalLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at")
                .last("LIMIT " + limit);
        return ragRetrievalLogMapper.selectList(wrapper);
    }

    @Override
    public List<RagRetrievalLog> getLogsByKbId(Long kbId, int limit) {
        QueryWrapper<RagRetrievalLog> wrapper = new QueryWrapper<>();
        wrapper.eq("kb_id", kbId)
                .orderByDesc("created_at")
                .last("LIMIT " + limit);
        return ragRetrievalLogMapper.selectList(wrapper);
    }
}
