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

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ApiResponse<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.createNote(request, userId);
        ApiResponse<NoteResponse> api = ApiResponse.success(resp);
        maybeAttachRemainingCapacity(api, resp.getContent());
        return api;
    }

    @GetMapping("/{id}")
    public ApiResponse<NoteResponse> detail(@PathVariable("id") Long id) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.getNoteById(id, userId);
        return ApiResponse.success(resp);
    }

    @GetMapping("/list")
    public ApiResponse<NoteListResponse> list(
            @RequestParam("kbId") Long kbId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        Long userId = extractUserId();
        NoteListResponse resp = noteService.listNotes(kbId, page, size, userId);
        return ApiResponse.success(resp);
    }

    @PutMapping("/{id}")
    public ApiResponse<NoteResponse> update(@PathVariable("id") Long id,
                                            @Valid @RequestBody UpdateNoteRequest request) {
        Long userId = extractUserId();
        NoteResponse resp = noteService.updateNote(id, request, userId);
        ApiResponse<NoteResponse> api = ApiResponse.success(resp);
        maybeAttachRemainingCapacity(api, resp.getContent());
        return api;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        Long userId = extractUserId();
        noteService.deleteNote(id, userId);
        return ApiResponse.success(null);
    }

    private Long extractUserId() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        throw new IllegalArgumentException("未认证用户");
    }

    private void maybeAttachRemainingCapacity(ApiResponse<?> api, String content) {
        if (content == null) {
            return;
        }
        int bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int maxBytes = 10_485_760;
        int threshold = 8 * 1024 * 1024;
        if (bytes > threshold) {
            api.setRemainingCapacity((long) (maxBytes - bytes));
        }
    }
}
