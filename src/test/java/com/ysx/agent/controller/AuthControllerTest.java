package com.ysx.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysx.agent.dto.AuthResponse;
import com.ysx.agent.dto.LoginRequest;
import com.ysx.agent.dto.RegisterRequest;
import com.ysx.agent.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_shouldReturnUserIdInApiResponse() throws Exception {
        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(123L);

        RegisterRequest req = new RegisterRequest();
        req.setPhone("13800138000");
        req.setPassword("P@ssw0rd123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(123));
    }

    @Test
    void login_shouldReturnTokenInApiResponse() throws Exception {
        AuthResponse resp = new AuthResponse(123L, "token-xxx", 7200L);
        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        LoginRequest req = new LoginRequest();
        req.setIdentifierType(LoginRequest.IdentifierType.PHONE);
        req.setIdentifier("13800138000");
        req.setPassword("P@ssw0rd123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(123))
                .andExpect(jsonPath("$.data.token").value("token-xxx"));
    }
}
