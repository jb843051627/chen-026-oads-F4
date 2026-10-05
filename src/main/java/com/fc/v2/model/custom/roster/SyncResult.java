package com.fc.v2.model.custom.roster;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次市里增量同步的结果：游标之后的行逐条比较，冲突逐条输出，不静默覆盖。
 *
 * <p>冲突两类：两边全称冲突；市里已注销而本局仍挂载。一律以本局为准，
 * 只出清单不改市里边、也不改本局挂载。游标推进到本次比较停的那一版，
 * 与清单出自同一份计算。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class SyncResult {

    /** 同步前游标版本（之前的行不复核） */
    private long fromVersion;

    /** 同步后游标版本（本次比对停在哪一版就推进到哪一版） */
    private long toVersion;

    /** 本次参与比较的行数（游标之后） */
    private long comparedRows;

    /** 逐条冲突清单 */
    private List<Conflict> conflicts = new ArrayList<Conflict>();

    public long getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(long fromVersion) {
        this.fromVersion = fromVersion;
    }

    public long getToVersion() {
        return toVersion;
    }

    public void setToVersion(long toVersion) {
        this.toVersion = toVersion;
    }

    public long getComparedRows() {
        return comparedRows;
    }

    public void setComparedRows(long comparedRows) {
        this.comparedRows = comparedRows;
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    /** 一条冲突：哪个点位、哪一类、两边各是什么、以谁为准。 */
    public static class Conflict {
        /** NAME_MISMATCH 全称冲突 / CITY_REVOKED 市里已注销本局仍挂载 */
        private final String type;
        private final String spotNo;
        private final String quarterCode;
        private final String localValue;
        private final String cityValue;
        private final String message;

        public Conflict(String type, String spotNo, String quarterCode,
                        String localValue, String cityValue, String message) {
            this.type = type;
            this.spotNo = spotNo;
            this.quarterCode = quarterCode;
            this.localValue = localValue;
            this.cityValue = cityValue;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public String getSpotNo() {
            return spotNo;
        }

        public String getQuarterCode() {
            return quarterCode;
        }

        public String getLocalValue() {
            return localValue;
        }

        public String getCityValue() {
            return cityValue;
        }

        public String getMessage() {
            return message;
        }
    }
}
