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

@RestController
@RequestMapping("/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public ApiResponse<KnowledgeBaseResponse> create(@Valid @RequestBody CreateKnowledgeBaseRequest request,
                                                     HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.createKnowledgeBase(request, userId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> update(@PathVariable("id") Long id,
                                                     @Valid @RequestBody UpdateKnowledgeBaseRequest request,
                                                     HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.updateKnowledgeBase(id, request, userId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        knowledgeBaseService.deleteKnowledgeBase(id, userId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> detail(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        KnowledgeBaseResponse response = knowledgeBaseService.getKnowledgeBaseById(id, userId);
        return ApiResponse.success(response);
    }

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
