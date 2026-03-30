package com.ysx.agent.service;

import com.ysx.agent.dto.AuthResponse;
import com.ysx.agent.dto.LoginRequest;
import com.ysx.agent.dto.RegisterRequest;

public interface AuthService {

    Long register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout();
}
