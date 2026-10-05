package com.fc.v2.common.exception.roster;

/**
 * 名册建档口径违例：五道检查点按序裁决，前一个没过不进下一个；
 * 每个检查点失败各给各的说法，不合并成一句“数据有误”。
 *
 * <p>消息原样回到页面，作为这一行没落进库的交代。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class RosterRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RosterRuleException(String message) {
        super(message);
    }
}
