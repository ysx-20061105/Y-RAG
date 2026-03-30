package com.ysx.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {

    public enum IdentifierType {
        PHONE,
        EMAIL
    }

    @NotNull(message = "identifierType不能为空")
    private IdentifierType identifierType;

    @NotBlank(message = "identifier不能为空")
    private String identifier;

    @NotBlank(message = "密码不能为空")
    private String password;

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
