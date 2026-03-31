package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName knowledge_base
 */
@TableName(value ="knowledge_base")
public class KnowledgeBase implements Serializable {
    /**
     * 知识库ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 知识库名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 知识库描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 知识库分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 标签，逗号分隔
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 1启用 0禁用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "created_at")
    private Date created_at;

    /**
     * 
     */
    @TableField(value = "updated_at")
    private Date updated_at;

    /**
     * 
     */
    @TableField(value = "deleted_at")
    private Date deleted_at;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 知识库ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 知识库ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 所属用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 所属用户ID
     */
    public void setOwner_user_id(Long userId) {
        this.userId = userId;
    }

    /**
     * 知识库名称
     */
    public String getName() {
        return name;
    }

    /**
     * 知识库名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 知识库描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 知识库描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 知识库分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 知识库分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 标签，逗号分隔
     */
    public String getTags() {
        return tags;
    }

    /**
     * 标签，逗号分隔
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 1启用 0禁用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 1启用 0禁用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * 
     */
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    /**
     * 
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * 
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    /**
     * 
     */
    public Date getDeleted_at() {
        return deleted_at;
    }

    /**
     * 
     */
    public void setDeleted_at(Date deleted_at) {
        this.deleted_at = deleted_at;
    }
}