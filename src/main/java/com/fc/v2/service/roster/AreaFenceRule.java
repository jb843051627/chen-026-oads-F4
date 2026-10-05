package com.fc.v2.service.roster;

import com.fc.v2.common.exception.roster.RosterRuleException;
import com.fc.v2.model.auto.TOadsAreaBook;

import java.math.BigDecimal;

/**
 * 检查点2 空间挂靠唯一裁决处：现场坐标必须落在辖段挂靠的围栏之内。
 *
 * <p>坐标与辖段挂靠来自现场采集，前端不得人工改写；落在围栏之外一律不收，
 * 提示写明“该点位在围栏之外”。矩形围栏四至含边界。
 *
 * @author fuce
 * @date 2026-10-05
 */
public final class AreaFenceRule {

    private AreaFenceRule() {
    }

    /**
     * 围栏校验。四至缺一栏视为该辖段围栏未圈成，照样不收——不替现场补坐标、不放宽到全辖段。
     */
    public static void requireInside(BigDecimal lng, BigDecimal lat, TOadsAreaBook area) {
        if (area == null) {
            // 名录依据检查点负责代号查不到的说法；走到这里的 area 必非空
            throw new RosterRuleException("该点位的辖段挂靠取不到，坐标无从落围栏");
        }
        if (lng == null || lat == null) {
            throw new RosterRuleException("该点位现场坐标（经度、纬度）缺栏，坐标来自现场采集不得人工补填，本行进不了库");
        }
        BigDecimal lngMin = area.getLngMin();
        BigDecimal lngMax = area.getLngMax();
        BigDecimal latMin = area.getLatMin();
        BigDecimal latMax = area.getLatMax();
        if (lngMin == null || lngMax == null || latMin == null || latMax == null) {
            throw new RosterRuleException("辖段[" + area.getAreaNo() + "]围栏四至未圈成，该点位暂不具备挂靠条件，本行进不了库");
        }
        boolean inside = lng.compareTo(lngMin) >= 0 && lng.compareTo(lngMax) <= 0
                && lat.compareTo(latMin) >= 0 && lat.compareTo(latMax) <= 0;
        if (!inside) {
            throw new RosterRuleException("该点位在围栏之外：现场坐标（经度" + lng.toPlainString()
                    + "，纬度" + lat.toPlainString() + "）不在辖段[" + area.getAreaNo() + "]围栏四至之内，本行不收");
        }
    }
}
