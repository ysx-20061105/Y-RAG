package com.ysx.agent.service.impl;

import com.ysx.agent.dto.AuthResponse;
import com.ysx.agent.dto.LoginRequest;
import com.ysx.agent.dto.RegisterRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class AuthServiceImplTest {

    @Resource
    private AuthServiceImpl authService;

    @Test
    void register() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhone("17303883112");
        registerRequest.setEmail("3088964573@qq.com");
        registerRequest.setPassword("123456");

        Long register = authService.register(registerRequest);
        assertNotNull(register, "注册失败");
    }

    @Test
    void login() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("17303883112");
        loginRequest.setIdentifierType(LoginRequest.IdentifierType.PHONE);
        loginRequest.setPassword("123456");
        AuthResponse login = authService.login(loginRequest);
        assertNotNull(login, "登录失败");
    }

    @Test
    void logout() {
    }
}
