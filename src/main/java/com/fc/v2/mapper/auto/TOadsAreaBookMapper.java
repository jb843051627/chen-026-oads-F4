package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TOadsAreaBook;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 属地段册底册数据层
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface TOadsAreaBookMapper extends BaseMapper<TOadsAreaBook> {

    /** 按路段代号取目录行（名录依据检查点、全称以目录为准修正都走这一处） */
    @Select("SELECT * FROM t_oads_area_book WHERE area_no = #{areaNo} AND del_flag = 0 LIMIT 1")
    TOadsAreaBook findNormalByAreaNo(@Param("areaNo") String areaNo);

    /** 目录全部段（含已注销：注销前旧行仍列出、仍计在册处数；可见划归也按它圈） */
    @Select("SELECT * FROM t_oads_area_book WHERE del_flag = 0 ORDER BY id ASC")
    List<TOadsAreaBook> listAllNormal();
}
