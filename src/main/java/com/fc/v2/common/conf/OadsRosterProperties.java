package com.fc.v2.common.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 名册相关区局配置。
 *
 * <p>每页条数由区局在配置里给出（{@code fuce.oads.roster.page-size}），名册分页只认这一个值，
 * 页面不传不覆盖；不在配置里给出时取默认 20。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Component
@ConfigurationProperties(prefix = "fuce.oads.roster")
public class OadsRosterProperties {

    /** 名册每页条数（区局配置，可继续下翻） */
    private int pageSize = 20;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
