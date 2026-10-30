package com.miribar.ordering.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应结构
 *
 * <p>所有分页查询接口都返回该结构，包含总条数、当前页码、每页条数和数据列表。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    /** 数据列表（当前页） */
    private List<T> list;

    /** 总条数（满足条件的全部记录数） */
    private long total;

    /** 当前页码，从 1 开始 */
    private int page;

    /** 每页条数 */
    private int pageSize;

    /** 总页数（由 total 与 pageSize 推导，方便前端直接渲染） */
    private int pages;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.list = list == null ? Collections.emptyList() : list;
        result.total = total;
        result.page = page;
        result.pageSize = pageSize;
        result.pages = total <= 0 || pageSize <= 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
        return result;
    }

    /** 空结果（页码越界时使用，仍返回正确的分页元信息） */
    public static <T> PageResult<T> empty(long total, int page, int pageSize) {
        return of(Collections.emptyList(), total, page, pageSize);
    }
}
