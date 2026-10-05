package com.fc.v2.mapper.custom;

import com.fc.v2.model.auto.TOadsSpot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 名册查询专用数据层：可见范围、三种查询入口（全等/前缀）都在这条 SQL 里落。
 *
 * <p>辖段清单由唯一裁决处按岗位划归与类别过滤圈好后传入；本层只呈现库中实际存在的行，
 * 本层当季未建行时返回空，绝不拿邻段数据补齐。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TOadsRosterQueryMapper {

    /**
     * 名册基础数据（同一份，分页与三视图都从它取）。
     *
     * @param quarterCode       季度码
     * @param areaNos           划归可见（并已按类别过滤）的辖段代号；空表示一个辖段都没划归
     * @param includeUnattached 区局岗是否把尚未挂靠辖段的旧行一并纳入
     * @param field             查询入口 spotNo/areaNo/null（areaKind 已折进 areaNos）
     * @param value             查询值
     * @param prefix            是否前缀匹配
     */
    List<TOadsSpot> queryRoster(@Param("quarterCode") String quarterCode,
                                @Param("areaNos") List<String> areaNos,
                                @Param("includeUnattached") boolean includeUnattached,
                                @Param("field") String field,
                                @Param("value") String value,
                                @Param("prefix") boolean prefix);
}
