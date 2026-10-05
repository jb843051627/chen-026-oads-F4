package com.fc.v2.model.auto;

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
 * 市里增量比对游标对象 t_oads_sync_cursor（全库只容一行，固定 id=0）。
 *
 * <p>游标之前的行不复核，游标之后的行才参与本次比较；游标随本次同步一并推进，
 * 且与本次冲突清单出自同一份计算、同一事务，别处不得自行调整。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_oads_sync_cursor")
@ApiModel(value = "TOadsSyncCursor", description = "市里增量比对游标（全库唯一）")
public class TOadsSyncCursor implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键（全库只容一行，固定 0） */
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键（全库只容一行，固定 0）")
    private Long id;

    /** 已比对到的市里侧行版本（之前不复核） */
    @TableField("last_version")
    @ApiModelProperty(value = "已比对到的市里侧行版本")
    private Long lastVersion;

    /** 上次同步时刻 */
    @TableField("ran_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "上次同步时刻")
    private Date ranAt;

    /** 上次同步交代（比对行数/冲突条数） */
    @TableField("run_remark")
    @ApiModelProperty(value = "上次同步交代")
    private String runRemark;

    /** 更新者 */
    @TableField("update_by")
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(Long lastVersion) {
        this.lastVersion = lastVersion;
    }

    public Date getRanAt() {
        return ranAt;
    }

    public void setRanAt(Date ranAt) {
        this.ranAt = ranAt;
    }

    public String getRunRemark() {
        return runRemark;
    }

    public void setRunRemark(String runRemark) {
        this.runRemark = runRemark;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
