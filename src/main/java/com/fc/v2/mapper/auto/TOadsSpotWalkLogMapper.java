package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TOadsSpotWalkLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位三档推步留痕数据层（append-only，没有修改、没有物理删除）
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TOadsSpotWalkLogMapper extends BaseMapper<TOadsSpotWalkLog> {

    /** 这一行的全部推步留痕，按动作时刻、主键排（退回重走的旧痕与新笔分开，各自可查） */
    @Select("SELECT * FROM t_oads_spot_walk_log WHERE spot_id = #{spotId} ORDER BY act_time ASC, id ASC")
    List<TOadsSpotWalkLog> listBySpot(@Param("spotId") Long spotId);
}
