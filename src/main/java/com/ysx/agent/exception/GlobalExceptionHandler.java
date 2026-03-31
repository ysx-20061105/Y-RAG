package com.ysx.agent.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.ysx.agent.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoteNotFound(NoteNotFoundException ex) {
        ApiResponse<Void> body = ApiResponse.error(404, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoteAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(NoteAccessDeniedException ex) {
        ApiResponse<Void> body = ApiResponse.error(403, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(KnowledgeBaseNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeBaseNotFound(KnowledgeBaseNotFoundException ex) {
        ApiResponse<Void> body = ApiResponse.error(404, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(KnowledgeBaseAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeBaseAccessDenied(KnowledgeBaseAccessDeniedException ex) {
        ApiResponse<Void> body = ApiResponse.error(403, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(KnowledgeBaseConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeBaseConflict(KnowledgeBaseConflictException ex) {
        ApiResponse<Void> body = ApiResponse.error(409, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        ApiResponse<Void> body = ApiResponse.error(413, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数错误";
        ApiResponse<Void> body = ApiResponse.error(400, message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<Void> body = ApiResponse.error(400, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLogin(NotLoginException ex, HttpServletRequest request) {
        Object loginId = null;
        try {
            loginId = StpUtil.getLoginIdDefaultNull();
        } catch (Exception ignored) {
        }
        String clientIp = request.getRemoteAddr();
        log.info("LogoutEvent userId={} clientIp={} triggerType={}", loginId, clientIp, ex.getType());

        ApiResponse<Void> body = ApiResponse.error(401, "UNAUTHORIZED");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        ApiResponse<Void> body = ApiResponse.error(500, "服务器内部错误");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
