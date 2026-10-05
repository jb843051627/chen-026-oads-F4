package com.fc.v2.service.impl;

import com.fc.v2.common.exception.roster.RosterRuleException;
import com.fc.v2.mapper.auto.TOadsAreaBookMapper;
import com.fc.v2.mapper.auto.TOadsCitySpotMapper;
import com.fc.v2.mapper.auto.TOadsSpotMapper;
import com.fc.v2.mapper.auto.TOadsSpotWalkLogMapper;
import com.fc.v2.mapper.auto.TOadsSyncCursorMapper;
import com.fc.v2.mapper.custom.TOadsRosterQueryMapper;
import com.fc.v2.model.auto.TOadsAreaBook;
import com.fc.v2.model.auto.TOadsCitySpot;
import com.fc.v2.model.auto.TOadsSpot;
import com.fc.v2.model.auto.TOadsSpotWalkLog;
import com.fc.v2.model.auto.TOadsSyncCursor;
import com.fc.v2.model.custom.roster.OperatorScope;
import com.fc.v2.model.custom.roster.RosterGroup;
import com.fc.v2.model.custom.roster.RosterPage;
import com.fc.v2.model.custom.roster.RosterQuery;
import com.fc.v2.model.custom.roster.RosterViews;
import com.fc.v2.model.custom.roster.SpotBuildForm;
import com.fc.v2.model.custom.roster.SpotRosterRow;
import com.fc.v2.model.custom.roster.SyncResult;
import com.fc.v2.service.ITOadsRosterService;
import com.fc.v2.service.roster.AreaFenceRule;
import com.fc.v2.service.roster.BuildChannel;
import com.fc.v2.service.roster.SpotQuantityRule;
import com.fc.v2.service.roster.WalkStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 点位名册建档唯一裁决处。
 *
 * <p>一行能否落库、待补处数算出多少、辖段视图里应出现哪几行、页首与回执那几个数从哪里取，
 * 四样只认本处给出的答案：五道检查点按序裁决（前一个没过不进下一个，各点各的说法），
 * 数量只在 {@link SpotQuantityRule} 一处轧，视图与页首同一份基础数据只捞一次，
 * 增量游标只在本处的同步事务里推进。旁处不留第二条判定路径。
 *
 * <p>建档岗逐条与集中更换带出两条通道复用本处同一套逻辑；旧的点位底册通用入口
 * （TOadsSpotService）收什么回什么维持原状，本处是另写的等价建档去处，不动它本体。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Service
public class TOadsRosterServiceImpl implements ITOadsRosterService {

    /** 辖段类别固定排序 */
    private static final List<String> AREA_KIND_ORDER = Arrays.asList("主干道", "次干道", "背街巷");
    /** 建档情形固定排序：建档岗逐条／集中更换／纸簿迁入 */
    private static final List<String> CHANNEL_ORDER = Arrays.asList(
            BuildChannel.MANUAL, BuildChannel.BATCH, BuildChannel.LEGACY);
    private static final String KIND_UNATTACHED = "未挂靠";
    private static final long CURSOR_ID = 0L;

    @Resource
    private TOadsSpotMapper spotMapper;
    @Resource
    private TOadsAreaBookMapper areaMapper;
    @Resource
    private TOadsSpotWalkLogMapper walkLogMapper;
    @Resource
    private TOadsCitySpotMapper citySpotMapper;
    @Resource
    private TOadsSyncCursorMapper syncCursorMapper;
    @Resource
    private TOadsRosterQueryMapper rosterQueryMapper;

