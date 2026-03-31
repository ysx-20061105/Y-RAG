package com.ysx.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateKnowledgeBaseRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过255")
    private String name;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    @Size(max = 128, message = "分类长度不能超过128")
    private String category;

    @Size(max = 20, message = "标签数量不能超过20")
    private List<@NotBlank(message = "标签不能为空") @Size(max = 32, message = "标签长度不能超过32") String> tags;

    public CreateKnowledgeBaseRequest() {
    }

    public CreateKnowledgeBaseRequest(String name, String description, String category, List<String> tags) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
