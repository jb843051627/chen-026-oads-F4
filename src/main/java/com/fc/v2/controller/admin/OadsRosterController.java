package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.conf.OadsRosterProperties;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.exception.roster.RosterRuleException;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.custom.roster.OperatorScope;
import com.fc.v2.model.custom.roster.RosterPage;
import com.fc.v2.model.custom.roster.RosterQuery;
import com.fc.v2.model.custom.roster.RosterViews;
import com.fc.v2.model.custom.roster.SpotBuildForm;
import com.fc.v2.model.custom.roster.SyncResult;
import com.fc.v2.service.ITOadsRosterService;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 点位名册建档 Controller（五道检查点形状）——另写的等价建档去处。
 *
 * <p>旧的 {@code OadsSpotController} 通用入口收什么回什么维持原状，本控制器不动它本体。
 * 建档只有两条通道（建档岗逐条、集中更换带出），共用同一张建档单、同一套检查点与数量判定，
 * 判定全在 {@link ITOadsRosterService} 唯一裁决处；本层不收步次、不数处数、不推游标。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Api(value = "点位名册建档")
@Controller
@RequestMapping("/oadsRoster")
public class OadsRosterController extends BaseController {

    private final String prefix = "admin/oadsRoster";

    @Autowired
    private ITOadsRosterService oadsRosterService;

    @Autowired
    private OadsRosterProperties rosterProperties;

    @ApiOperation(value = "名册跳转", notes = "名册跳转")
    @GetMapping("/view")
    @RequiresPermissions("oads:oadsRoster:view")
    public String view(ModelMap model) {
        model.put("pageSize", rosterProperties.getPageSize());
        return prefix + "/list";
    }

    @Log(title = "点位名册分页查询", action = "list")
    @ApiOperation(value = "名册分页", notes = "每页条数取区局配置；三种入口前缀/全等；可见范围按辖段切")
    @GetMapping("/page")
    @RequiresPermissions("oads:oadsRoster:list")
    @ResponseBody
    public ResultTable page(@RequestParam("quarterCode") String quarterCode,
                            RosterQuery query,
                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        RosterPage data = oadsRosterService.pageRoster(quarterCode, query, currentScope(),
                pageNum, rosterProperties.getPageSize());
        // 页首与页内同一份基础数据：count 是服务端数的总行数，处数放在 data.totalSpotNum
        ResultTable table = pageTable(data.getRows(), data.getTotal());
        table.setMsg("本季在册处数 " + data.getTotalSpotNum() + "（页首与回执同一处写回）");
        return table;
    }

    @ApiOperation(value = "三种视图", notes = "辖段类别/路段代号/建档情形，同一份基础数据，处数一致")
    @GetMapping("/views")
    @RequiresPermissions("oads:oadsRoster:list")
    @ResponseBody
    public AjaxResult views(@RequestParam("quarterCode") String quarterCode) {
        RosterViews views = oadsRosterService.views(quarterCode, currentScope());
        return AjaxResult.success("ok")
                .put("data", views)
                .put("spotNum", views.getTotalSpotNum());
    }

    @ApiOperation(value = "一行详情", notes = "库中同一行加推步留痕")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("oads:oadsRoster:view")
    @ResponseBody
    public AjaxResult detail(@PathVariable("id") Long id) {
        return AjaxResult.success("ok").put("data", oadsRosterService.detail(id));
    }

    @ApiOperation(value = "推步留痕", notes = "退回重走的旧痕与新笔分开，原样保留")
    @GetMapping("/walkHistory/{id}")
    @RequiresPermissions("oads:oadsRoster:view")
    @ResponseBody
    public AjaxResult walkHistory(@PathVariable("id") Long id) {
        return AjaxResult.success("ok").put("data", oadsRosterService.walkHistory(id));
    }

    // ------------------------------------------------ 两条建行通道（同一张单）

