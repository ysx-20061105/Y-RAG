package com.ysx.agent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ysx.agent.dto.ApiResponse;
import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteListResponse;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;
import com.ysx.agent.service.NoteService;
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
 * 笔记管理接口
 * 提供笔记的创建、查询、更新、删除等 CRUD 操作
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    private static final int MAX_NOTE_BYTES = 10_485_760; // 10MB
    private static final int CAPACITY_CHECK_THRESHOLD = 8 * 1024 * 1024; // 8MB

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 创建笔记
     *
     * @param request 笔记创建请求参数
     * @return 创建成功的笔记信息，当内容超过8MB时附加剩余容量
     */
    @PostMapping
    public ApiResponse<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.createNote(request, userId);
        ApiResponse<NoteResponse> api = ApiResponse.success(resp);
        maybeAttachRemainingCapacity(api, resp.getContent());
        return api;
    }

    /**
     * 获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详细信息
     */
    @GetMapping("/{id}")
    public ApiResponse<NoteResponse> detail(@PathVariable("id") Long id) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.getNoteById(id, userId);
        return ApiResponse.success(resp);
    }

    /**
     * 分页查询指定知识库下的笔记列表
     *
     * @param kbId 知识库ID（必填）
     * @param page 页码（可选），默认为1
     * @param size 每页大小（可选），默认为10
     * @return 笔记列表及分页信息
     */
    @GetMapping("/list")
    public ApiResponse<NoteListResponse> list(
            @RequestParam("kbId") Long kbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        Long userId = extractUserId();
        NoteListResponse resp = noteService.listNotes(kbId, page, size, userId);
        return ApiResponse.success(resp);
    }

    /**
     * 更新笔记
     *
     * @param id      笔记ID
     * @param request 笔记更新请求参数
     * @return 更新后的笔记信息，当内容超过8MB时附加剩余容量
     */
    @PutMapping("/{id}")
    public ApiResponse<NoteResponse> update(@PathVariable("id") Long id,
                                            @Valid @RequestBody UpdateNoteRequest request) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.updateNote(id, request, userId);
        ApiResponse<NoteResponse> api = ApiResponse.success(resp);
        maybeAttachRemainingCapacity(api, resp.getContent());
        return api;
    }

    /**
     * 删除笔记
     *
     * @param id 笔记ID
     * @return 无返回内容
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        Long userId = extractUserId();
        noteService.deleteNote(id, userId);
        return ApiResponse.success(null);
    }

    /**
     * 从 Sa-Token 会话中提取当前登录用户ID
     *
     * @return 用户ID
     * @throws IllegalArgumentException 当用户未登录时抛出
     */
    private Long extractUserId() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        throw new IllegalArgumentException("未认证用户");
    }

    /**
     * 当笔记内容超过阈值时，计算并附加剩余容量信息
     * 用于提示用户当前笔记的剩余可用空间
     *
     * @param api     API响应对象
     * @param content 笔记内容
     */
    private void maybeAttachRemainingCapacity(ApiResponse<?> api, String content) {
        if (content == null) {
            return;
        }
        int bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (bytes > CAPACITY_CHECK_THRESHOLD) {
            api.setRemainingCapacity((long) (MAX_NOTE_BYTES - bytes));
        }
    }
}
