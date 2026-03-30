package com.ysx.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysx.agent.domain.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【note】的数据库操作Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    Page<Note> selectByKbId(@Param("kbId") Long kbId, Page<Note> page);

    Note selectByIdAndKbId(@Param("id") Long id, @Param("kbId") Long kbId);
}
