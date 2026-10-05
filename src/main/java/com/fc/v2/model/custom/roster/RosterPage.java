package com.fc.v2.model.custom.roster;

import java.util.List;

/**
 * 名册分页：页内行与页首处数同源——都来自服务端这一次捞出的同一份基础数据。
 * 每页条数由区局在配置里给出（fuce.oads.roster.page-size），可继续下翻。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class RosterPage {

    /** 第几页（从 1 起） */
    private int pageNum;

    /** 每页条数（区局配置） */
    private int pageSize;

    /** 总行数 */
    private long total;

    /** 本季在册处数合计（页首与回执都取这一处写回的数） */
    private long totalSpotNum;

    /** 本页行 */
    private List<SpotRosterRow> rows;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalSpotNum() {
        return totalSpotNum;
    }

    public void setTotalSpotNum(long totalSpotNum) {
        this.totalSpotNum = totalSpotNum;
    }

    public List<SpotRosterRow> getRows() {
        return rows;
    }

    public void setRows(List<SpotRosterRow> rows) {
        this.rows = rows;
    }
}
