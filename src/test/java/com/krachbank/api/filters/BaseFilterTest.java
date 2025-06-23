package com.krachbank.api.filters;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.*;

class BaseFilterTest {

    @Test
    void testDefaultToPageAble() {
        BaseFilter filter = new BaseFilter();
        Pageable pageable = filter.toPageAble();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void testToPageAbleWithCustomLimitAndPage() {
        BaseFilter filter = new BaseFilter();
        filter.setLimit(25);
        filter.setPage(3);
        Pageable pageable = filter.toPageAble();
        assertEquals(2, pageable.getPageNumber()); // page is 3, so 3-1=2
        assertEquals(25, pageable.getPageSize());
    }

    @Test
    void testToPageAbleWithZeroAndNegativeValues() {
        BaseFilter filter = new BaseFilter();
        filter.setLimit(0);
        filter.setPage(0);
        Pageable pageable = filter.toPageAble();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        filter.setLimit(-5);
        filter.setPage(-2);
        pageable = filter.toPageAble();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void testGettersAndSetters() {
        BaseFilter filter = new BaseFilter();
        filter.setLimit(15);
        filter.setPage(4);
        assertEquals(15, filter.getLimit());
        assertEquals(4, filter.getPage());
    }
}