package com.edutrack.util;

/**
 * Pagination parameters for list views (Section 17: search, filter, paginate).
 */
public final class Pagination {
    private final int page;
    private final int pageSize;
    private final long totalRecords;

    public Pagination(int page, int pageSize, long totalRecords) {
        this.page = Math.max(1, page);
        this.pageSize = Math.max(1, pageSize);
        this.totalRecords = totalRecords;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < getTotalPages();
    }
}
