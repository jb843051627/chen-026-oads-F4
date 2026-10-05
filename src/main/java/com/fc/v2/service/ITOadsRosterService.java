package com.fc.v2.service;

import com.fc.v2.model.auto.TOadsSpot;
import com.fc.v2.model.auto.TOadsSpotWalkLog;
import com.fc.v2.model.custom.roster.OperatorScope;
import com.fc.v2.model.custom.roster.RosterPage;
import com.fc.v2.model.custom.roster.RosterQuery;
import com.fc.v2.model.custom.roster.RosterViews;
import com.fc.v2.model.custom.roster.SpotBuildForm;
import com.fc.v2.model.custom.roster.SpotRosterRow;
import com.fc.v2.model.custom.roster.SyncResult;

import java.util.List;

/**
 * 点位名册建档唯一裁决处——五个检查点、数量轧差、辖段视图取行、页首与回执计数，
 * 答案只从这一处给出；旁处不保留第二条能绕开它的判定路径。
 *
 * <p>建档岗逐条建立（{@link #buildManual}）与集中更换核验完成后带出（{@link #buildFromBatch}）
 * 是仅有的两条建行通道，复用同一套检查点、数量逻辑与界面样式。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface ITOadsRosterService {

    /** 通道一：建档岗逐条建立 */
    SpotRosterRow buildManual(SpotBuildForm form, String operator);

    /** 通道二：集中更换业务核验完成后带出（同一套检查点，不另长样式） */
    SpotRosterRow buildFromBatch(SpotBuildForm form, String operator);

    /** 更正已核验落地处数：写库前在唯一裁决处重轧待补处数（30/12→18，20/7→13，相等→0） */
    SpotRosterRow correctGotNum(Long id, Integer gotNum, String operator);

    /** 改挂别的路段目录：重过围栏与名录依据，写库前重轧待补处数；坐标不得由前端改写 */
    SpotRosterRow rehang(Long id, String newAreaNo, String operator);

    /** 往上推一步：待核→已核→压卡，不许多跨；档由系统按留痕数，屏填不算 */
    SpotRosterRow advance(Long id, String operator);

    /** 已核退回待核一步重来：上一回留痕原样保留，与重走那笔分开存放 */
    SpotRosterRow returnToWait(Long id, String operator);

    /** 就地停住：行仍在册可列可计，但让出同点位在走名额（与走到压卡一样腾出名额） */
    SpotRosterRow halt(Long id, String operator);

    /** 名册分页：页内行、页首处数、回执处数同一份基础数据；条数取区局配置 */
    RosterPage pageRoster(String quarterCode, RosterQuery query, OperatorScope scope,
                          int pageNum, int configuredPageSize);

    /** 三种视图（辖段类别/路段代号/建档情形）一次给齐，处数必须一致 */
    RosterViews views(String quarterCode, OperatorScope scope);

    /** 一行详情：库中同一行加推步留痕（含退回保留的旧痕） */
    SpotRosterRow detail(Long id);

    /** 这一行的推步留痕，按动作时刻排 */
    List<TOadsSpotWalkLog> walkHistory(Long id);

    /** 每日一次市里增量比对：游标之后才参与，冲突逐条输出不静默覆盖，游标同一事务推进 */
    SyncResult syncWithCity(String operator);
}
