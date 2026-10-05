package com.fc.v2.service.roster;

/**
 * 名册三档：待核 → 已核 → 压卡。只认这三档，步子不得跨。
 *
 * <p>行目前停在哪一档，由系统按 append-only 推步留痕自己数；
 * 屏上填的档、经办人记的档都不作数。
 *
 * @author fuce
 * @date 2026-10-05
 */
public enum WalkStep {

    /** 第0档：待核（建行即落此档，0 步完成） */
    WAIT(0, "待核"),
    /** 第1档：已核（未压卡前可退回待核一步重来） */
    CHECKED(1, "已核"),
    /** 第2档：压卡（到头：加不进去、字改不动、也撤不下来，只许另开一行） */
    SEALED(2, "压卡");

    private final int step;
    private final String label;

    WalkStep(int step, String label) {
        this.step = step;
        this.label = label;
    }

    public int getStep() {
        return step;
    }

    public String getLabel() {
        return label;
    }

    public static WalkStep ofStep(int step) {
        for (WalkStep s : values()) {
            if (s.step == step) {
                return s;
            }
        }
        throw new IllegalArgumentException("不存在的档：" + step + "（只认 0待核/1已核/2压卡）");
    }
}
