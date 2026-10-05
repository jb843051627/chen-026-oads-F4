package com.fc.v2.model.custom.roster;

import java.util.List;

/**
 * 三种视图一次给齐：按辖段类别、按路段代号、按建档情形。
 *
 * <p>同一季度同一批行，三种视图给出的处数必须一致——三组分组与总计数出自同一份基础数据
 * （库中实际存在的行只捞一次，分组在这一份上数），页面不再数第二遍。本层当季未建行时
 * 各分组留空，不许拿邻段数据补齐。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class RosterViews {

    /** 按辖段类别（主干道、次干道、背街巷，各自排序） */
    private List<RosterGroup> byAreaKind;

    /** 按路段代号 */
    private List<RosterGroup> byAreaNo;

    /** 按建档情形（建档岗逐条／集中更换／纸簿迁入） */
    private List<RosterGroup> byBuildChannel;

    /** 总行数（三视图一致） */
    private long totalRowCount;

    /** 在册处数合计（三视图一致，核定应设处数合计） */
    private long totalSpotNum;

    public List<RosterGroup> getByAreaKind() {
        return byAreaKind;
    }

    public void setByAreaKind(List<RosterGroup> byAreaKind) {
        this.byAreaKind = byAreaKind;
    }

    public List<RosterGroup> getByAreaNo() {
        return byAreaNo;
    }

    public void setByAreaNo(List<RosterGroup> byAreaNo) {
        this.byAreaNo = byAreaNo;
    }

    public List<RosterGroup> getByBuildChannel() {
        return byBuildChannel;
    }

    public void setByBuildChannel(List<RosterGroup> byBuildChannel) {
        this.byBuildChannel = byBuildChannel;
    }

    public long getTotalRowCount() {
        return totalRowCount;
    }

    public void setTotalRowCount(long totalRowCount) {
        this.totalRowCount = totalRowCount;
    }

    public long getTotalSpotNum() {
        return totalSpotNum;
    }

    public void setTotalSpotNum(long totalSpotNum) {
        this.totalSpotNum = totalSpotNum;
    }
}
