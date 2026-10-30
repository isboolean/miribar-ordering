package com.miribar.ordering.common;

/**
 * 分页参数规整工具
 *
 * <p>统一所有列表接口的分页默认值与取值规则：</p>
 * <ul>
 *   <li>页码从 1 开始，非法值（null / 小于 1）按 1 处理；</li>
 *   <li>每页条数只允许 10 / 15 / 30，其它取值回落到默认 10 条；</li>
 *   <li>偏移量统一按 (page - 1) * pageSize 计算。</li>
 * </ul>
 */
public final class PageQuery {

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 支持的每页条数选项 */
    public static final int[] PAGE_SIZE_OPTIONS = {10, 15, 30};

    private PageQuery() {
    }

    /** 规整页码 */
    public static int page(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    /** 规整每页条数，仅接受 10 / 15 / 30，其余回落默认值 */
    public static int pageSize(Integer pageSize) {
        if (pageSize != null) {
            for (int option : PAGE_SIZE_OPTIONS) {
                if (option == pageSize) {
                    return option;
                }
            }
        }
        return DEFAULT_PAGE_SIZE;
    }

    /** 计算 SQL 偏移量 */
    public static int offset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }
}
