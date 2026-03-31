package com.ysx.agent.dto;

import java.util.List;

public class KnowledgeBaseListResponse {

    private List<KnowledgeBaseResponse> list;

    private Long total;

    private Integer page;

    private Integer size;

    public KnowledgeBaseListResponse() {
    }

    public KnowledgeBaseListResponse(List<KnowledgeBaseResponse> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<KnowledgeBaseResponse> getList() {
        return list;
    }

    public void setList(List<KnowledgeBaseResponse> list) {
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
