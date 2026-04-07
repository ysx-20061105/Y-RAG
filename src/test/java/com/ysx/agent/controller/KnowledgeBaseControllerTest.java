package com.ysx.agent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ysx.agent.dto.CreateKnowledgeBaseRequest;
import com.ysx.agent.dto.KnowledgeBaseListResponse;
import com.ysx.agent.dto.KnowledgeBaseResponse;
import com.ysx.agent.dto.UpdateKnowledgeBaseRequest;
import com.ysx.agent.exception.GlobalExceptionHandler;
import com.ysx.agent.exception.KnowledgeBaseAccessDeniedException;
import com.ysx.agent.exception.KnowledgeBaseConflictException;
import com.ysx.agent.service.KnowledgeBaseService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseControllerTest {

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        KnowledgeBaseController controller = new KnowledgeBaseController(knowledgeBaseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void create_shouldReturnApiResponse() throws Exception {
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest(
                "团队知识库",
                "说明",
                "技术",
                List.of("Java", "后端"));

        KnowledgeBaseResponse response = new KnowledgeBaseResponse();
        response.setId(101L);
        response.setOwnerUserId(2001L);
        response.setName("团队知识库");
        response.setTags(List.of("Java", "后端"));

        when(knowledgeBaseService.createKnowledgeBase(any(CreateKnowledgeBaseRequest.class), eq(2001L)))
                .thenReturn(response);

        mockMvc.perform(post("/knowledge-bases")
                        .requestAttr("userId", 2001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.name").value("团队知识库"));
    }

    @Test
    void update_shouldReturnConflictWhenServiceThrows() throws Exception {
        UpdateKnowledgeBaseRequest request = new UpdateKnowledgeBaseRequest(
                "团队知识库-更新",
                "说明",
                "技术",
                List.of("Java"),
                LocalDateTime.of(2026, 3, 30, 10, 0, 0));

        doThrow(new KnowledgeBaseConflictException("RESOURCE_CONFLICT"))
                .when(knowledgeBaseService)
                .updateKnowledgeBase(eq(101L), any(UpdateKnowledgeBaseRequest.class), eq(2001L));

        mockMvc.perform(put("/knowledge-bases/101")
                        .requestAttr("userId", 2001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("RESOURCE_CONFLICT"));
    }

    @Test
    void delete_shouldCallServiceAndReturnSuccess() throws Exception {
        mockMvc.perform(delete("/knowledge-bases/101")
                        .requestAttr("userId", 2001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeBaseService).deleteKnowledgeBase(101L, 2001L);
    }

    @Test
    void list_shouldReturnPagedResult() throws Exception {
        KnowledgeBaseResponse item = new KnowledgeBaseResponse();
        item.setId(101L);
        item.setName("团队知识库");
        item.setTags(List.of("Java"));
        KnowledgeBaseListResponse response = new KnowledgeBaseListResponse(List.of(item), 1L, 1, 10);

        when(knowledgeBaseService.listKnowledgeBases("团队", "技术", "Java", 1, 10, 2001L))
                .thenReturn(response);

        mockMvc.perform(get("/knowledge-bases")
                        .requestAttr("userId", 2001L)
                        .param("keyword", "团队")
                        .param("category", "技术")
                        .param("tag", "Java")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(101));
    }

    @Test
    void detail_shouldReturnForbiddenWhenServiceThrowsAccessDenied() throws Exception {
        doThrow(new KnowledgeBaseAccessDeniedException("无权限访问该知识库"))
                .when(knowledgeBaseService)
                .getKnowledgeBaseById(101L, 2001L);

        mockMvc.perform(get("/knowledge-bases/101")
                        .requestAttr("userId", 2001L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限访问该知识库"));
    }

    @Test
    void create_shouldReturnBadRequestWhenValidationFails() throws Exception {
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest(
                " ",
                "说明",
                "技术",
                List.of("Java"));

        mockMvc.perform(post("/knowledge-bases")
                        .requestAttr("userId", 2001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
