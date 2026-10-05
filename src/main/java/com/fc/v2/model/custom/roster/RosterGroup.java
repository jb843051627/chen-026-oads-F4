package com.fc.v2.model.custom.roster;

/**
 * 视图里的一组：分组键、行数、处数（核定应设处数合计）。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class RosterGroup {

    /** 分组键（类别名／路段代号／建档情形标签） */
    private String key;

    /** 本组行数 */
    private long rowCount;

    /** 本组在册处数（核定应设处数合计） */
    private long spotNum;

    public RosterGroup() {
    }

    public RosterGroup(String key, long rowCount, long spotNum) {
        this.key = key;
        this.rowCount = rowCount;
        this.spotNum = spotNum;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getSpotNum() {
        return spotNum;
    }

    public void setSpotNum(long spotNum) {
        this.spotNum = spotNum;
    }
}
