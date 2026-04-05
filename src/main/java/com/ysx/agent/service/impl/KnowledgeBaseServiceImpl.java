package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysx.agent.domain.KnowledgeBase;
import com.ysx.agent.domain.Note;
import com.ysx.agent.dto.CreateKnowledgeBaseRequest;
import com.ysx.agent.dto.KnowledgeBaseListResponse;
import com.ysx.agent.dto.KnowledgeBaseResponse;
import com.ysx.agent.dto.UpdateKnowledgeBaseRequest;
import com.ysx.agent.exception.KnowledgeBaseAccessDeniedException;
import com.ysx.agent.exception.KnowledgeBaseNotFoundException;
import com.ysx.agent.mapper.KnowledgeBaseMapper;
import com.ysx.agent.mapper.NoteMapper;
import com.ysx.agent.service.KnowledgeBaseService;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库服务实现类
 * 处理知识库的 CRUD 操作，包含归属权校验、审计日志等
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseServiceImpl.class);

    private static final int DEFAULT_PAGE = 1;

    private static final int DEFAULT_SIZE = 10;

    private static final int MAX_SIZE = 100;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final NoteMapper noteMapper;

    public KnowledgeBaseServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper, NoteMapper noteMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.noteMapper = noteMapper;
    }

    /**
     * 创建知识库
     * 验证必填字段（名称），处理可选字段（描述、分类、标签）
     */
    @Override
    @Transactional
    public KnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request, Long userId) {
        Long targetId = null;
        try {
            String name = normalizeRequiredText(request.getName(), "名称不能为空");
            String description = normalizeOptionalText(request.getDescription());
            String category = normalizeOptionalText(request.getCategory());
            List<String> tags = normalizeTags(request.getTags());

            KnowledgeBase kb = new KnowledgeBase();
            kb.setOwner_user_id(userId);
            kb.setName(name);
            kb.setDescription(description);
            kb.setCategory(category);
            kb.setTags(encodeTags(tags));
            kb.setStatus(1);
            kb.setCreated_at(new Date());
            kb.setUpdated_at(new Date());

            knowledgeBaseMapper.insert(kb);
            targetId = kb.getId();
            KnowledgeBase persisted = knowledgeBaseMapper.selectById(kb.getId());
            KnowledgeBase finalKb = persisted != null ? persisted : kb;

            auditLog("CREATE", finalKb.getId(), userId, new Date(), "SUCCESS", null);
            return toResponse(finalKb);
        } catch (RuntimeException ex) {
            auditLog("CREATE", targetId, userId, new Date(), "FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 更新知识库
     * 支持部分更新，使用乐观锁机制（lastKnownUpdatedAt）处理并发冲突
     */
    @Override
    @Transactional
    public KnowledgeBaseResponse updateKnowledgeBase(Long id, UpdateKnowledgeBaseRequest request, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        try {
            KnowledgeBase current = loadByIdWithAccessCheck(id, userId, false);
            if (request == null) {
                auditLog("UPDATE", id, userId, new Date(), "SUCCESS", "NO_CHANGES");
                return toResponse(current);
            }
            Date lastKnownUpdatedAt = request.getLastKnownUpdatedAt() == null
                    ? new Date()
                    : Date.from(request.getLastKnownUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());

            UpdateWrapper<KnowledgeBase> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .eq("user_id", userId)
                    .isNull("deleted_at")
                    .set("updated_at", lastKnownUpdatedAt)
                    .set(request.getName() != null , "name", request.getName())
                    .set(request.getDescription() != null, "description", request.getDescription())
                    .set(request.getCategory() != null, "category", request.getCategory())
                    .set(request.getTags() != null, "tags", request.getTags());

            if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isBlank()) {
                auditLog("UPDATE", id, userId, new Date(), "SUCCESS", "NO_CHANGES");
                return toResponse(current);
            }

            updateWrapper.set("updated_at", new Date());
            this.update(updateWrapper);
            KnowledgeBase updated = loadByIdWithAccessCheck(id, userId, false);
            auditLog("UPDATE", id, userId, new Date(), "SUCCESS", null);
            return toResponse(updated);
        } catch (RuntimeException ex) {
            auditLog("UPDATE", id, userId, new Date(), "FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 删除知识库（软删除）
     * 设置 deleted_at 时间戳，同时更新 status 为 0
     */
    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id, Long userId) {
        try {
            KnowledgeBase current = loadByIdWithAccessCheck(id, userId, true);
            if (current.getDeleted_at() != null || (current.getStatus() != null && current.getStatus() == 0)) {
                auditLog("DELETE", id, userId, new Date(), "SUCCESS", "ALREADY_DELETED");
                return;
            }
            knowledgeBaseMapper.softDeleteByIdAndOwner(id, userId, new Date());
            auditLog("DELETE", id, userId, new Date(), "SUCCESS", null);
        } catch (RuntimeException ex) {
            auditLog("DELETE", id, userId, new Date(), "FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 获取知识库详情
     */
    @Override
    public KnowledgeBaseResponse getKnowledgeBaseById(Long id, Long userId) {
        KnowledgeBase kb = loadByIdWithAccessCheck(id, userId, false);
        return toResponse(kb);
    }

    /**
     * 分页查询知识库列表
     * 支持关键词（名称/描述模糊匹配）、分类、标签过滤
     */
    @Override
    public KnowledgeBaseListResponse listKnowledgeBases(String keyword,
                                                        String category,
                                                        String tag,
                                                        Integer page,
                                                        Integer size,
                                                        Long userId) {
        int pageNum = page == null || page < 1 ? DEFAULT_PAGE : page;
        int pageSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String normalizedKeyword = normalizeOptionalText(keyword);
        String normalizedCategory = normalizeOptionalText(category);
        String normalizedTag = normalizeOptionalText(tag);

        Page<KnowledgeBase> mpPage = new Page<>(pageNum, pageSize);
        Page<KnowledgeBase> resultPage = knowledgeBaseMapper.selectVisibleByOwnerAndFilters(
                userId,
                normalizedKeyword,
                normalizedCategory,
                normalizedTag,
                mpPage);

        List<KnowledgeBaseResponse> list = resultPage.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        KnowledgeBaseListResponse response = new KnowledgeBaseListResponse();
        response.setList(list);
        response.setTotal(resultPage.getTotal());
        response.setPage((int) resultPage.getCurrent());
        response.setSize((int) resultPage.getSize());
        return response;
    }

    /**
     * 根据知识库ID获取关联的笔记列表
     *
     * @param kbId 知识库ID
     * @return 该知识库下的所有笔记列表，若知识库不存在返回null
     */
    @Override
    public List<Note> getNotsByKnowledgeId(Long kbId) {
        if (kbId == null || kbId <= 0) {
            log.error("kbId is null or less than 0");
            return null;
        }
        KnowledgeBase knowledgeBase = this.getById(kbId);
        if (knowledgeBase == null) {
            log.error("kbId is not exist");
            return null;
        }
        return noteMapper.selectList(new QueryWrapper<Note>().eq("kb_id", kbId));
    }

    /**
     * 加载知识库并验证用户归属权
     *
     * @param id           知识库ID
     * @param userId       用户ID
     * @param allowDeleted 是否允许返回已删除的知识库
     * @return 知识库实体
     * @throws KnowledgeBaseNotFoundException     知识库不存在或已删除
     * @throws KnowledgeBaseAccessDeniedException 用户无权限访问该知识库
     */
    private KnowledgeBase loadByIdWithAccessCheck(Long id, Long userId, boolean allowDeleted) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) {
            throw new KnowledgeBaseNotFoundException(id);
        }
        if (!Objects.equals(kb.getUserId(), userId)) {
            throw new KnowledgeBaseAccessDeniedException("无权限访问该知识库");
        }
        if (!allowDeleted && (kb.getDeleted_at() != null || (kb.getStatus() != null && kb.getStatus() == 0))) {
            throw new KnowledgeBaseNotFoundException(id);
        }
        return kb;
    }

    /**
     * 将实体转换为响应DTO，同时解码标签列表
     */
    private KnowledgeBaseResponse toResponse(KnowledgeBase kb) {
        return KnowledgeBaseResponse.fromEntity(kb, decodeTags(kb.getTags()));
    }

    /**
     * 校验必填文本字段
     */
    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * 规范化可选文本字段，null或空返回null
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 规范化标签列表：去重、过滤null和空字符串、校验非法标签
     */
    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = tags.stream()
                .map(tag -> tag == null ? null : tag.trim())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (normalized.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("标签不能为空");
        }
        return normalized.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 标签列表编码为逗号分隔字符串
     */
    private String encodeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }

    /**
     * 逗号分隔字符串解码为标签列表
     */
    private List<String> decodeTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 审计日志记录
     * 记录知识库操作类型、ID、操作用户、时间、结果和原因
     */
    private void auditLog(String operationType,
                          Long knowledgeBaseId,
                          Long operatorUserId,
                          Date operationAt,
                          String result,
                          String reason) {
        log.info(
                "KnowledgeBaseAudit operationType={} knowledgeBaseId={} operatorUserId={} new Date()={} result={} reason={}",
                operationType,
                knowledgeBaseId,
                operatorUserId,
                operationAt,
                result,
                reason);
    }
}
