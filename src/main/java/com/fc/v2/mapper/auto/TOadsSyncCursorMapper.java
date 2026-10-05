package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TOadsSyncCursor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 市里增量比对游标数据层（全库唯一一行，固定 id=0）
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TOadsSyncCursorMapper extends BaseMapper<TOadsSyncCursor> {

    /** 唯一游标那一行；同步处理里行级锁住，游标推进与冲突清单同一事务 */
    @Select("SELECT * FROM t_oads_sync_cursor WHERE id = 0 FOR UPDATE")
    TOadsSyncCursor selectSingletonForUpdate();

    /** 不加锁读法（页面回显当前游标用） */
    @Select("SELECT * FROM t_oads_sync_cursor WHERE id = 0")
    TOadsSyncCursor selectSingleton();
}
