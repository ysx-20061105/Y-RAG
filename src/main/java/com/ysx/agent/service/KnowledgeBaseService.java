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
 * 知识库服务接口
 * 定义知识库的创建、更新、删除、查询等业务操作
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 创建知识库
     *
     * @param request 知识库创建请求参数
     * @param userId  所有者用户ID
     * @return 创建成功的知识库信息
     */
    KnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request, Long userId);

    /**
     * 更新知识库
     *
     * @param id      知识库ID
     * @param request 知识库更新请求参数
     * @param userId  所有者用户ID
     * @return 更新后的知识库信息
     */
    KnowledgeBaseResponse updateKnowledgeBase(Long id, UpdateKnowledgeBaseRequest request, Long userId);

    /**
     * 删除知识库（软删除）
     *
     * @param id     知识库ID
     * @param userId 所有者用户ID
     */
    void deleteKnowledgeBase(Long id, Long userId);

    /**
     * 获取知识库详情
     *
     * @param id 知识库ID
     * @param userId 所有者用户ID
     * @return 知识库详细信息
     */
    KnowledgeBaseResponse getKnowledgeBaseById(Long id, Long userId);

    /**
     * 分页查询知识库列表
     * 支持按关键词、分类、标签进行过滤筛选
     *
     * @param keyword 搜索关键词（可选），匹配名称或描述
     * @param category 分类筛选（可选）
     * @param tag 标签筛选（可选）
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10，最大100
     * @param userId 所有者用户ID
     * @return 知识库列表及分页信息
     */
    KnowledgeBaseListResponse listKnowledgeBases(String keyword, String category, String tag, Integer page, Integer size, Long userId);

    /**
     * 根据知识库ID获取关联的笔记列表
     *
     * @param kbId 知识库ID
     * @return 该知识库下的所有笔记列表
     */
    List<Note> getNotsByKnowledgeId(Long kbId);
}
