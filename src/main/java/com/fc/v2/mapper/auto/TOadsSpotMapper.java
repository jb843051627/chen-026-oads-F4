package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TOadsSpot;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位底册数据层
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface TOadsSpotMapper extends BaseMapper<TOadsSpot> {

    /**
     * 推步/退回/停住处理专用：行级锁住点位行。
     * 同一处点位并发两笔推步时后一笔等前一笔提交后再读，步数只按留痕逐笔串行数。
     */
    @Select("SELECT * FROM t_oads_spot WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    TOadsSpot selectByIdForUpdate(@Param("id") Long id);

    /** 同一点位码跨季度的全部正常行（身份检查点判在走名额用） */
    @Select("SELECT * FROM t_oads_spot WHERE spot_no = #{spotNo} AND del_flag = 0")
    List<TOadsSpot> listNormalBySpotNo(@Param("spotNo") String spotNo);

    /** 某点位码+季度的当前正常行（市里增量冲突核对用） */
    @Select("SELECT * FROM t_oads_spot WHERE spot_no = #{spotNo} AND quarter_code = #{quarterCode} "
            + "AND del_flag = 0 ORDER BY create_time DESC, id DESC LIMIT 1")
    TOadsSpot findNormalBySpotQuarter(@Param("spotNo") String spotNo,
                                      @Param("quarterCode") String quarterCode);
}
