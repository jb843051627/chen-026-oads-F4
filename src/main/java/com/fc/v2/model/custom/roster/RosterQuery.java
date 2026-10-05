package com.fc.v2.model.custom.roster;

/**
 * 名册查询条件：三种入口（点位编码／路段代号／路段类别），每种都支持全等与前缀匹配。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class RosterQuery {

    /** 匹配字段：spotNo 点位编码 / areaNo 路段代号 / areaKind 路段类别 */
    private String field;

    /** 查询值 */
    private String value;

    /** 匹配方式：eq 全等（默认） / prefix 前缀匹配 */
    private String match;

    /** 季度码（名册按季度成册，必填） */
    private String quarterCode;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getQuarterCode() {
        return quarterCode;
    }

    public void setQuarterCode(String quarterCode) {
        this.quarterCode = quarterCode;
    }

    public boolean prefix() {
        return "prefix".equalsIgnoreCase(match);
    }
}
