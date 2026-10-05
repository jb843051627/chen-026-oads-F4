package com.fc.v2.service.roster;

import com.fc.v2.common.exception.roster.RosterRuleException;

/**
 * 数量三字段唯一裁决处：核定应设处数、已核验落地处数、待补处数。
 *
 * <p>待补处数没有录入口，由前两个相减得出：30 与 12 得 18；20 与 7 得 13；相等得 0。
 * 建行、更正已核验处数、改挂别的路段目录，每次写库前都在本处重新轧一遍，覆盖上一次落库值。
 *
 * <p>核定应设缺填、不是整数、为负，或小于已核验落地；已核验落地为负——本行进不了库，
 * 提示点名不合规的字段名。早年存量行与本年新增行同一套算法，不因建档时间分叉。
 *
 * @author fuce
 * @date 2026-10-05
 */
public final class SpotQuantityRule {

    private SpotQuantityRule() {
    }

    /** 三个数只认这一处给出的答案。 */
    public static final class Quantity {
        private final int needNum;
        private final int gotNum;
        private final int lackNum;

        Quantity(int needNum, int gotNum, int lackNum) {
            this.needNum = needNum;
            this.gotNum = gotNum;
            this.lackNum = lackNum;
        }

        public int getNeedNum() {
            return needNum;
        }

        public int getGotNum() {
            return gotNum;
        }

        public int getLackNum() {
            return lackNum;
        }
    }

    /**
     * 校验并轧数。报文里的待补处数一律不采信——只按核定应设减已核验落地重算。
     *
     * @param needNum 核定应设处数（缺填/非整/为负拒绝）
     * @param gotNum  已核验落地处数（缺填按 0；为负拒绝；高于核定应设拒绝）
     */
    public static Quantity settle(Integer needNum, Integer gotNum) {
        // 核定应设处数缺填、不是整数、为负——Integer 收进来已无非整一说，非整在控制器绑定层挡，这里再把负数点掉
        if (needNum == null) {
            throw new RosterRuleException("核定应设处数缺填，本行进不了库");
        }
        if (needNum < 0) {
            throw new RosterRuleException("核定应设处数为负（" + needNum + "）不合规，本行进不了库");
        }
        int got = gotNum == null ? 0 : gotNum;
        if (got < 0) {
            throw new RosterRuleException("已核验落地处数为负（" + got + "）不合规，本行进不了库");
        }
        if (got > needNum) {
            // 已核验落地高于核定应设：两个字段名一起点名，谁也不替谁兜
            throw new RosterRuleException("已核验落地处数（" + got + "）高于核定应设处数（" + needNum
                    + "），字段[已核验落地处数]不合规，本行进不了库");
        }
        int lack = needNum - got;
        return new Quantity(needNum, got, lack);
    }
}
