package com.fc.v2.model.custom.roster;

/**
 * 名册可见范围：由控制器按登录岗位点出，服务端按此切辖段。
 *
 * <p>本段经办岗只见本段挂靠的行；区局岗三类路段都可见，但范围仍限于划归它的辖段
 * （辖段目录 scope_posts 里有没有它这一岗为准）。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class OperatorScope {

    /** 岗位名（与 t_oads_area_book.scope_posts 清单对账） */
    private final String post;

    /** 是否区局岗（三类路段都可见；仍受划归辖段限制） */
    private final boolean district;

    public OperatorScope(String post, boolean district) {
        this.post = post;
        this.district = district;
    }

    public static OperatorScope segment(String post) {
        return new OperatorScope(post, false);
    }

    public static OperatorScope district(String post) {
        return new OperatorScope(post, true);
    }

    public String getPost() {
        return post;
    }

    public boolean isDistrict() {
        return district;
    }
}
