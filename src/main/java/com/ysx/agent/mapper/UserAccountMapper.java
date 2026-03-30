package com.ysx.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ysx.agent.domain.UserAccount;
import org.apache.ibatis.annotations.Param;

/**
* @author ysx
* @description 针对表【user_account】的数据库操作Mapper
* @createDate 2026-03-30 12:13:01
* @Entity com.ysx.agent.domain.UserAccount
*/
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    UserAccount selectByUsernameForLogin(@Param("username") String username);

    UserAccount selectByEmailForLogin(@Param("email") String email);
}




