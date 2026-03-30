package com.ysx.agent.dto;

import java.util.List;

public class NoteListResponse {

    private List<NoteResponse> list;

    private Long total;

    private Integer page;

    private Integer size;

    public NoteListResponse() {
    }

    public NoteListResponse(List<NoteResponse> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<NoteResponse> getList() {
        return list;
    }

    public void setList(List<NoteResponse> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