    // ================================================================ 两条建行通道

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow buildManual(SpotBuildForm form, String operator) {
        return buildInternal(form, BuildChannel.MANUAL, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow buildFromBatch(SpotBuildForm form, String operator) {
        // 集中更换业务核验完成后带出：与逐条建行同一套检查点、同一套数量、同一张单
        if (form == null || isBlank(form.getBatchNo())) {
            throw new RosterRuleException("集中更换带出的行须带集中更换批次码，缺批次码本行进不了库");
        }
        return buildInternal(form, BuildChannel.BATCH, operator);
    }

    /**
     * 五个检查点按固定先后：身份 → 空间挂靠 → 数量 → 名录依据 →（入库后的同步与展示归查询/同步口）。
     * 前一个没过不进下一个；每个检查点失败各给各的提示，绝不合并成一句“数据有误”。
     */
    private SpotRosterRow buildInternal(SpotBuildForm form, String channel, String operator) {
        if (form == null) {
            throw new RosterRuleException("建档单是空的，本行进不了库");
        }
        requireOperator(operator);
        boolean isUpdate = form.getId() != null;

        // -------- 检查点1：身份 --------
        String spotNo = trim(form.getSpotNo());
        String quarterCode = trim(form.getQuarterCode());
        if (isBlank(spotNo)) {
            throw new RosterRuleException("检查点1·身份：点位编码缺栏。编码由报送材料自带，系统不另发第二串，本行进不了库");
        }
        if (isBlank(quarterCode)) {
            throw new RosterRuleException("检查点1·身份：季度码缺栏，无法按“本季度同一编码”认行，本行进不了库");
        }

        TOadsSpot self = null;
        if (isUpdate) {
            self = spotMapper.selectById(form.getId());
            if (self == null || (self.getDelFlag() != null && self.getDelFlag() != 0)) {
                throw new RosterRuleException("检查点1·身份：主键[" + form.getId() + "]在库里查无此行，无从更新");
            }
            assertMutable(self);
            if (!spotNo.equals(self.getSpotNo())) {
                throw new RosterRuleException("检查点1·身份：点位编码是报送材料自带的那一串，更新不许改码（报来"
                        + spotNo + "，原行" + self.getSpotNo() + "）");
            }
            // 更新行的季度以原行为准（同一行不跨季挪）
            quarterCode = self.getQuarterCode() == null ? quarterCode : self.getQuarterCode();
        }

        // 同一处点位，同时只容一行在走的册：本季度同一编码已有“仍然有效（在走）”的行则拒绝；
        // 走到压卡、或就地停住后名额空出，同点位再立新行才接得上。例外只一种：对象正是那一行本身，按更新。
        List<TOadsSpot> sameCode = spotMapper.listNormalBySpotNo(spotNo);
        for (TOadsSpot row : sameCode) {
            if (isUpdate && row.getId().equals(self.getId())) {
                continue;
            }
            if (isWalking(row)) {
                throw new RosterRuleException("检查点1·身份：同一处点位同时只容一行在走的册——点位编码[" + spotNo
                        + "]库里已有仍然有效的在走行（季度" + row.getQuarterCode() + "，id=" + row.getId()
                        + "），本次建立被拒绝；要改的就是那一行时带它的主键按更新办理。头一行没走到压卡、"
                        + "也未就地停住之前，第二行立不起来（两家维护单位同日各报、换人重报都不许一点位两行叠着）");
            }
        }

        // -------- 检查点2：空间挂靠 --------
        // 坐标与辖段挂靠来自现场采集，前端不得人工改写：更新时一律沿用原坐标，报来的坐标不采信
        BigDecimal lng;
        BigDecimal lat;
        if (isUpdate) {
            lng = self.getLng();
            lat = self.getLat();
        } else {
            lng = form.getLng();
            lat = form.getLat();
        }
        String areaNo = trim(form.getAreaNo());
        if (isBlank(areaNo) && isUpdate) {
            areaNo = self.getAreaNo();
        }
        // 坐标缺栏是检查点2自己的说法；代号在目录里查不到则留给检查点4说，不抢先
        if (lng == null || lat == null) {
            throw new RosterRuleException("检查点2·空间挂靠：该点位现场坐标（经度、纬度）缺栏，"
                    + "坐标来自现场采集不得人工补填，本行进不了库");
        }
        TOadsAreaBook areaForFence = isBlank(areaNo) ? null : findAreaByNo(areaNo);
        // 辖段取得到才落围栏；取不到（代号缺/目录无此段）交检查点4
        if (areaForFence != null) {
            AreaFenceRule.requireInside(lng, lat, areaForFence);
        }

        // -------- 检查点3：数量（待补处数无录入口，写库前在唯一一处重轧） --------
        int needNum = parseIntField(form.getNeedNum(), "核定应设处数", isUpdate ? self.getNeedNum() : null);
        int gotNum = parseGotField(form.getGotNum(), isUpdate ? self.getGotNum() : null);
        SpotQuantityRule.Quantity qty = SpotQuantityRule.settle(needNum, gotNum);

        // -------- 检查点4：名录依据 --------
        TOadsAreaBook area;
        if (isBlank(areaNo)) {
            throw new RosterRuleException("检查点4·名录依据：路段代号缺栏，路段目录里找不到这一段，建行失败");
        }
        if (areaForFence == null) {
            throw new RosterRuleException("检查点4·名录依据：路段代号[" + areaNo
                    + "]在路段目录里找不到，建行失败");
        }
        area = areaForFence;
        boolean stayingOnDeregistered = isUpdate && areaNo.equals(self.getAreaNo());
        if (area.getStatus() != null && area.getStatus() == 1 && !stayingOnDeregistered) {
            throw new RosterRuleException("检查点4·名录依据：路段[" + areaNo + "]已注销"
                    + "（整段划归邻段或改隶别的辖段），该段不再接受新增行；注销前已存在的行仍列出、仍计在册处数");
        }

        // -------- 五道过齐，落库（页面与库看到的是同一行） --------
        Date now = new Date();
        TOadsSpot row = isUpdate ? self : new TOadsSpot();
        row.setSpotNo(spotNo);
        row.setQuarterCode(quarterCode);
        row.setAreaId(area.getId() == null ? null : area.getId().intValue());
        row.setAreaNo(area.getAreaNo());
        // 行上冗余全称与目录有出入以目录为准修正，两边并存视为缺陷
        row.setAreaName(area.getAreaName());
        row.setLng(lng);
        row.setLat(lat);
        // 待补处数每次写库前重算并覆盖上一次落库值（30/12→18、20/7→13、相等→0）
        row.setNeedNum(qty.getNeedNum());
        row.setGotNum(qty.getGotNum());
        row.setLackNum(qty.getLackNum());
        if (!isBlank(form.getSetNo())) {
            row.setSetNo(form.getSetNo());
        }
        if (form.getContent() != null) {
            row.setContent(form.getContent());
        }
        row.setDelFlag(0);
        if (isUpdate) {
            // 更新/改数：册面情形随轧差结果，待补 0 记已核齐，否则记缺项
            row.setStatus(qty.getLackNum() == 0 ? 1 : 2);
            row.setUpdateBy(operator);
            row.setUpdateTime(now);
            spotMapper.updateById(row);
        } else {
            // 两条通道同一套落库规矩；挂牌档/挂设档/安全分档沿用底册既有口径，建档不碰它们
            row.setSetState(0);
            row.setSetVer(0);
            row.setSafeLevel(0);
            row.setSafeRuleCode(null);
            row.setSafeEffStart(null);
            row.setWalkStep(WalkStep.WAIT.getStep());
            row.setHalted(0);
            row.setBuildChannel(channel);
            // 新入册一律先记“新入册”，档由三档推步走，不因数齐就当已核齐
            row.setStatus(0);
            row.setCreateBy(operator);
            row.setCreateTime(now);
            spotMapper.insert(row);
        }
        return toRow(row, area);
    }

    // ================================================================ 数量三时刻：更正 / 改挂

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow correctGotNum(Long id, Integer gotNum, String operator) {
        requireOperator(operator);
        TOadsSpot row = requireMutableSpot(id);
        // 已核验处数被更正：写库前在唯一裁决处重轧待补，覆盖旧值
        SpotQuantityRule.Quantity qty = SpotQuantityRule.settle(row.getNeedNum(), gotNum);
        row.setGotNum(qty.getGotNum());
        row.setLackNum(qty.getLackNum());
        row.setStatus(qty.getLackNum() == 0 ? 1 : 2);
        row.setUpdateBy(operator);
        row.setUpdateTime(new Date());
        spotMapper.updateById(row);
        return toRow(row, findAreaByNo(row.getAreaNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow rehang(Long id, String newAreaNo, String operator) {
        requireOperator(operator);
        TOadsSpot row = requireMutableSpot(id);
        String targetNo = trim(newAreaNo);
        if (isBlank(targetNo)) {
            throw new RosterRuleException("检查点4·名录依据：改挂须点出目的路段代号，缺栏改挂不了");
        }
        if (targetNo.equals(row.getAreaNo())) {
            throw new RosterRuleException("改挂的目的路段[" + targetNo + "]与现挂靠同一段，无从改挂");
        }
        TOadsAreaBook target = findAreaByNo(targetNo);
        if (target == null) {
            throw new RosterRuleException("检查点4·名录依据：路段代号[" + targetNo + "]在路段目录里找不到，改挂失败");
        }
        if (target.getStatus() != null && target.getStatus() == 1) {
            throw new RosterRuleException("检查点4·名录依据：路段[" + targetNo
                    + "]已注销，不再接受新增行（含改挂进来）");
        }
        // 坐标现场采集不得人工改写：沿用原坐标对新围栏重过检查点2
        AreaFenceRule.requireInside(row.getLng(), row.getLat(), target);

        // 改挂到别的路段目录：数量第三个重算时刻
        SpotQuantityRule.Quantity qty = SpotQuantityRule.settle(row.getNeedNum(), row.getGotNum());
        row.setAreaId(target.getId() == null ? null : target.getId().intValue());
        row.setAreaNo(target.getAreaNo());
        row.setAreaName(target.getAreaName());
        row.setLackNum(qty.getLackNum());
        row.setUpdateBy(operator);
        row.setUpdateTime(new Date());
        spotMapper.updateById(row);
        return toRow(row, target);
    }

    // ================================================================ 三档推步

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow advance(Long id, String operator) {
        requireOperator(operator);
        TOadsSpot row = spotMapper.selectByIdForUpdate(id);
        requireExisting(row);
        assertNotSealed(row);
        if (isHalted(row)) {
            throw new RosterRuleException("此行已就地停住，不再往上推；要继续只能另开一行");
        }
        // 当前停在哪一步只按 append-only 留痕逐笔重数，行上步数只是这一处写回的回显
        int from = recountStep(row);
        if (from >= WalkStep.SEALED.getStep()) {
            throw new RosterRuleException("此行已压卡到头，加不进去、字改不动、也撤不下来");
        }
        int to = from + 1;
        Date now = new Date();
        appendWalkLog(row, "ADVANCE", from, to, operator, now);
        row.setWalkStep(to);
        if (to == WalkStep.SEALED.getStep()) {
            // 走到压卡：到头，压卡时刻落档；占过的名额此刻空出，同点位再立新行才接得上
            row.setSealedAt(now);
        }
        row.setUpdateBy(operator);
        row.setUpdateTime(now);
        spotMapper.updateById(row);
        return toRow(row, findAreaByNo(row.getAreaNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow returnToWait(Long id, String operator) {
        requireOperator(operator);
        TOadsSpot row = spotMapper.selectByIdForUpdate(id);
        requireExisting(row);
        assertNotSealed(row);
        if (isHalted(row)) {
            throw new RosterRuleException("此行已就地停住，没有可退回的档");
        }
        int from = recountStep(row);
        if (from == WalkStep.WAIT.getStep()) {
            throw new RosterRuleException("此行还在待核，前面没有“已核”可退，步子不得跨着数");
        }
        if (from > WalkStep.CHECKED.getStep()) {
            throw new RosterRuleException("只许已核退回待核一步；压卡那一级退不回来");
        }
        Date now = new Date();
        // 退回另起一行留痕：上一回记下的原样保留，重走那笔与旧痕分开排，不覆盖不并排
        appendWalkLog(row, "RETURN", from, WalkStep.WAIT.getStep(), operator, now);
        row.setWalkStep(WalkStep.WAIT.getStep());
        row.setUpdateBy(operator);
        row.setUpdateTime(now);
        spotMapper.updateById(row);
        return toRow(row, findAreaByNo(row.getAreaNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotRosterRow halt(Long id, String operator) {
        requireOperator(operator);
        TOadsSpot row = spotMapper.selectByIdForUpdate(id);
        requireExisting(row);
        assertNotSealed(row);
        if (isHalted(row)) {
            throw new RosterRuleException("此行本就就地停住，不必再停一次");
        }
        // 就地停住：行仍在册、可列可计；同点位在走名额即时让出，新人可另立一行
        row.setHalted(1);
        row.setUpdateBy(operator);
        row.setUpdateTime(new Date());
        spotMapper.updateById(row);
        return toRow(row, findAreaByNo(row.getAreaNo()));
    }

    // ================================================================ 视图 / 分页 / 查询

    @Override
    public RosterPage pageRoster(String quarterCode, RosterQuery query, OperatorScope scope,
                                 int pageNum, int configuredPageSize) {
        List<TOadsSpot> base = fetchBase(quarterCode, query, scope);
        // 页首与页内行同一份基础数据；页首处数只从这一次捞取合计
        long totalSpotNum = sumSpotNum(base);
        int size = configuredPageSize > 0 ? configuredPageSize : 20;
        int page = pageNum < 1 ? 1 : pageNum;
        int fromIndex = Math.min((page - 1) * size, base.size());
        int toIndex = Math.min(fromIndex + size, base.size());
        Map<String, TOadsAreaBook> areaMap = loadAreaMap();

        RosterPage result = new RosterPage();
        result.setPageNum(page);
        result.setPageSize(size);
        result.setTotal(base.size());
        result.setTotalSpotNum(totalSpotNum);
        List<SpotRosterRow> rows = new ArrayList<SpotRosterRow>();
        for (TOadsSpot s : base.subList(fromIndex, toIndex)) {
            rows.add(toRow(s, areaMap.get(s.getAreaNo())));
        }
        result.setRows(rows);
        return result;
    }

    @Override
    public RosterViews views(String quarterCode, OperatorScope scope) {
        // 同一季度同一批行只捞一次，三种分组在同一份基础数据上数，处数必须一致
        List<TOadsSpot> base = fetchBase(quarterCode, null, scope);
        Map<String, TOadsAreaBook> areaMap = loadAreaMap();

        Map<String, long[]> byKind = new LinkedHashMap<String, long[]>();
        Map<String, long[]> byNo = new LinkedHashMap<String, long[]>();
        Map<String, long[]> byChannel = new LinkedHashMap<String, long[]>();
        long totalRows = 0;
        long totalSpotNum = 0;
        for (TOadsSpot s : base) {
            TOadsAreaBook area = s.getAreaNo() == null ? null : areaMap.get(s.getAreaNo());
            String kind = area == null ? KIND_UNATTACHED : area.getAreaKind();
            String areaNo = s.getAreaNo() == null ? "(无代号)" : s.getAreaNo();
            String channel = s.getBuildChannel() == null ? BuildChannel.LEGACY : s.getBuildChannel();
            int num = s.getNeedNum() == null ? 0 : s.getNeedNum();
            accumulate(byKind, kind, num);
            accumulate(byNo, areaNo, num);
            accumulate(byChannel, channel, num);
            totalRows++;
            totalSpotNum += num;
        }

        RosterViews views = new RosterViews();
        views.setByAreaKind(groupsOrdered(byKind, kindOrderComparator()));
        views.setByAreaNo(groupsOrdered(byNo, Comparator.naturalOrder()));
        views.setByBuildChannel(groupsOrdered(byChannel, channelOrderComparator()));
        views.setTotalRowCount(totalRows);
        views.setTotalSpotNum(totalSpotNum);
        return views;
    }

    @Override
    public SpotRosterRow detail(Long id) {
        if (id == null) {
            throw new RosterRuleException("须点出哪一行");
        }
        TOadsSpot row = spotMapper.selectById(id);
        requireExisting(row);
        return toRow(row, findAreaByNo(row.getAreaNo()));
    }

    @Override
    public List<TOadsSpotWalkLog> walkHistory(Long id) {
        if (id == null) {
            throw new RosterRuleException("须点出哪一行");
        }
        return walkLogMapper.listBySpot(id);
    }

    // ================================================================ 市里增量同步

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResult syncWithCity(String operator) {
        requireOperator(operator);
        // 游标全库唯一，行级锁住：本次比较与游标推进同一份计算、同一事务，别处不得自行调整
        TOadsSyncCursor cursor = syncCursorMapper.selectSingletonForUpdate();
        boolean newborn = false;
        if (cursor == null) {
            cursor = new TOadsSyncCursor();
            cursor.setId(CURSOR_ID);
            cursor.setLastVersion(0L);
            cursor.setRunRemark("首次同步前补建的游标：起点 0");
            newborn = true;
        }
        long fromVersion = cursor.getLastVersion() == null ? 0L : cursor.getLastVersion();

        // 游标之前的行不复核，游标之后的行才参与本次比较
        List<TOadsCitySpot> changed = citySpotMapper.listChangedAfter(fromVersion);
        SyncResult result = new SyncResult();
        result.setFromVersion(fromVersion);
        long toVersion = fromVersion;

        for (TOadsCitySpot city : changed) {
            long ver = city.getRowVersion() == null ? toVersion : city.getRowVersion();
            if (ver > toVersion) {
                toVersion = ver;
            }
            TOadsSpot local = spotMapper.findNormalBySpotQuarter(city.getSpotNo(), city.getQuarterCode());
            if (local == null) {
                // 本局没有的行不冲突：不是“仍挂载”，也轮不到市里替本局建行
                continue;
            }
            // 以本局为准：冲突只逐条出清单，不静默覆盖市里边、也不改本局挂载
            if (city.getCityStatus() != null && city.getCityStatus() == 1) {
                result.getConflicts().add(new SyncResult.Conflict("CITY_REVOKED",
                        city.getSpotNo(), city.getQuarterCode(), "本局仍挂载", "市里已注销",
                        "点位[" + city.getSpotNo() + "]市里已注销而本局仍挂载，以本局为准保留挂载，逐条列出不静默清除"));
                continue;
            }
            TOadsAreaBook area = findAreaByNo(local.getAreaNo());
            String localName = area != null ? area.getAreaName() : local.getAreaName();
            if (city.getCityAreaName() != null && !city.getCityAreaName().equals(localName)) {
                result.getConflicts().add(new SyncResult.Conflict("NAME_MISMATCH",
                        city.getSpotNo(), city.getQuarterCode(), localName, city.getCityAreaName(),
                        "点位[" + city.getSpotNo() + "]两边路段全称冲突：本局[" + localName + "]／市里["
                                + city.getCityAreaName() + "]，以本局目录全称[" + localName + "]为准，不覆盖"));
            }
        }

        result.setToVersion(toVersion);
        result.setComparedRows(changed.size());

        // 游标随本次同步一并推进，停在哪一版推进到哪一版（有冲突也推进：冲突已逐条留单，不再复核）
        Date now = new Date();
        cursor.setLastVersion(toVersion);
        cursor.setRanAt(now);
        cursor.setUpdateBy(operator);
        cursor.setUpdateTime(now);
        cursor.setRunRemark("本次比对游标之后" + changed.size() + "行，冲突" + result.getConflicts().size()
                + "条（全称冲突/市里注销本局仍挂载，均以本局为准出清单）；游标 " + fromVersion + " → " + toVersion);
        if (newborn) {
            syncCursorMapper.insert(cursor);
        } else {
            syncCursorMapper.updateById(cursor);
        }
        return result;
    }

    // ================================================================ 内部口径

    /**
     * 可见范围 + 查询条件的同一份基础数据，只捞库中实际存在的行（del_flag=0）。
     * 本段经办岗只见本段挂靠的行；区局岗三类路段都可见，但范围仍限于划归它的辖段
     * （以辖段目录 scope_posts 里有没有它这一岗为准，含已注销辖段——旧行仍可列出）。
     * 本层当季未建行时返回空表，绝不拿邻段数据补齐。
     */
    private List<TOadsSpot> fetchBase(String quarterCode, RosterQuery query, OperatorScope scope) {
        if (query != null && !isBlank(query.getField())
                && !"spotNo".equals(query.getField())
                && !"areaNo".equals(query.getField())
                && !"areaKind".equals(query.getField())) {
            throw new RosterRuleException("名册只认三种查询入口：点位编码、路段代号、路段类别；["
                    + query.getField() + "]不是受认入口");
        }

        List<TOadsAreaBook> catalog = areaMapper.listAllNormal();
        List<String> visible = visibleAreaNos(catalog, scope);

        // 类别入口：先在目录上把该类（全等/前缀）辖段代号圈出，再与可见划归取交
        String field = null;
        String value = null;
        boolean prefix = query != null && query.prefix();
        if (query != null && !isBlank(query.getField()) && !isBlank(query.getValue())) {
            field = query.getField().trim();
            value = query.getValue().trim();
            if ("areaKind".equals(field)) {
                visible = retainKind(visible, catalog, value, prefix);
                field = null;
                value = null;
            }
        }

        boolean includeUnattached = scope != null && scope.isDistrict();
        if (scope == null) {
            // 无登录岗位（如定时对账）：一条不看，而不是全看
            return new ArrayList<TOadsSpot>();
        }
        if (!scope.isDistrict() && visible.isEmpty()) {
            return new ArrayList<TOadsSpot>();
        }
        return rosterQueryMapper.queryRoster(isBlank(quarterCode) ? null : quarterCode.trim(),
                visible, includeUnattached, field, value, field == null ? false : prefix);
    }

    /** 划归当前岗位的辖段代号（已注销辖段仍计入——注销前旧行要能列出来） */
    private List<String> visibleAreaNos(List<TOadsAreaBook> catalog, OperatorScope scope) {
        List<String> nos = new ArrayList<String>();
        if (scope == null || isBlank(scope.getPost())) {
            return nos;
        }
        for (TOadsAreaBook area : catalog) {
            if (postListed(area.getScopePosts(), scope.getPost())) {
                nos.add(area.getAreaNo());
            }
        }
        return nos;
    }

    private List<String> retainKind(List<String> areaNos, List<TOadsAreaBook> catalog,
                                    String kind, boolean prefix) {
        List<String> kept = new ArrayList<String>();
        for (TOadsAreaBook area : catalog) {
            if (area.getAreaKind() == null || !areaNos.contains(area.getAreaNo())) {
                continue;
            }
            boolean hit = prefix ? area.getAreaKind().startsWith(kind) : area.getAreaKind().equals(kind);
            if (hit) {
                kept.add(area.getAreaNo());
            }
        }
        return kept;
    }

    private boolean postListed(String scopePosts, String post) {
        if (isBlank(scopePosts)) {
            return false;
        }
        for (String p : scopePosts.split(",")) {
            if (post.equals(p.trim())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, TOadsAreaBook> loadAreaMap() {
        Map<String, TOadsAreaBook> map = new LinkedHashMap<String, TOadsAreaBook>();
        for (TOadsAreaBook area : areaMapper.listAllNormal()) {
            map.put(area.getAreaNo(), area);
        }
        return map;
    }

    private TOadsAreaBook findAreaByNo(String areaNo) {
        if (isBlank(areaNo)) {
            return null;
        }
        return areaMapper.findNormalByAreaNo(areaNo);
    }

    private void appendWalkLog(TOadsSpot row, String action, int from, int to, String operator, Date at) {
        TOadsSpotWalkLog log = new TOadsSpotWalkLog();
        log.setSpotId(row.getId());
        log.setSpotNo(row.getSpotNo());
        log.setAction(action);
        log.setFromStep(from);
        log.setToStep(to);
        log.setOperateBy(operator);
        log.setActTime(at);
        log.setCreateBy(operator);
        walkLogMapper.insert(log);
    }

    private SpotRosterRow toRow(TOadsSpot s, TOadsAreaBook area) {
        String kind = area == null ? KIND_UNATTACHED : area.getAreaKind();
        String canonical = area == null ? s.getAreaName() : area.getAreaName();
        // 展示读服务端写回的步数（动作发生时由留痕重数后落库的那一值）；旧行空着按待核
        int step = s.getWalkStep() == null ? WalkStep.WAIT.getStep() : s.getWalkStep();
        boolean walking = step < WalkStep.SEALED.getStep() && !isHalted(s);
        return new SpotRosterRow(s, kind, canonical, WalkStep.ofStep(step).getLabel(), walking);
    }

    private TOadsSpot requireMutableSpot(Long id) {
        if (id == null) {
            throw new RosterRuleException("须点出哪一行");
        }
        TOadsSpot row = spotMapper.selectById(id);
        requireExisting(row);
        assertMutable(row);
        return row;
    }

    private void requireExisting(TOadsSpot row) {
        if (row == null || (row.getDelFlag() != null && row.getDelFlag() != 0)) {
            throw new RosterRuleException("点位底册上查无此行（或已删除）");
        }
    }

    private void assertMutable(TOadsSpot row) {
        assertNotSealed(row);
        if (isHalted(row)) {
            throw new RosterRuleException("此行已就地停住，停住后不再改字，要动只能另开一行");
        }
    }

    private void assertNotSealed(TOadsSpot row) {
        if (recountStep(row) == WalkStep.SEALED.getStep()) {
            throw new RosterRuleException("此行已压卡到头：往后加不进去、字改不动、也撤不下来，非动不可只能另开一行");
        }
    }

    private void requireOperator(String operator) {
        if (isBlank(operator)) {
            throw new RosterRuleException("口头报备不算建档：须落建档岗经办人");
        }
    }

    private int parseIntField(String raw, String fieldName, Integer keepOnBlank) {
        if (isBlank(raw)) {
            if (keepOnBlank != null) {
                return keepOnBlank;
            }
            throw new RosterRuleException("检查点3·数量：字段[" + fieldName + "]缺填，本行进不了库");
        }
        String v = raw.trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new RosterRuleException("检查点3·数量：字段[" + fieldName + "]不是整数（原值“" + v
                    + "”），本行进不了库");
        }
    }

    private int parseGotField(String raw, Integer keepOnBlank) {
        if (isBlank(raw)) {
            // 已核验落地处数缺填按 0 起算（首次建档时常见），不是拒绝项
            return keepOnBlank == null ? 0 : keepOnBlank;
        }
        String v = raw.trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new RosterRuleException("检查点3·数量：字段[已核验落地处数]不是整数（原值“" + v
                    + "”），本行进不了库");
        }
    }

    private void accumulate(Map<String, long[]> map, String key, int num) {
        long[] agg = map.get(key);
        if (agg == null) {
            agg = new long[2];
            map.put(key, agg);
        }
        agg[0]++;
        agg[1] += num;
    }

    private List<RosterGroup> groupsOrdered(Map<String, long[]> agg, Comparator<String> order) {
        List<String> keys = new ArrayList<String>(agg.keySet());
        keys.sort(order);
        List<RosterGroup> groups = new ArrayList<RosterGroup>();
        for (String key : keys) {
            long[] v = agg.get(key);
            groups.add(new RosterGroup(key, v[0], v[1]));
        }
        return groups;
    }

    private Comparator<String> kindOrderComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                int ia = AREA_KIND_ORDER.indexOf(a);
                int ib = AREA_KIND_ORDER.indexOf(b);
                if (ia < 0) {
                    ia = AREA_KIND_ORDER.size();
                }
                if (ib < 0) {
                    ib = AREA_KIND_ORDER.size();
                }
                if (ia != ib) {
                    return ia - ib;
                }
                return a.compareTo(b);
            }
        };
    }

    private Comparator<String> channelOrderComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                int ia = CHANNEL_ORDER.indexOf(a);
                int ib = CHANNEL_ORDER.indexOf(b);
                if (ia < 0) {
                    ia = CHANNEL_ORDER.size();
                }
                if (ib < 0) {
                    ib = CHANNEL_ORDER.size();
                }
                if (ia != ib) {
                    return ia - ib;
                }
                return a.compareTo(b);
            }
        };
    }

    private long sumSpotNum(List<TOadsSpot> rows) {
        long n = 0;
        for (TOadsSpot s : rows) {
            if (s.getNeedNum() != null) {
                n += s.getNeedNum();
            }
        }
        return n;
    }

    /**
     * 当前停在哪一步只按 append-only 留痕逐笔重数（ADVANCE +1、RETURN 回 0，按动作时刻排），
     * 行上 walk_step 只是这一处写回的回显；屏上填的档、经办人记的档都不算数。
     * 纸簿旧行一条留痕都没有：行上步数若已记就认，空着按待核——不把在走的旧行当已压行的。
     */
    private int recountStep(TOadsSpot row) {
        if (row == null || row.getId() == null) {
            return WalkStep.WAIT.getStep();
        }
        List<TOadsSpotWalkLog> logs = walkLogMapper.listBySpot(row.getId());
        if (logs.isEmpty()) {
            return row.getWalkStep() == null ? WalkStep.WAIT.getStep() : row.getWalkStep();
        }
        int step = WalkStep.WAIT.getStep();
        for (TOadsSpotWalkLog log : logs) {
            if (log.getToStep() != null) {
                step = log.getToStep();
            }
        }
        return step;
    }

    private boolean isHalted(TOadsSpot row) {
        return row.getHalted() != null && row.getHalted() == 1;
    }

    /** 在走：留痕重数未压卡且未就地停住。旧行步数空着算在走（占名额），不能当成已压行。 */
    private boolean isWalking(TOadsSpot row) {
        return recountStep(row) < WalkStep.SEALED.getStep() && !isHalted(row);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
