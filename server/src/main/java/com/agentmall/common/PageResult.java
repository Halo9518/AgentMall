package com.agentmall.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页响应体
 */
@Schema(description = "分页响应")
public class PageResult<T> {

    @Schema(description = "记录列表")
    private List<T> records;

    @Schema(description = "当前页码")
    private long page;

    @Schema(description = "每页条数")
    private long pageSize;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "总页数")
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long page, long pageSize, long total) {
        this.records = records;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 从 MyBatis-Plus IPage 构造
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal());
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
