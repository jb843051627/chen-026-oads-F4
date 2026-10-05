package com.fc.v2.model.custom.roster;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fc.v2.model.auto.TOadsSpot;

import java.io.Serializable;
import java.util.Date;

/**
 * 名册上的一行：库中点位底册那一行，加目录点出的辖段类别、目录全称。
 *
 * <p>页面与库两边看到的是同一行——本行就是底册行的回显，不另存第二份；
 * 路段全称以目录为准（行内冗余与目录有出入时，建档/改挂那一次已按目录修正）。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class SpotRosterRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 底册原行（页面与库同一行） */
    private TOadsSpot spot;

    /** 目录点出的辖段类别（主干道/次干道/背街巷） */
    private String areaKind;

    /** 目录点出的辖段全称（以此为准） */
    private String areaNameCanonical;

    /** 当前档标签（服务端按留痕数出：待核/已核/压卡） */
    private String stepLabel;

    /** 是否在走（未压卡且未就地停住；在走则占着同点位名额） */
    private boolean walking;

    public SpotRosterRow() {
    }

    public SpotRosterRow(TOadsSpot spot, String areaKind, String areaNameCanonical,
                         String stepLabel, boolean walking) {
        this.spot = spot;
        this.areaKind = areaKind;
        this.areaNameCanonical = areaNameCanonical;
        this.stepLabel = stepLabel;
        this.walking = walking;
    }

    public TOadsSpot getSpot() {
        return spot;
    }

    public void setSpot(TOadsSpot spot) {
        this.spot = spot;
    }

    public String getAreaKind() {
        return areaKind;
    }

    public void setAreaKind(String areaKind) {
        this.areaKind = areaKind;
    }

    public String getAreaNameCanonical() {
        return areaNameCanonical;
    }

    public void setAreaNameCanonical(String areaNameCanonical) {
        this.areaNameCanonical = areaNameCanonical;
    }

    public String getStepLabel() {
        return stepLabel;
    }

    public void setStepLabel(String stepLabel) {
        this.stepLabel = stepLabel;
    }

    public boolean isWalking() {
        return walking;
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getCreateTime() {
        return spot == null ? null : spot.getCreateTime();
    }
}
