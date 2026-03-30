package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysx.agent.domain.NoteVersion;
import com.ysx.agent.service.NoteVersionService;
import com.ysx.agent.mapper.NoteVersionMapper;
import org.springframework.stereotype.Service;

/**
* @author ysx
* @description 针对表【note_version】的数据库操作Service实现
* @createDate 2026-03-30 12:13:01
*/
@Service
public class NoteVersionServiceImpl extends ServiceImpl<NoteVersionMapper, NoteVersion>
    implements NoteVersionService{

}




