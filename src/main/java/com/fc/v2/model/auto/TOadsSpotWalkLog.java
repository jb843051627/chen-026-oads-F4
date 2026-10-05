package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 点位三档推步留痕对象 t_oads_spot_walk_log（append-only）
 *
 * <p>一次推进/退回一行，只许追加。当前停在哪一步只按本表逐笔重数，
 * 屏上填的档、经办人记的档都不作数。退回把上一回留痕原样保留，另起一行记退回。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_oads_spot_walk_log")
@ApiModel(value = "TOadsSpotWalkLog", description = "点位三档推步留痕")
public class TOadsSpotWalkLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 点位底册行 */
    @TableField("spot_id")
    @ApiModelProperty(value = "点位底册行")
    private Long spotId;

    /** 点位码（当时冗余） */
    @TableField("spot_no")
    @ApiModelProperty(value = "点位码（当时冗余）")
    private String spotNo;

    /** 动作 ADVANCE推进一步 RETURN已核退回待核 */
    @TableField("action")
    @ApiModelProperty(value = "动作 ADVANCE推进一步 RETURN已核退回待核")
    private String action;

    /** 动前步数 0待核 1已核 2压卡 */
    @TableField("from_step")
    @ApiModelProperty(value = "动前步数 0待核 1已核 2压卡")
    private Integer fromStep;

    /** 动后步数 */
    @TableField("to_step")
    @ApiModelProperty(value = "动后步数")
    private Integer toStep;

    /** 经办人（记账用，档不由他点） */
    @TableField("operate_by")
    @ApiModelProperty(value = "经办人（记账用，档不由他点）")
    private String operateBy;

    /** 动作时刻 */
    @TableField("act_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "动作时刻")
    private Date actTime;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public String getSpotNo() {
        return spotNo;
    }

    public void setSpotNo(String spotNo) {
        this.spotNo = spotNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getFromStep() {
        return fromStep;
    }

    public void setFromStep(Integer fromStep) {
        this.fromStep = fromStep;
    }

    public Integer getToStep() {
        return toStep;
    }

    public void setToStep(Integer toStep) {
        this.toStep = toStep;
    }

    public String getOperateBy() {
        return operateBy;
    }

    public void setOperateBy(String operateBy) {
        this.operateBy = operateBy;
    }

    public Date getActTime() {
        return actTime;
    }

    public void setActTime(Date actTime) {
        this.actTime = actTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
