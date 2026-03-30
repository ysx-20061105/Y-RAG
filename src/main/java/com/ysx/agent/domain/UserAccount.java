package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user_account
 */
@TableName(value ="user_account")
public class UserAccount implements Serializable {
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 邮箱，可用于登录/找回
     */
    @TableField(value = "email")
    private String email;

    /**
     * 密码哈希(BCrypt/Argon2)
     */
    @TableField(value = "password_hash")
    private String password_hash;

    /**
     * 展示昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField(value = "avatar_url")
    private String avatar_url;

    /**
     * 1正常 0禁用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_at")
    private Date last_login_at;

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
     * 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 用户ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 登录用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 登录用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 邮箱，可用于登录/找回
     */
    public String getEmail() {
        return email;
    }

    /**
     * 邮箱，可用于登录/找回
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 密码哈希(BCrypt/Argon2)
     */
    public String getPassword_hash() {
        return password_hash;
    }

    /**
     * 密码哈希(BCrypt/Argon2)
     */
    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    /**
     * 展示昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 展示昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 头像URL
     */
    public String getAvatar_url() {
        return avatar_url;
    }

    /**
     * 头像URL
     */
    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    /**
     * 1正常 0禁用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 1正常 0禁用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 最后登录时间
     */
    public Date getLast_login_at() {
        return last_login_at;
    }

    /**
     * 最后登录时间
     */
    public void setLast_login_at(Date last_login_at) {
        this.last_login_at = last_login_at;
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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        UserAccount other = (UserAccount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getPassword_hash() == null ? other.getPassword_hash() == null : this.getPassword_hash().equals(other.getPassword_hash()))
            && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
            && (this.getAvatar_url() == null ? other.getAvatar_url() == null : this.getAvatar_url().equals(other.getAvatar_url()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getLast_login_at() == null ? other.getLast_login_at() == null : this.getLast_login_at().equals(other.getLast_login_at()))
            && (this.getCreated_at() == null ? other.getCreated_at() == null : this.getCreated_at().equals(other.getCreated_at()))
            && (this.getUpdated_at() == null ? other.getUpdated_at() == null : this.getUpdated_at().equals(other.getUpdated_at()))
            && (this.getDeleted_at() == null ? other.getDeleted_at() == null : this.getDeleted_at().equals(other.getDeleted_at()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getPassword_hash() == null) ? 0 : getPassword_hash().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getAvatar_url() == null) ? 0 : getAvatar_url().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getLast_login_at() == null) ? 0 : getLast_login_at().hashCode());
        result = prime * result + ((getCreated_at() == null) ? 0 : getCreated_at().hashCode());
        result = prime * result + ((getUpdated_at() == null) ? 0 : getUpdated_at().hashCode());
        result = prime * result + ((getDeleted_at() == null) ? 0 : getDeleted_at().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", username=").append(username);
        sb.append(", email=").append(email);
        sb.append(", password_hash=").append(password_hash);
        sb.append(", nickname=").append(nickname);
        sb.append(", avatar_url=").append(avatar_url);
        sb.append(", status=").append(status);
        sb.append(", last_login_at=").append(last_login_at);
        sb.append(", created_at=").append(created_at);
        sb.append(", updated_at=").append(updated_at);
        sb.append(", deleted_at=").append(deleted_at);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}