package com.ysx.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysx.agent.domain.Note;
import com.ysx.agent.domain.NoteVersion;
import com.ysx.agent.dto.CreateNoteRequest;
import com.ysx.agent.dto.NoteListResponse;
import com.ysx.agent.dto.NoteResponse;
import com.ysx.agent.dto.UpdateNoteRequest;

/**
 * 笔记服务接口
 * 定义笔记的创建、更新、删除、查询等业务操作
 */
public interface NoteService extends IService<Note> {

    /**
     * 创建笔记
     *
     * @param request 笔记创建请求参数
     * @param userId  所有者用户ID
     * @return 创建成功的笔记信息
     */
    NoteResponse createNote(CreateNoteRequest request, Long userId);

    /**
     * 更新笔记
     *
     * @param noteId  笔记ID
     * @param request 笔记更新请求参数
     * @param userId  所有者用户ID
     * @return 更新后的笔记信息
     */
    NoteResponse updateNote(Long noteId, UpdateNoteRequest request, Long userId);

    /**
     * 删除笔记
     *
     * @param noteId 笔记ID
     * @param userId 所有者用户ID
     */
    void deleteNote(Long noteId, Long userId);

    /**
     * 获取笔记详情
     *
     * @param noteId 笔记ID
     * @param userId 所有者用户ID
     * @return 笔记详细信息
     */
    NoteResponse getNoteById(Long noteId, Long userId);

    /**
     * 分页查询指定知识库下的笔记列表
     *
     * @param kbId   知识库ID
     * @param page   页码，默认为1
     * @param size   每页大小，默认为10，最大100
     * @param userId 所有者用户ID
     * @return 笔记列表及分页信息
     */
    NoteListResponse listNotes(Long kbId, Integer page, Integer size, Long userId);
}
