package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName knowledge_base
 */
@TableName(value ="knowledge_base")
public class KnowledgeBase implements Serializable {
    private Long id;

    private Long owner_user_id;

    private String name;

    private String description;

    private Integer status;

    private Date created_at;

    private Date updated_at;

    private Date deleted_at;

    private static final long serialVersionUID = 1L;

    public KnowledgeBase() {
    }

    public KnowledgeBase(Long id, Long owner_user_id, String name, String description, Integer status, Date created_at,
                         Date updated_at, Date deleted_at) {
        this.id = id;
        this.owner_user_id = owner_user_id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.deleted_at = deleted_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwner_user_id() {
        return owner_user_id;
    }

    public void setOwner_user_id(Long owner_user_id) {
        this.owner_user_id = owner_user_id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public Date getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Date deleted_at) {
        this.deleted_at = deleted_at;
    }
}
