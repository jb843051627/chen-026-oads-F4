package com.fc.v2.service.roster;

/**
 * 建行通道。只有两条：建档岗逐条建立、集中更换核验完成后带出。
 *
 * <p>两条通道复用同一套五道检查点、同一套数量逻辑、同一套判定，
 * 界面上也只许一张建档单样式；纸簿迁入的存量旧行通道留空（null），走同一套算法。
 *
 * @author fuce
 * @date 2026-10-05
 */
public final class BuildChannel {

    /** 建档岗逐条建立 */
    public static final String MANUAL = "MANUAL";
    /** 集中更换业务在核验完成后带出 */
    public static final String BATCH = "BATCH";
    /** 纸簿迁入的存量旧行（库内值为 null，仅视图分组用此标签） */
    public static final String LEGACY = "LEGACY";

    private BuildChannel() {
    }

    public static boolean valid(String channel) {
        return MANUAL.equals(channel) || BATCH.equals(channel);
    }
}
