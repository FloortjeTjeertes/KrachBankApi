package com.krachbank.api.filters;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class BaseFilter {
    private Integer limit;
    private Integer page;

     public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
    public Pageable toPageAble() {
        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;
        return PageRequest.of(pageNum, pageSize);
    }
}
