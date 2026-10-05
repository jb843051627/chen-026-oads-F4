package com.fc.v2.model.custom.roster;

import java.math.BigDecimal;

/**
 * 建档表单（两条通道同一张单）：建档岗逐条建立与集中更换带出，收的字段、过的检查点完全一样。
 *
 * <p>数量两个录入字段用字符串收，好把“不是整数”挡在数量检查点并点名字段；
 * 待补处数没有录入口，报文里即便带来也不采信。坐标与辖段挂靠来自现场采集。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class SpotBuildForm {

    /** 主键：带着它且对上本季度同码那一行时按更新处理，不当成多出来的一行 */
    private Long id;

    /** 点位码（报送材料自带，系统不另发第二串，不校对构成规则） */
    private String spotNo;

    /** 季度码，如 2026Q4 */
    private String quarterCode;

    /** 路段代号（挂路段目录） */
    private String areaNo;

    /** 现场采集经度 */
    private BigDecimal lng;

    /** 现场采集纬度 */
    private BigDecimal lat;

    /** 设置主体代号或承办岗代号（报送方：各路段经营主体） */
    private String setNo;

    /** 核定应设处数（录入口，字符串收：非整好点名） */
    private String needNum;

    /** 已核验落地处数（录入口） */
    private String gotNum;

    /** 随件交来的牌面题名与要件 */
    private String content;

    /** 集中更换批次码（仅集中更换通道带） */
    private String batchNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpotNo() {
        return spotNo;
    }

    public void setSpotNo(String spotNo) {
        this.spotNo = spotNo;
    }

    public String getQuarterCode() {
        return quarterCode;
    }

    public void setQuarterCode(String quarterCode) {
        this.quarterCode = quarterCode;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNo(String areaNo) {
        this.areaNo = areaNo;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public String getSetNo() {
        return setNo;
    }

    public void setSetNo(String setNo) {
        this.setNo = setNo;
    }

    public String getNeedNum() {
        return needNum;
    }

    public void setNeedNum(String needNum) {
        this.needNum = needNum;
    }

    public String getGotNum() {
        return gotNum;
    }

    public void setGotNum(String gotNum) {
        this.gotNum = gotNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
}
