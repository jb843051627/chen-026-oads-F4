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
import com.fc.v2.service.impl.TOadsRosterServiceImpl;
import com.fc.v2.service.roster.BuildChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 名册建档五道检查点、三档推步、旧行兼容、三视图一致、增量游标逐条核对
 *（mapper 全 Mock，内存账台记账，不落真库）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TOadsRosterServiceImplTest {

    @Mock private TOadsSpotMapper spotMapper;
    @Mock private TOadsAreaBookMapper areaMapper;
    @Mock private TOadsSpotWalkLogMapper walkLogMapper;
    @Mock private TOadsCitySpotMapper citySpotMapper;
    @Mock private TOadsSyncCursorMapper syncCursorMapper;
    @Mock private TOadsRosterQueryMapper rosterQueryMapper;

    private TOadsRosterServiceImpl service;

    private final Map<Long, TOadsSpot> spots = new HashMap<Long, TOadsSpot>();
    private final Map<Long, TOadsAreaBook> areas = new HashMap<Long, TOadsAreaBook>();
    private final List<TOadsSpotWalkLog> logs = new ArrayList<TOadsSpotWalkLog>();
    private final List<TOadsCitySpot> cityRows = new ArrayList<TOadsCitySpot>();
    private TOadsSyncCursor cursor;
    private long idSeq = 5000;

    private static final String Q = "2026Q4";
    private static final String SEG_POST = "临江经办岗";
    private static final String SEG2_POST = "河滨经办岗";
    private static final String DISTRICT_POST = "区局建档岗";

    @BeforeEach
    public void wireHarness() {
        service = new TOadsRosterServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "spotMapper", spotMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "areaMapper", areaMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "walkLogMapper", walkLogMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "citySpotMapper", citySpotMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "syncCursorMapper", syncCursorMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "rosterQueryMapper", rosterQueryMapper);

        when(spotMapper.selectById(anyLong())).thenAnswer(i -> spots.get(i.getArgument(0)));
        when(spotMapper.selectByIdForUpdate(anyLong())).thenAnswer(i -> spots.get(i.getArgument(0)));
        when(spotMapper.insert(any())).thenAnswer(i -> {
            TOadsSpot s = i.getArgument(0);
            if (s.getId() == null) {
                s.setId(++idSeq);
            }
            spots.put(s.getId(), s);
            return 1;
        });
        when(spotMapper.updateById(any())).thenAnswer(i -> {
            TOadsSpot s = i.getArgument(0);
            spots.put(s.getId(), s);
            return 1;
        });
        when(spotMapper.listNormalBySpotNo(anyString())).thenAnswer(i -> {
            String no = i.getArgument(0);
            List<TOadsSpot> l = new ArrayList<TOadsSpot>();
            for (TOadsSpot s : spots.values()) {
                if (no.equals(s.getSpotNo()) && (s.getDelFlag() == null || s.getDelFlag() == 0)) {
                    l.add(s);
                }
            }
            return l;
        });
        when(spotMapper.findNormalBySpotQuarter(anyString(), anyString())).thenAnswer(i -> {
            String no = i.getArgument(0), q = i.getArgument(1);
            for (TOadsSpot s : spots.values()) {
                if (no.equals(s.getSpotNo()) && q.equals(s.getQuarterCode())
                        && (s.getDelFlag() == null || s.getDelFlag() == 0)) {
                    return s;
                }
            }
            return null;
        });

        when(areaMapper.findNormalByAreaNo(anyString())).thenAnswer(i -> {
            String no = i.getArgument(0);
            for (TOadsAreaBook a : areas.values()) {
                if (no.equals(a.getAreaNo()) && (a.getDelFlag() == null || a.getDelFlag() == 0)) {
                    return a;
                }
            }
            return null;
        });
        when(areaMapper.listAllNormal()).thenAnswer(i -> new ArrayList<TOadsAreaBook>(areas.values()));

        when(walkLogMapper.insert(any())).thenAnswer(i -> {
            TOadsSpotWalkLog g = i.getArgument(0);
            if (g.getId() == null) {
                g.setId(++idSeq);
            }
            logs.add(g);
            return 1;
        });
        when(walkLogMapper.listBySpot(anyLong())).thenAnswer(i -> {
            Long sid = i.getArgument(0);
            List<TOadsSpotWalkLog> l = new ArrayList<TOadsSpotWalkLog>();
            for (TOadsSpotWalkLog g : logs) {
                if (sid.equals(g.getSpotId())) {
                    l.add(g);
                }
            }
            l.sort(Comparator.comparing(TOadsSpotWalkLog::getActTime)
                    .thenComparing(TOadsSpotWalkLog::getId));
            return l;
        });

        when(citySpotMapper.listChangedAfter(org.mockito.ArgumentMatchers.anyLong())).thenAnswer(i -> {
            long v = i.getArgument(0);
            List<TOadsCitySpot> l = new ArrayList<TOadsCitySpot>();
            for (TOadsCitySpot c : cityRows) {
                if ((c.getDelFlag() == null || c.getDelFlag() == 0) && c.getRowVersion() > v) {
                    l.add(c);
                }
            }
            l.sort(Comparator.comparing(TOadsCitySpot::getRowVersion));
            return l;
        });
        when(citySpotMapper.selectBySpotQuarter(anyString(), anyString())).thenAnswer(i -> {
            String no = i.getArgument(0), q = i.getArgument(1);
            for (TOadsCitySpot c : cityRows) {
                if (no.equals(c.getSpotNo()) && q.equals(c.getQuarterCode())) {
                    return c;
                }
            }
            return null;
        });

        lenient().when(syncCursorMapper.selectSingletonForUpdate()).thenAnswer(i -> cursor);
        lenient().when(syncCursorMapper.selectSingleton()).thenAnswer(i -> cursor);
        when(syncCursorMapper.insert(any())).thenAnswer(i -> { cursor = i.getArgument(0); return 1; });
        when(syncCursorMapper.updateById(any())).thenAnswer(i -> { cursor = i.getArgument(0); return 1; });

        // 与 custom XML 等价的内存过滤
        when(rosterQueryMapper.queryRoster(anyString(), any(), anyBoolean(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.nullable(String.class), anyBoolean()))
                .thenAnswer(i -> queryInMemory(i.getArgument(0), i.getArgument(1), i.getArgument(2),
                        i.getArgument(3), i.getArgument(4), i.getArgument(5)));

        seedAreas();
    }

    // ------------------------------------------------ 账台与夹具

    private void seedAreas() {
        area(0L, "DU00", "临江路段全称", "主干道", "114.0", "114.2", "30.0", "30.2", 0,
                DISTRICT_POST + "," + SEG_POST);
        area(2L, "DU02", "河滨路段全称", "次干道", "120.0", "121.0", "31.0", "31.2", 0,
                DISTRICT_POST + "," + SEG2_POST);
        // 邻段 DU03 与 DU00 围栏四至相交：点位在交界，坐标两边围栏内都成立（改挂的合法去处）
        area(3L, "DU03", "改隶的邻段全称", "背街巷", "114.05", "114.15", "30.05", "30.15", 0,
                DISTRICT_POST + "," + SEG_POST);
        area(9L, "DU99", "已注销的旧段全称", "背街巷", "1.0", "2.0", "1.0", "2.0", 1,
                DISTRICT_POST + "," + SEG_POST);
    }

    private TOadsAreaBook area(long id, String no, String name, String kind,
                               String x0, String x1, String y0, String y1, int status, String posts) {
        TOadsAreaBook a = new TOadsAreaBook();
        a.setId(id);
        a.setAreaNo(no);
        a.setAreaName(name);
        a.setAreaKind(kind);
        a.setLngMin(new BigDecimal(x0));
        a.setLngMax(new BigDecimal(x1));
        a.setLatMin(new BigDecimal(y0));
        a.setLatMax(new BigDecimal(y1));
        a.setStatus(status);
        a.setScopePosts(posts);
        a.setDelFlag(0);
        areas.put(id, a);
        return a;
    }

    private SpotBuildForm form(String spotNo, String areaNo, String need, String got) {
        return form(spotNo, areaNo, "114.10", "30.10", need, got);
    }

    private SpotBuildForm form(String spotNo, String areaNo, String lng, String lat,
                               String need, String got) {
        SpotBuildForm f = new SpotBuildForm();
        f.setSpotNo(spotNo);
        f.setQuarterCode(Q);
        f.setAreaNo(areaNo);
        f.setLng(new BigDecimal(lng));
        f.setLat(new BigDecimal(lat));
        f.setNeedNum(need);
        f.setGotNum(got);
        f.setSetNo("经营主体甲");
        return f;
    }

    private SpotRosterRow build(String spotNo, String areaNo, String need, String got) {
        SpotBuildForm f = form(spotNo, areaNo, need, got);
        // 默认坐标落在 DU00/DU03 围栏；挂 DU02 的行给一组落在 DU02 围栏内的现场坐标
        if ("DU02".equals(areaNo)) {
            f.setLng(new BigDecimal("120.50"));
            f.setLat(new BigDecimal("31.10"));
        }
        return service.buildManual(f, "建档岗甲");
    }

    private List<TOadsSpot> queryInMemory(String quarter, List<String> areaNos, boolean includeUnattached,
                                          String field, String value, boolean prefix) {
        List<TOadsSpot> out = new ArrayList<TOadsSpot>();
        for (TOadsSpot s : spots.values()) {
            if (s.getDelFlag() != null && s.getDelFlag() != 0) {
                continue;
            }
            if (quarter != null && !quarter.equals(s.getQuarterCode())) {
                continue;
            }
            boolean scopeOk;
            if (includeUnattached) {
                scopeOk = s.getAreaNo() == null || s.getAreaNo().trim().isEmpty()
                        || (areaNos != null && areaNos.contains(s.getAreaNo()));
            } else {
                scopeOk = areaNos != null && areaNos.contains(s.getAreaNo());
            }
            if (!scopeOk) {
                continue;
            }
            if ("spotNo".equals(field)) {
                boolean hit = prefix ? s.getSpotNo() != null && s.getSpotNo().startsWith(value)
                        : value.equals(s.getSpotNo());
                if (!hit) {
                    continue;
                }
            } else if ("areaNo".equals(field)) {
                boolean hit = prefix ? s.getAreaNo() != null && s.getAreaNo().startsWith(value)
                        : value.equals(s.getAreaNo());
                if (!hit) {
                    continue;
                }
            }
            out.add(s);
        }
        out.sort(Comparator.comparing(TOadsSpot::getCreateTime,
                Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(TOadsSpot::getId, Comparator.reverseOrder()));
        return out;
    }

    private TOadsCitySpot cityRow(long version, String spotNo, String name, int status) {
        TOadsCitySpot c = new TOadsCitySpot();
        c.setId(version);
        c.setSpotNo(spotNo);
        c.setQuarterCode(Q);
        c.setCityAreaName(name);
        c.setCityStatus(status);
        c.setRowVersion(version);
        c.setUpdateTime(new java.util.Date());
        c.setDelFlag(0);
        cityRows.add(c);
        return c;
    }

    // ------------------------------------------------ 检查点3：数量（唯一一处轧）

    @Test
    public void lackNumIsSettledThirtyMinusTwelveAndTwentyMinusSevenAndEqual() {
        SpotRosterRow r1 = build("SP30", "DU00", "30", "12");
        assertEquals(Integer.valueOf(18), r1.getSpot().getLackNum(), "30 与 12 得 18");

        SpotRosterRow r2 = build("SP20", "DU00", "20", "7");
        assertEquals(Integer.valueOf(13), r2.getSpot().getLackNum(), "20 与 7 得 13");

        SpotRosterRow r3 = build("SP5", "DU00", "9", "9");
        assertEquals(Integer.valueOf(0), r3.getSpot().getLackNum(), "两者相等得 0");

        // 报文即便自带待补处数也不采信：表单压根没有这个入口
        assertEquals(3, spots.size());
    }

    @Test
    public void gotNumCorrectionRecomputesLackAndOverwrites() {
        SpotRosterRow r = build("SP30", "DU00", "30", "12");
        assertEquals(Integer.valueOf(18), r.getSpot().getLackNum());
        SpotRosterRow c = service.correctGotNum(r.getSpot().getId(), 23, "建档岗甲");
        assertEquals(Integer.valueOf(30), c.getSpot().getNeedNum());
        assertEquals(Integer.valueOf(23), c.getSpot().getGotNum());
        assertEquals(Integer.valueOf(7), c.getSpot().getLackNum(), "更正已核验处数，写库前重轧待补");
    }

    @Test
    public void rehangRecomputesLackAndFixesCanonicalName() {
        SpotRosterRow r = build("SP30", "DU00", "30", "12");
        // 库里冗余全称被人改成与目录有出入：改挂那一次以目录为准修正
        TOadsSpot s = spots.get(r.getSpot().getId());
        s.setAreaName("被人写歪的全称");
        SpotRosterRow moved = service.rehang(s.getId(), "DU03", "建档岗甲");
        assertEquals("DU03", moved.getSpot().getAreaNo());
        assertEquals("改隶的邻段全称", moved.getSpot().getAreaName(), "冗余全称以目录为准修正");
        assertEquals(Integer.valueOf(18), moved.getSpot().getLackNum(), "改挂是第三个重算时刻");
    }

    @Test
    public void quantityFailuresNameTheField() {
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("S1", "DU00", "114.1", "30.1", null, "1"), "建档岗甲"))
                .getMessage().contains("核定应设处数]缺填"));
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("S2", "DU00", "114.1", "30.1", "-1", "0"), "建档岗甲"))
                .getMessage().contains("核定应设处数为负"));
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("S3", "DU00", "114.1", "30.1", "12.5", "0"), "建档岗甲"))
                .getMessage().contains("核定应设处数]不是整数"));
        RosterRuleException high = assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("S4", "DU00", "114.1", "30.1", "10", "12"), "建档岗甲"));
        assertTrue(high.getMessage().contains("已核验落地处数") && high.getMessage().contains("不合规"),
                high.getMessage());
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("S5", "DU00", "114.1", "30.1", "10", "-3"), "建档岗甲"))
                .getMessage().contains("已核验落地处数为负"));
        assertEquals(0, spots.size(), "哪条都没进库");
    }

    // ------------------------------------------------ 检查点1：身份

    @Test
    public void duplicateSpotCodeSameQuarterRejectedButSelfUpdatePasses() {
        SpotRosterRow first = build("SPDUP", "DU00", "30", "12");
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> build("SPDUP", "DU00", "30", "12"));
        assertTrue(e.getMessage().contains("检查点1") && e.getMessage().contains("仍然有效"), e.getMessage());
        assertEquals(1, spots.size());

        // 对象正是那一行本身：带主键按更新，不当成多出来的一行挡回去
        SpotBuildForm uf = form("SPDUP", "DU00", "30", "20");
        uf.setId(first.getSpot().getId());
        SpotRosterRow updated = service.buildManual(uf, "建档岗甲");
        assertEquals(1, spots.size(), "没有多出一行");
        assertEquals(Integer.valueOf(10), updated.getSpot().getLackNum());
    }

    @Test
    public void twoUnitsSameDayOrChangeOfKeeperCannotStackTwoRows() {
        build("SAME", "DU00", "30", "12");
        // 同一处点位换人重报（哪怕换辖段、换主体）也不许叠两行
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("SAME", "DU02", "120.5", "31.1", "5", "0"), "另一维护单位"));
        assertTrue(e.getMessage().contains("同时只容一行在走"));
    }

    @Test
    public void afterSealedOrHaltedTheSlotIsFreedForANewRow() {
        SpotRosterRow first = build("SEAL", "DU00", "30", "30");
        service.advance(first.getSpot().getId(), "建档岗甲");
        service.advance(first.getSpot().getId(), "建档岗甲");
        // 压卡那头名额空出：同点位同季度可立新行
        SpotRosterRow second = build("SEAL", "DU00", "8", "2");
        assertNotEquals(first.getSpot().getId(), second.getSpot().getId());
        assertEquals(2, spots.size());

        SpotRosterRow halted = build("HALT", "DU00", "4", "0");
        service.halt(halted.getSpot().getId(), "建档岗甲");
        assertNotNull(build("HALT", "DU00", "6", "0"), "就地停住同样让出名额");
    }

    // ------------------------------------------------ 检查点2：空间挂靠

    @Test
    public void coordinateOutsideFenceRejectedWithSpecificMessage() {
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("OUT", "DU00", "118.9", "30.1", "5", "0"), "甲"));
        assertTrue(e.getMessage().contains("该点位在围栏之外"), e.getMessage());
        assertEquals(0, spots.size());
    }

    @Test
    public void coordinatesAreFieldCollectedAndCannotBeRewritten() {
        SpotRosterRow r = build("FIX", "DU00", "30", "0");
        SpotBuildForm uf = form("FIX", "DU02", "120.50", "31.10", "30", "5");
        uf.setId(r.getSpot().getId());
        // 报来的新坐标不采信，仍按现场原值对新辖段围栏重校——原值在 DU02 围栏外，挡下
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(uf, "甲"));
        assertTrue(e.getMessage().contains("围栏之外"), e.getMessage());
    }

    @Test
    public void missingCoordinatesRejectedAtCheckpointTwo() {
        SpotBuildForm f = form("NOCOORD", "DU00", "30", "0");
        f.setLng(null);
        f.setLat(null);
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(f, "甲"));
        assertTrue(e.getMessage().startsWith("检查点2"), e.getMessage());
    }

    // ------------------------------------------------ 检查点4：名录依据

    @Test
    public void unknownRoadCodeFailsAtCheckpointFour() {
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("NOROAD", "DUXX", "114.1", "30.1", "5", "0"), "甲"));
        assertTrue(e.getMessage().startsWith("检查点4") && e.getMessage().contains("找不到"));
    }

    @Test
    public void deregisteredRoadRejectsNewButKeepsOldRowsListedAndCounted() {
        // 旧行挂在如今已注销的 DU99 上
        TOadsSpot legacy = new TOadsSpot();
        legacy.setId(701L);
        legacy.setSpotNo("OLD99");
        legacy.setQuarterCode(Q);
        legacy.setAreaId(9);
        legacy.setAreaNo("DU99");
        legacy.setAreaName("已注销的旧段全称");
        legacy.setNeedNum(11);
        legacy.setGotNum(0);
        legacy.setLackNum(11);
        legacy.setWalkStep(null); // 纸簿旧行步数空着
        legacy.setHalted(0);
        legacy.setDelFlag(0);
        spots.put(701L, legacy);

        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildManual(form("NEW99", "DU99", "1.5", "1.5", "5", "0"), "甲"));
        assertTrue(e.getMessage().contains("已注销") && e.getMessage().contains("不再接受新增行"));

        // 注销前旧行仍列出、仍计在册处数（经办岗辖段划归里含 DU99）
        RosterViews v = service.views(Q, OperatorScope.segment(SEG_POST));
        long num99 = sum(v.getByAreaKind(), "背街巷");
        assertEquals(11, num99, "注销不连带清除，旧行照计");
    }

    // ------------------------------------------------ 两条通道同一样式

    @Test
    public void batchChannelReusesSameChecksAndNeedsBatchNo() {
        RosterRuleException e = assertThrows(RosterRuleException.class,
                () -> service.buildFromBatch(form("B1", "DU00", "114.1", "30.1", "5", "0"), "甲"));
        assertTrue(e.getMessage().contains("批次码"));

        SpotBuildForm f = form("B1", "DU00", "118.0", "30.1", "5", "0");
        f.setBatchNo("BN2026Q4-1");
        RosterRuleException fence = assertThrows(RosterRuleException.class,
                () -> service.buildFromBatch(f, "甲"));
        assertTrue(fence.getMessage().contains("围栏之外"), "集中更换通道同一套检查点");

        f.setLng(new BigDecimal("114.10"));
        SpotRosterRow r = service.buildFromBatch(f, "甲");
        assertEquals(BuildChannel.BATCH, r.getSpot().getBuildChannel());
        assertEquals(Integer.valueOf(5), r.getSpot().getLackNum());
    }

    // ------------------------------------------------ 三档推步：不跨档/退回/压卡冻结

    @Test
    public void walkAdvancesOneStepAtATimeSealedRowIsImmutable() {
        SpotRosterRow r = build("WALK", "DU00", "30", "30");
        assertEquals("待核", r.getStepLabel());

        assertEquals("已核", service.advance(r.getSpot().getId(), "甲").getStepLabel());
        SpotRosterRow sealed = service.advance(r.getSpot().getId(), "甲");
        assertEquals("压卡", sealed.getStepLabel());
        assertNotNull(sealed.getSpot().getSealedAt());

        // 到头：推进、改数、改挂、按更新重建，一个都不让
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.advance(r.getSpot().getId(), "甲")).getMessage().contains("压卡"));
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.correctGotNum(r.getSpot().getId(), 1, "甲")).getMessage().contains("压卡"));
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.rehang(r.getSpot().getId(), "DU02", "甲")).getMessage().contains("压卡"));
        SpotBuildForm uf = form("WALK", "DU00", "30", "29");
        uf.setId(r.getSpot().getId());
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.buildManual(uf, "甲")).getMessage().contains("压卡"));
    }

    @Test
    public void returnOnlyFromCheckedKeepsOldLogApartFromResign() {
        SpotRosterRow r = build("RET", "DU00", "30", "12");
        service.advance(r.getSpot().getId(), "甲"); // 待核→已核
        // 待核那一步没有“已核”可退：新建行直接退是不让的（这里先验已核可退）
        SpotRosterRow back = service.returnToWait(r.getSpot().getId(), "甲");
        assertEquals("待核", back.getStepLabel());

        // 上一回记下的仍保留，与重走那笔分开
        service.advance(r.getSpot().getId(), "甲"); // 重走
        List<TOadsSpotWalkLog> h = service.walkHistory(r.getSpot().getId());
        assertEquals(3, h.size(), "推进、退回、再推进各一行，旧痕不删不并");
        assertEquals("RETURN", h.get(1).getAction());
        assertEquals(Integer.valueOf(1), h.get(1).getFromStep());
        assertEquals(Integer.valueOf(0), h.get(1).getToStep());
    }

    @Test
    public void cannotReturnFromWaitOrSealed() {
        SpotRosterRow r = build("WAIT0", "DU00", "30", "12");
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.returnToWait(r.getSpot().getId(), "甲")).getMessage().contains("待核"));
        service.advance(r.getSpot().getId(), "甲");
        service.advance(r.getSpot().getId(), "甲"); // 压卡
        assertTrue(assertThrows(RosterRuleException.class,
                () -> service.returnToWait(r.getSpot().getId(), "甲")).getMessage().contains("压卡"));
    }

    @Test
    public void currentStepIsCountedFromLogNotFromScreenOrOperator() {
        SpotRosterRow r = build("COUNT", "DU00", "30", "12");
        service.advance(r.getSpot().getId(), "甲");
        // 有人把行上步数拨回 0，留痕仍在已核：推进时按留痕重数，下一笔该从已核往压卡走
        spots.get(r.getSpot().getId()).setWalkStep(0);
        SpotRosterRow again = service.advance(r.getSpot().getId(), "甲");
        assertEquals("压卡", again.getStepLabel(), "档由系统按留痕自己数，拨表不算数");
    }

    // ------------------------------------------------ 纸簿旧行兼容

    @Test
    public void legacyRowsWithEmptyStepCodeAndVerifiedStringAreTolerated() {
        TOadsSpot legacy = new TOadsSpot();
        legacy.setId(801L);
        legacy.setSpotNo("LEGACY1");
        legacy.setQuarterCode(Q);
        legacy.setAreaId(0);
        legacy.setAreaNo("DU00");
        legacy.setNeedNum(7);
        legacy.setGotNum(null);
        legacy.setLackNum(null);
        legacy.setWalkStep(null);
        legacy.setHalted(0);
        legacy.setBuildChannel(null); // 纸簿迁入
        legacy.setDelFlag(0);
        spots.put(801L, legacy);

        // 一碰不能报错
        SpotRosterRow d = service.detail(801L);
        assertEquals("待核", d.getStepLabel(), "空步数按待核容下，不当成已压行");
        assertTrue(d.isWalking(), "在走的旧行不是压行");

        // 旧行也能往上推，不因其建档早而分叉
        assertEquals("已核", service.advance(801L, "甲").getStepLabel());

        RosterViews v = service.views(Q, OperatorScope.segment(SEG_POST));
        assertEquals(7, sum(v.getByBuildChannel(), BuildChannel.LEGACY), "旧行走同一套计数");
    }

    // ------------------------------------------------ 三视图一致 / 查询 / 可见范围 / 分页

    @Test
    public void threeViewsAgreeAndEmptyQuarterIsLegal() {
        build("A1", "DU00", "30", "12"); // 主干道 18 待补，处数 30
        build("A2", "DU02", "20", "7");  // 次干道，处数 20
        SpotBuildForm b = form("A3", "DU00", "10", "10");
        b.setBatchNo("BN-1");
        service.buildFromBatch(b, "甲"); // 主干道，集中更换，处数 10

        RosterViews v = service.views(Q, OperatorScope.district(DISTRICT_POST));
        long k = sumAll(v.getByAreaKind());
        long n = sumAll(v.getByAreaNo());
        long c = sumAll(v.getByBuildChannel());
        assertEquals(60, v.getTotalSpotNum());
        assertEquals(k, v.getTotalSpotNum());
        assertEquals(n, v.getTotalSpotNum());
        assertEquals(c, v.getTotalSpotNum());
        assertEquals(40, sum(v.getByAreaKind(), "主干道"));
        assertEquals(20, sum(v.getByAreaKind(), "次干道"));
        assertEquals(40, sum(v.getByAreaNo(), "DU00"));
        assertEquals(50, sum(v.getByBuildChannel(), BuildChannel.MANUAL));
        assertEquals(10, sum(v.getByBuildChannel(), BuildChannel.BATCH));
        assertEquals(3, v.getTotalRowCount());

        // 空季度是合法结果，不许邻段补齐
        RosterViews empty = service.views("2099Q1", OperatorScope.district(DISTRICT_POST));
        assertEquals(0, empty.getTotalRowCount());
        assertEquals(0, empty.getTotalSpotNum());
        assertTrue(empty.getByAreaKind().isEmpty());
    }

    @Test
    public void scopeCutsByAssignedAreasDistrictSeesAllThreeKinds() {
        build("V1", "DU00", "30", "0");
        build("V2", "DU02", "20", "0");

        RosterViews seg1 = service.views(Q, OperatorScope.segment(SEG_POST));
        assertEquals(1, seg1.getTotalRowCount(), "本段经办岗只见本段");
        assertEquals(30, seg1.getTotalSpotNum());

        RosterViews seg2 = service.views(Q, OperatorScope.segment(SEG2_POST));
        assertEquals(1, seg2.getTotalRowCount());

        RosterViews district = service.views(Q, OperatorScope.district(DISTRICT_POST));
        assertEquals(2, district.getTotalRowCount(), "区局岗三类路段都可见（限划归辖段）");
        assertEquals(50, district.getTotalSpotNum());

        // 一个辖段都没划归的岗：什么也看不到
        RosterViews nobody = service.views(Q, OperatorScope.segment("别的岗"));
        assertEquals(0, nobody.getTotalRowCount());
    }

    @Test
    public void queriesSupportEqualsAndPrefixOnThreeEntries() {
        build("PREFIX-01", "DU00", "5", "0");
        build("PREFIX-02", "DU00", "7", "0");
        build("OTHER-9", "DU02", "9", "0");

        assertEquals(2, service.pageRoster(Q, q("spotNo", "PREFIX", "prefix"),
                OperatorScope.district(DISTRICT_POST), 1, 20).getTotal());
        assertEquals(1, service.pageRoster(Q, q("spotNo", "PREFIX-01", "eq"),
                OperatorScope.district(DISTRICT_POST), 1, 20).getTotal());
        assertEquals(2, service.pageRoster(Q, q("areaNo", "DU00", "prefix"),
                OperatorScope.district(DISTRICT_POST), 1, 20).getTotal());
        RosterPage byKind = service.pageRoster(Q, q("areaKind", "主干道", "eq"),
                OperatorScope.district(DISTRICT_POST), 1, 20);
        assertEquals(2, byKind.getTotal());
        assertEquals(12, byKind.getTotalSpotNum(), "页首处数与页内同一处写回");

        RosterRuleException bad = assertThrows(RosterRuleException.class,
                () -> service.pageRoster(Q, q("setNo", "x", "eq"),
                        OperatorScope.district(DISTRICT_POST), 1, 20));
        assertTrue(bad.getMessage().contains("三种查询入口"));
    }

    @Test
    public void paginationUsesConfiguredSizeAndKeepsOneSpotNum() {
        for (int i = 0; i < 5; i++) {
            build("PG" + i, "DU00", "2", "0");
        }
        RosterPage p1 = service.pageRoster(Q, null, OperatorScope.district(DISTRICT_POST), 1, 2);
        assertEquals(2, p1.getRows().size());
        assertEquals(5, p1.getTotal());
        assertEquals(10, p1.getTotalSpotNum(), "页首处数不随翻页变");
        RosterPage p3 = service.pageRoster(Q, null, OperatorScope.district(DISTRICT_POST), 3, 2);
        assertEquals(1, p3.getRows().size());
        assertEquals(10, p3.getTotalSpotNum(), "回执仍取同一处的数");
    }

    // ------------------------------------------------ 检查点5：增量同步与游标

    @Test
    public void dailySyncComparesAfterCursorListsConflictsAndAdvancesCursorTogether() {
        SpotRosterRow r = build("SYNC1", "DU00", "30", "12");
        assertNotNull(r);
        cityRow(1L, "SYNC1", "市里头另一套全称", 0);     // 全称冲突
        cityRow(2L, "SYNC2", "无所谓", 1);              // 市里注销而本局没有 → 不冲突
        build("SYNC3", "DU00", "4", "0");
        cityRow(3L, "SYNC3", "临江路段全称", 1);         // 市里注销、本局仍挂载 → 冲突

        SyncResult res = service.syncWithCity("区局建档岗");
        assertEquals(0L, res.getFromVersion());
        assertEquals(3L, res.getToVersion(), "游标随本次一并推进到比对停的那一版");
        assertEquals(3, res.getComparedRows());
        assertEquals(2, res.getConflicts().size(), "逐条输出，不静默覆盖");
        assertNotNull(cursor);
        assertEquals(3L, cursor.getLastVersion().longValue());

        List<String> types = new ArrayList<String>();
        for (SyncResult.Conflict c : res.getConflicts()) {
            types.add(c.getType());
        }
        assertTrue(types.contains("NAME_MISMATCH"));
        assertTrue(types.contains("CITY_REVOKED"));

        // 第二日：游标之前的行不复核，没有新增就没有比较
        SyncResult again = service.syncWithCity("区局建档岗");
        assertEquals(3L, again.getFromVersion());
        assertEquals(0, again.getComparedRows());
        assertTrue(again.getConflicts().isEmpty());
        assertEquals(3L, again.getToVersion());

        // 冲突只出清单：本局挂载与全称原样未动
        assertEquals("临江路段全称", service.detail(r.getSpot().getId()).getAreaNameCanonical());
    }

    // ------------------------------------------------ 辅助

    private RosterQuery q(String field, String value, String match) {
        RosterQuery query = new RosterQuery();
        query.setField(field);
        query.setValue(value);
        query.setMatch(match);
        return query;
    }

    private long sum(List<RosterGroup> groups, String key) {
        for (RosterGroup g : groups) {
            if (key.equals(g.getKey())) {
                return g.getSpotNum();
            }
        }
        return 0;
    }

    private long sumAll(List<RosterGroup> groups) {
        long n = 0;
        for (RosterGroup g : groups) {
            n += g.getSpotNum();
        }
        return n;
    }
}
