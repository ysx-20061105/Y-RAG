package com.ysx.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysx.agent.domain.KnowledgeBase;
import com.ysx.agent.domain.Note;
import com.ysx.agent.dto.CreateKnowledgeBaseRequest;
import com.ysx.agent.dto.KnowledgeBaseListResponse;
import com.ysx.agent.dto.KnowledgeBaseResponse;
import com.ysx.agent.dto.UpdateKnowledgeBaseRequest;

import java.util.List;

/**
* @author ysx
* @description 针对表【knowledge_base】的数据库操作Service
* @createDate 2026-03-30 12:13:01
*/
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    KnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request, Long userId);

    KnowledgeBaseResponse updateKnowledgeBase(Long id, UpdateKnowledgeBaseRequest request, Long userId);

    void deleteKnowledgeBase(Long id, Long userId);

    KnowledgeBaseResponse getKnowledgeBaseById(Long id, Long userId);

    KnowledgeBaseListResponse listKnowledgeBases(String keyword, String category, String tag, Integer page, Integer size, Long userId);

    /**
     * 根据知识库id获取笔记列表
     * @param kbId
     * @return
     */
    List<Note> getNotsByKnowledgeId(Long kbId);
}
