package com.ysx.agent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ysx.agent.dto.ApiResponse;
import com.ysx.agent.dto.CreateKnowledgeBaseRequest;
import com.ysx.agent.dto.KnowledgeBaseListResponse;
import com.ysx.agent.dto.KnowledgeBaseResponse;
import com.ysx.agent.dto.UpdateKnowledgeBaseRequest;
import com.ysx.agent.service.KnowledgeBaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理接口
 * 提供知识库的创建、更新、删除、查询等 CRUD 操作
 */
@RestController
@RequestMapping("/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 创建知识库
     *
     * @param request     知识库创建请求参数
     * @param httpRequest HTTP请求对象，用于提取用户ID
     * @return 创建成功的知识库信息
     */
    @PostMapping
    public ApiResponse<KnowledgeBaseResponse> create(@Valid @RequestBody CreateKnowledgeBaseRequest request,
                                                     HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.createKnowledgeBase(request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 更新知识库
     *
     * @param id          知识库ID
     * @param request     知识库更新请求参数
     * @param httpRequest HTTP请求对象，用于提取用户ID
     * @return 更新后的知识库信息
     */
    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> update(@PathVariable("id") Long id,
                                                     @Valid @RequestBody UpdateKnowledgeBaseRequest request,
                                                     HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.updateKnowledgeBase(id, request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 删除知识库
     *
     * @param id          知识库ID
     * @param httpRequest HTTP请求对象，用于提取用户ID
     * @return 无返回内容
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        knowledgeBaseService.deleteKnowledgeBase(id, userId);
        return ApiResponse.success(null);
    }

    /**
     * 获取知识库详情
     *
     * @param id 知识库ID
     * @param httpRequest HTTP请求对象，用于提取用户ID
     * @return 知识库详细信息
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> detail(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.getKnowledgeBaseById(id, userId);
        return ApiResponse.success(response);
    }

    /**
     * 分页查询知识库列表
     * 支持按关键词、分类、标签进行过滤筛选
     *
     * @param keyword 搜索关键词（可选），匹配知识库名称或描述
     * @param category 分类筛选（可选）
     * @param tag 标签筛选（可选）
     * @param page 页码（可选），默认为1
     * @param size 每页大小（可选），默认为10
     * @param httpRequest HTTP请求对象，用于提取用户ID
     * @return 知识库列表及分页信息
     */
    @GetMapping
    public ApiResponse<KnowledgeBaseListResponse> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseListResponse response = knowledgeBaseService.listKnowledgeBases(keyword, category, tag, page, size, userId);
        return ApiResponse.success(response);
    }

    /**
     * 从请求中提取用户ID
     * 优先级：请求属性 > Sa-Token登录会话
     *
     * @param request HTTP请求对象
     * @return 用户ID
     * @throws IllegalArgumentException 当用户未认证时抛出
     */
    private Long extractUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr instanceof Long) {
            return (Long) attr;
        }
        if (attr instanceof String str) {
            return Long.parseLong(str);
        }
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        throw new IllegalArgumentException("未认证用户");
    }
}
