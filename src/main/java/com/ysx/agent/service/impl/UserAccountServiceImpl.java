package com.ysx.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysx.agent.domain.UserAccount;
import com.ysx.agent.service.UserAccountService;
import com.ysx.agent.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author ysx
* @description 针对表【user_account】的数据库操作Service实现
* @createDate 2026-03-30 12:13:01
*/
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
    implements UserAccountService{

}




