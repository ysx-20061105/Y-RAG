package com.ysx.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * @TableName note_version
 */
@TableName(value ="note_version")
public class NoteVersion implements Serializable {
    private Long id;

    private Long note_id;

    private Integer version_no;

    private String title;

    private String content;

    private Integer content_bytes;

    private Long created_by;

    private Date created_at;

    private static final long serialVersionUID = 1L;

    public NoteVersion() {
    }

    public NoteVersion(Long id, Long note_id, Integer version_no, String title, String content, Integer content_bytes,
                       Long created_by, Date created_at) {
        this.id = id;
        this.note_id = note_id;
        this.version_no = version_no;
        this.title = title;
        this.content = content;
        this.content_bytes = content_bytes;
        this.created_by = created_by;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNote_id() {
        return note_id;
    }

    public void setNote_id(Long note_id) {
        this.note_id = note_id;
    }

    public Integer getVersion_no() {
        return version_no;
    }

    public void setVersion_no(Integer version_no) {
        this.version_no = version_no;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getContent_bytes() {
        return content_bytes;
    }

    public void setContent_bytes(Integer content_bytes) {
        this.content_bytes = content_bytes;
    }

    public Long getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Long created_by) {
        this.created_by = created_by;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
