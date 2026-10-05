package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TOadsCitySpot;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 市里侧名册行数据层（本局只读，不回写市里数据）
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TOadsCitySpotMapper extends BaseMapper<TOadsCitySpot> {

    /**
     * 游标之后的市里侧行才参与本次比较：游标之前（含游标本身）的行不复核。
     * 按行版本升序，本次比对停在哪一版，游标就推进到哪一版——同一份计算。
     */
    @Select("SELECT * FROM t_oads_city_spot WHERE del_flag = 0 AND row_version > #{lastVersion} "
            + "ORDER BY row_version ASC")
    List<TOadsCitySpot> listChangedAfter(@Param("lastVersion") long lastVersion);

    /** 按点位码+季度取市里侧当前行（冲突核对用） */
    @Select("SELECT * FROM t_oads_city_spot WHERE spot_no = #{spotNo} AND quarter_code = #{quarterCode} "
            + "AND del_flag = 0 LIMIT 1")
    TOadsCitySpot selectBySpotQuarter(@Param("spotNo") String spotNo,
                                      @Param("quarterCode") String quarterCode);
}