    @Log(title = "名册建档·建档岗逐条", action = "buildManual")
    @ApiOperation(value = "建档岗逐条建立", notes = "五道检查点按序，数量服务端轧")
    @PostMapping("/buildManual")
    @RequiresPermissions("oads:oadsRoster:build")
    @ResponseBody
    public AjaxResult buildManual(SpotBuildForm form) {
        try {
            return AjaxResult.success("建档通过五道检查点，页面与库是同一行")
                    .put("data", oadsRosterService.buildManual(form, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "名册建档·集中更换带出", action = "buildFromBatch")
    @ApiOperation(value = "集中更换核验完成后带出", notes = "与逐条建行同一套检查点/数量/样式")
    @PostMapping("/buildFromBatch")
    @RequiresPermissions("oads:oadsRoster:build")
    @ResponseBody
    public AjaxResult buildFromBatch(SpotBuildForm form) {
        try {
            return AjaxResult.success("集中更换核验完成带出，同一套建档检查与数量逻辑")
                    .put("data", oadsRosterService.buildFromBatch(form, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "名册更正已核验落地处数", action = "correctGotNum")
    @ApiOperation(value = "更正已核验处数", notes = "写库前重轧待补处数，覆盖旧值")
    @PostMapping("/correctGotNum")
    @RequiresPermissions("oads:oadsRoster:edit")
    @ResponseBody
    public AjaxResult correctGotNum(Long id, Integer gotNum) {
        try {
            return AjaxResult.success("已按唯一裁决处重轧待补处数")
                    .put("data", oadsRosterService.correctGotNum(id, gotNum, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "名册改挂路段目录", action = "rehang")
    @ApiOperation(value = "改挂别的路段", notes = "重过围栏与名录依据，重轧待补；坐标不得改写")
    @PostMapping("/rehang")
    @RequiresPermissions("oads:oadsRoster:edit")
    @ResponseBody
    public AjaxResult rehang(Long id, String newAreaNo) {
        try {
            return AjaxResult.success("已改挂，冗余全称按目录修正，待补处数重轧")
                    .put("data", oadsRosterService.rehang(id, newAreaNo, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    // ------------------------------------------------ 三档推步

    @Log(title = "名册往上推一步", action = "advance")
    @ApiOperation(value = "推一步", notes = "待核→已核→压卡，不跨档；档由系统按留痕数")
    @PostMapping("/advance")
    @RequiresPermissions("oads:oadsRoster:walk")
    @ResponseBody
    public AjaxResult advance(Long id) {
        try {
            return AjaxResult.success("已推进一步")
                    .put("data", oadsRosterService.advance(id, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "名册已核退回待核", action = "returnToWait")
    @ApiOperation(value = "退回一步重来", notes = "上回留痕保留，重走那笔分开记")
    @PostMapping("/returnToWait")
    @RequiresPermissions("oads:oadsRoster:walk")
    @ResponseBody
    public AjaxResult returnToWait(Long id) {
        try {
            return AjaxResult.success("已退回待核，上一回记下的原样保留")
                    .put("data", oadsRosterService.returnToWait(id, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "名册就地停住", action = "halt")
    @ApiOperation(value = "就地停住", notes = "行仍在册可列可计，让出同点位在走名额")
    @PostMapping("/halt")
    @RequiresPermissions("oads:oadsRoster:walk")
    @ResponseBody
    public AjaxResult halt(Long id) {
        try {
            return AjaxResult.success("已就地停住，同点位名额腾出")
                    .put("data", oadsRosterService.halt(id, ShiroUtils.getLoginName()));
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    // ------------------------------------------------ 市里增量同步

    @Log(title = "市里增量比对", action = "syncWithCity")
    @ApiOperation(value = "每日增量同步", notes = "游标之后才比；冲突逐条输出不静默覆盖；游标同事务推进")
    @PostMapping("/sync")
    @RequiresPermissions("oads:oadsRoster:sync")
    @ResponseBody
    public AjaxResult sync() {
        try {
            SyncResult result = oadsRosterService.syncWithCity(ShiroUtils.getLoginName());
            return AjaxResult.success("本次比对" + result.getComparedRows() + "行，冲突"
                    + result.getConflicts().size() + "条，已逐条输出；游标 "
                    + result.getFromVersion() + " → " + result.getToVersion())
                    .put("data", result);
        } catch (RosterRuleException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /** 压卡行不可删：名册不给物理删除入口（撤不下来，非动不可另开一行），保留方法签名的 @DeleteMapping 不暴露 */
    @ApiOperation(value = "名册不提供删除", notes = "压卡撤不下来；未压卡也无撤行入口，停住或另开一行")
    @DeleteMapping("/remove")
    @RequiresPermissions("oads:oadsRoster:walk")
    @ResponseBody
    public AjaxResult remove() {
        return AjaxResult.error("名册行撤不下来：走到压卡只许另开一行，未压卡可就地停住，不做物理删除");
    }

    /**
     * 可见范围由登录岗位点出：岗位名含“区局”的是区局岗（三类路段都可见、仍限划归辖段），
     * 其余按本段经办岗（只见本段挂靠的行）。岗位与辖段的划归以辖段目录 scope_posts 为准。
     */
    private OperatorScope currentScope() {
        String post = ShiroUtils.getUser() == null ? null : ShiroUtils.getUser().getPosName();
        if (post == null || post.trim().isEmpty()) {
            post = ShiroUtils.getLoginName();
        }
        boolean district = post.contains("区局");
        return new OperatorScope(post, district);
    }
}
