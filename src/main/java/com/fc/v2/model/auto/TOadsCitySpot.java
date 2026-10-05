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
 * 市里侧名册行对象 t_oads_city_spot（增量比对对侧底影）。
 *
 * <p>本局服务只读本侧，不回写市里数据；行版本号单调递增，供游标比较。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_oads_city_spot")
@ApiModel(value = "TOadsCitySpot", description = "市里侧名册行")
public class TOadsCitySpot implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 点位码 */
    @TableField("spot_no")
    @ApiModelProperty(value = "点位码")
    private String spotNo;

    /** 季度码 */
    @TableField("quarter_code")
    @ApiModelProperty(value = "季度码")
    private String quarterCode;

    /** 市里记的路段全称（与本局冲突逐条出清单，不静默覆盖） */
    @TableField("city_area_name")
    @ApiModelProperty(value = "市里记的路段全称")
    private String cityAreaName;

    /** 市里情形 0在册 1已注销 */
    @TableField("city_status")
    @ApiModelProperty(value = "市里情形 0在册 1已注销")
    private Integer cityStatus;

    /** 市里侧行版本号（单调递增，供游标比较） */
    @TableField("row_version")
    @ApiModelProperty(value = "市里侧行版本号")
    private Long rowVersion;

    /** 市里侧该行最近变更时刻 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "市里侧该行最近变更时刻")
    private Date updateTime;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

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

    public String getCityAreaName() {
        return cityAreaName;
    }

    public void setCityAreaName(String cityAreaName) {
        this.cityAreaName = cityAreaName;
    }

    public Integer getCityStatus() {
        return cityStatus;
    }

    public void setCityStatus(Integer cityStatus) {
        this.cityStatus = cityStatus;
    }

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
