-- oads 城镇户外广告与店招牌匾设置安全监管 -- schema (chen-026)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_026

CREATE TABLE IF NOT EXISTS t_oads_area_book (
  id bigint NOT NULL COMMENT '主键',
  area_no varchar(64) DEFAULT NULL COMMENT '属地段册代号',
  area_name varchar(128) DEFAULT NULL COMMENT '辖段全称',
  area_kind varchar(32) DEFAULT NULL COMMENT '辖段类别',
  lng_min decimal(10,6) DEFAULT NULL COMMENT '围栏经度起（含，现场坐标落在围栏之外一律不收）',
  lng_max decimal(10,6) DEFAULT NULL COMMENT '围栏经度止（含）',
  lat_min decimal(10,6) DEFAULT NULL COMMENT '围栏纬度起（含）',
  lat_max decimal(10,6) DEFAULT NULL COMMENT '围栏纬度止（含）',
  scope_posts varchar(255) DEFAULT NULL COMMENT '可见岗位清单（逗号分隔：本段经办岗只见本段；区局岗按划归辖段）',
  road_name varchar(128) DEFAULT NULL COMMENT '属地一直写到门牌（澜原省—江水市—区—街巷）',
  align_kind varchar(32) DEFAULT NULL COMMENT '来路（市里段册下发／本局自录）',
  status int DEFAULT NULL COMMENT '段册情形 0在册 1已撤出',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属地段册底册';

CREATE TABLE IF NOT EXISTS t_oads_check_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '集中更换批次码',
  row_no int DEFAULT NULL COMMENT '原报行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被对上的点位码',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行应更换处数',
  chk_num decimal(12,2) DEFAULT NULL COMMENT '本行核验合格处数',
  put_kind varchar(32) DEFAULT NULL COMMENT '来路（区级报上来／市局自核两条来路）',
  status int DEFAULT NULL COMMENT '行落地情形 0未勾 1已收下 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中更换核验行';

CREATE TABLE IF NOT EXISTS t_oads_gov_flow (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '点位监管办件流转单号',
  stage int DEFAULT NULL COMMENT '当前关次 0..3（申报/核准/设置/验收）',
  status int DEFAULT NULL COMMENT '单子走到哪一关 0未起 1在办 2已封住',
  content varchar(255) DEFAULT NULL COMMENT '一关一记',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次挪关动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位监管办件流转单';

CREATE TABLE IF NOT EXISTS t_oads_notice_item (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '届期催办与巡查条目代号',
  due_at datetime DEFAULT NULL COMMENT '该发催办那一一日（落库为 datetime）',
  amount decimal(12,2) DEFAULT NULL COMMENT '最多许提前几日',
  content varchar(255) DEFAULT NULL COMMENT '事由与所对点位的记要',
  spot_no varchar(64) DEFAULT NULL COMMENT '所对点位码（挂点位底册）',
  area_no varchar(64) DEFAULT NULL COMMENT '路段目录代号（并拨键一）',
  door_seg varchar(64) DEFAULT NULL COMMENT '门牌段（并拨键二）',
  contact varchar(128) DEFAULT NULL COMMENT '联系栏',
  status int DEFAULT NULL COMMENT '条目情形 0搁着(未启动) 1已发出(已派发) 2发不成(派不出)',
  sent_at datetime DEFAULT NULL COMMENT '派发那一刻（两边记号之一）',
  sent_post varchar(64) DEFAULT NULL COMMENT '接手的岗位（两边记号之一）',
  fail_step varchar(64) DEFAULT NULL COMMENT '卡住的那一步',
  fail_reason varchar(255) DEFAULT NULL COMMENT '派不出原因（毛病出在哪一格）',
  round_no varchar(32) DEFAULT NULL COMMENT '本轮轮次码（三个数只讲本次这一轮）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期催办与巡查条目';

CREATE TABLE IF NOT EXISTS t_oads_rest_day (
  id bigint NOT NULL COMMENT '主键',
  rest_day date NOT NULL COMMENT '休办那一日',
  kind int DEFAULT NULL COMMENT '休办来路 1法定节假日 2市里下文停办',
  note varchar(255) DEFAULT NULL COMMENT '依据文号或说明',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='休办日历（法定节假日与停办日，数办工日只认这一张表）';

CREATE TABLE IF NOT EXISTS t_oads_post_quota (
  id bigint NOT NULL COMMENT '主键',
  post_no varchar(64) NOT NULL COMMENT '岗位代号',
  quarter_code varchar(8) NOT NULL COMMENT '季度码 如2026Q4',
  quota_total int DEFAULT NULL COMMENT '本季定额件数',
  status int DEFAULT '0' COMMENT '定额情形 0在用 1停用',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位季度派发定额';

CREATE TABLE IF NOT EXISTS t_oads_permit_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '设置许可核准单号',
  spot_id bigint DEFAULT NULL COMMENT '单据来源：点位主档那一行的主键（只能由主档记录生成）',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码（建单时自点位主档冗余，封存后也不许改）',
  change_kind varchar(16) DEFAULT NULL COMMENT '本次变更情形 HANG挂上去 MOVE改位置 REMOVE整块拆除',
  from_state int DEFAULT NULL COMMENT '变更前挂牌情形档（建单时自主档点出）',
  to_state int DEFAULT NULL COMMENT '变更后挂牌情形档（封存时同一次处理写回主档）',
  node_no int DEFAULT NULL COMMENT '当前所在段 0..2（窗口受理/区局核准/市局备案，服务端计算，不采信报文）',
  sign_mode int DEFAULT NULL COMMENT '本段通过办法 0一名即过 1两关须同判 2先落为定（三段各用各的，不许串用）',
  need_count int DEFAULT NULL COMMENT '本段要求签署人数',
  sign_count int DEFAULT NULL COMMENT '本段本轮已落单笔数（只按留痕逐笔算，屏上仅回显）',
  round_no int DEFAULT '1' COMMENT '本段当前轮次（本段被清一次加一轮，老轮文本留在留痕表）',
  status int DEFAULT NULL COMMENT '核准情形 0在签(含待议) 1已点齐(已封存) 2已压回(本段本轮被否)',
  live_token varchar(128) DEFAULT NULL COMMENT '在途去重令牌 同一点位同一种变更在途只一张；封存置空',
  sealed_at datetime(3) DEFAULT NULL COMMENT '走满封存那一刻（第三段两条同意齐备）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_permit_live_token (live_token),
  KEY idx_permit_spot (spot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设置许可核准单';

-- 签署留痕：一次签署一行，只许追加不许改；清段只把 active 抹成 0，老轮文本继续可查。
-- 段次(node_no)由服务端按单据当前段点出后落库，报文自称第几段不作数。
CREATE TABLE IF NOT EXISTS t_oads_permit_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '哪张核准单',
  node_no int NOT NULL COMMENT '落单时服务端点出的段 0窗口受理 1区局核准 2市局备案',
  round_no int NOT NULL COMMENT '本段轮次 清一段加一轮 本轮文本与上轮分开存放',
  seq_no int NOT NULL COMMENT '本单本段本轮第几笔（逐笔累加，同秒各记各的也不并笔）',
  sign_post varchar(32) DEFAULT NULL COMMENT '所在岗（服务端按段点出：窗口受理岗/区局核定岗/市局备案岗）',
  signer varchar(64) NOT NULL COMMENT '签署人',
  opinion int NOT NULL COMMENT '判断 1同意 2不同意',
  content varchar(500) DEFAULT NULL COMMENT '签署文本，原文入库留痕',
  sign_time datetime(3) NOT NULL COMMENT '各自落单时刻（毫秒计时，同一秒两笔仍为两条）',
  active int DEFAULT '1' COMMENT '本轮是否在档 1当前轮 0随本段清轮退出（不删行，继续可查）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sign_once (bill_id, node_no, round_no, signer),
  KEY idx_sign_bill_node (bill_id, node_no, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='核准单逐段签署留痕（ append-only ）';

-- 点位主档变更履历：封存那一次处理里与主档改动同生的一条，事后按时间倒序回看。
CREATE TABLE IF NOT EXISTS t_oads_spot_change (
  id bigint NOT NULL COMMENT '主键',
  spot_id bigint NOT NULL COMMENT '点位主档行',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码',
  bill_id bigint NOT NULL COMMENT '依据哪张核准单',
  bill_no varchar(64) DEFAULT NULL COMMENT '依据的核准单号',
  change_kind varchar(16) DEFAULT NULL COMMENT '变更情形 HANG挂上去 MOVE改位置 REMOVE整块拆除',
  from_state int DEFAULT NULL COMMENT '从哪一档（挂牌情形档）',
  to_state int DEFAULT NULL COMMENT '改到哪一档（挂牌情形档）',
  from_ver int DEFAULT NULL COMMENT '变更前挂设档位',
  to_ver int DEFAULT NULL COMMENT '变更后挂设档位',
  operate_by varchar(64) DEFAULT NULL COMMENT '由谁操作（封存处理人）',
  change_time datetime(3) NOT NULL COMMENT '发生于哪一时刻',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_change_spot_time (spot_id, change_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位主档变更履历（append-only）';

CREATE TABLE IF NOT EXISTS t_oads_rule_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '牌面安全归层判据线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  obj_kind varchar(32) DEFAULT NULL COMMENT '这条线管点位册面还是核准卷面',
  th1_max decimal(12,3) DEFAULT NULL COMMENT '起算限值',
  th2_max decimal(12,3) DEFAULT NULL COMMENT '够线限值',
  th3_max decimal(12,3) DEFAULT NULL COMMENT '封顶限值',
  eff_start datetime DEFAULT NULL COMMENT '起用那一一日',
  eff_end datetime DEFAULT NULL COMMENT '交班那一一日(不含)',
  priority int DEFAULT NULL COMMENT '交班次序(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0在位 1已撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='牌面安全归层判据线';

CREATE TABLE IF NOT EXISTS t_oads_spot (
  id bigint NOT NULL COMMENT '主键',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码',
  area_id int DEFAULT NULL COMMENT '挂在哪一段册名下',
  area_no varchar(64) DEFAULT NULL COMMENT '段册代号（冗余自段册）',
  quarter_code varchar(8) DEFAULT NULL COMMENT '季度码 如2026Q4（本季度同码判重按它）',
  lng decimal(10,6) DEFAULT NULL COMMENT '现场采集经度（建档落一次，旁入口不改写）',
  lat decimal(10,6) DEFAULT NULL COMMENT '现场采集纬度（建档落一次，旁入口不改写）',
  area_name varchar(128) DEFAULT NULL COMMENT '路段全称冗余（与目录有出入以目录为准修正）',
  walk_step int DEFAULT NULL COMMENT '已完成推步步数 0待核 1已核 2压卡（服务端按留痕数，屏填不算）',
  halted int DEFAULT '0' COMMENT '是否就地停住 0否 1是（停住即让出同点位在走名额，行仍在册）',
  build_channel varchar(16) DEFAULT NULL COMMENT '建档通道 MANUAL建档岗逐条 BATCH集中更换带出',
  sealed_at datetime(3) DEFAULT NULL COMMENT '压卡那一刻（到头，只许另开一行）',
  set_no varchar(64) DEFAULT NULL COMMENT '设置主体代号或承办岗代号',
  rule_code varchar(64) DEFAULT NULL COMMENT '点位挂着的判据线代号（区级配置；按标识取值入口照它问，空着的只走整表取优）',
  need_num int DEFAULT NULL COMMENT '核定应设处数',
  got_num int DEFAULT NULL COMMENT '已核验落地处数',
  lack_num int DEFAULT NULL COMMENT '还差几处（轧出来，不手填）',
  content varchar(255) DEFAULT NULL COMMENT '随件交来的牌面题名与要件',
  set_state int DEFAULT '0' COMMENT '挂牌情形档 0未挂设 1挂设在册 2整块拆除（只有核准单封存才许挪）',
  set_ver int DEFAULT '0' COMMENT '挂设档位 挂上去或改位置封存一次加一；拆除不进位',
  safe_level int DEFAULT '0' COMMENT '牌面安全分档 0不分档 1..4（服务端一处区间比对给出，页面不再算第二遍）',
  safe_rule_code varchar(64) DEFAULT NULL COMMENT '本轮固化的判据线代号（不分档留空，逐字照抄当次采信那一条）',
  safe_eff_start datetime DEFAULT NULL COMMENT '本轮固化的起用那一一日（不分档留空，与代号同一对，逐字相同）',
  status int DEFAULT NULL COMMENT '册面情形 0新入册 1已核齐 2缺项',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位底册';

-- 牌面安全分档固化留痕：一次判定一行，只许追加。固化（改点位行）与留痕（加这一行）须同一个事务，
-- 成则同成、败则同回。固化进点位行的判据线代号与起用那一日，与本行这一对逐字相同。
CREATE TABLE IF NOT EXISTS t_oads_spot_grade_log (
  id bigint NOT NULL COMMENT '主键',
  spot_id bigint NOT NULL COMMENT '哪一处点位',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码（判定当时冗余）',
  score decimal(12,3) DEFAULT NULL COMMENT '本次询问使用的折算分（原始取值，0..100）',
  score_aligned decimal(12,2) DEFAULT NULL COMMENT '两位小数四舍五入取齐后的折算分',
  safe_level int NOT NULL COMMENT '落定档位 0不分档 1..4',
  rule_code varchar(64) DEFAULT NULL COMMENT '采信那一条的判据线代号（不分档留空）',
  rule_id bigint DEFAULT NULL COMMENT '采信那一条的主键（不分档留空）',
  eff_start datetime DEFAULT NULL COMMENT '采信那一条的起用那一一日（不分档留空，逐字照抄）',
  ask_at datetime NOT NULL COMMENT '本次询问时刻（按提交当时生效版本判，不按此刻最新版）',
  grade_msg varchar(500) DEFAULT NULL COMMENT '这一处给的说法（采信谁/为何不分档）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_grade_log_spot (spot_id, ask_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='牌面安全分档固化留痕（append-only）';

-- 老库升级（CREATE TABLE IF NOT EXISTS 不会补列，已建过 t_oads_spot 的库手跑这四句）：
-- ALTER TABLE t_oads_spot ADD COLUMN rule_code varchar(64) DEFAULT NULL COMMENT '点位挂着的判据线代号（区级配置）' AFTER set_no;
-- ALTER TABLE t_oads_spot ADD COLUMN safe_level int DEFAULT '0' COMMENT '牌面安全分档 0不分档 1..4' AFTER set_ver;
-- ALTER TABLE t_oads_spot ADD COLUMN safe_rule_code varchar(64) DEFAULT NULL COMMENT '本轮固化的判据线代号（不分档留空）' AFTER safe_level;
-- ALTER TABLE t_oads_spot ADD COLUMN safe_eff_start datetime DEFAULT NULL COMMENT '本轮固化的起用那一一日（不分档留空）' AFTER safe_rule_code;

-- ============================================================================
-- 名册建档（chen-026 五道检查点 / 三档推步 / 辖段围栏 / 市里增量同步）
-- 老库升级：已建过表的库手跑下列 ALTER；列名与实体契约逐列对齐，改列必须同步实体。
-- ============================================================================

-- 点位底册补列：季度码、现场坐标、路段全称冗余、三档推步步数、就地停住、建档通道、压卡时刻
-- ALTER TABLE t_oads_spot ADD COLUMN quarter_code varchar(8) DEFAULT NULL COMMENT '季度码 如2026Q4（本季度同码判重按它）' AFTER area_no;
-- ALTER TABLE t_oads_spot ADD COLUMN lng decimal(10,6) DEFAULT NULL COMMENT '现场采集经度（建档落一次，旁入口不改写）' AFTER quarter_code;
-- ALTER TABLE t_oads_spot ADD COLUMN lat decimal(10,6) DEFAULT NULL COMMENT '现场采集纬度（建档落一次，旁入口不改写）' AFTER lng;
-- ALTER TABLE t_oads_spot ADD COLUMN area_name varchar(128) DEFAULT NULL COMMENT '路段全称冗余（与目录有出入以目录为准修正）' AFTER lat;
-- ALTER TABLE t_oads_spot ADD COLUMN walk_step int DEFAULT NULL COMMENT '已完成推步步数 0待核 1已核 2压卡（服务端按留痕数，屏填不算）' AFTER area_name;
-- ALTER TABLE t_oads_spot ADD COLUMN halted int DEFAULT '0' COMMENT '是否就地停住 0否 1是（停住即让出同点位在走名额，行仍在册）' AFTER walk_step;
-- ALTER TABLE t_oads_spot ADD COLUMN build_channel varchar(16) DEFAULT NULL COMMENT '建档通道 MANUAL建档岗逐条 BATCH集中更换带出（两条通道同一套样式）' AFTER halted;
-- ALTER TABLE t_oads_spot ADD COLUMN sealed_at datetime(3) DEFAULT NULL COMMENT '压卡那一刻（到头，只许另开一行）' AFTER build_channel;

-- 属地段册补列：矩形围栏四至（现场坐标落在四至之外一律不收）
-- ALTER TABLE t_oads_area_book ADD COLUMN lng_min decimal(10,6) DEFAULT NULL COMMENT '围栏经度起（含）' AFTER area_kind;
-- ALTER TABLE t_oads_area_book ADD COLUMN lng_max decimal(10,6) DEFAULT NULL COMMENT '围栏经度止（含）' AFTER lng_min;
-- ALTER TABLE t_oads_area_book ADD COLUMN lat_min decimal(10,6) DEFAULT NULL COMMENT '围栏纬度起（含）' AFTER lng_max;
-- ALTER TABLE t_oads_area_book ADD COLUMN lat_max decimal(10,6) DEFAULT NULL COMMENT '围栏纬度止（含）' AFTER lat_min;
-- ALTER TABLE t_oads_area_book ADD COLUMN scope_posts varchar(255) DEFAULT NULL COMMENT '可见岗位清单（逗号分隔：本段经办岗只见本段；区局岗按划归辖段，三类路段都可见）' AFTER lat_max;
-- 旧种子补围栏（手跑）：
-- UPDATE t_oads_area_book SET lng_min=114.000000,lng_max=114.200000,lat_min=30.000000,lat_max=30.200000 WHERE id=0;
-- UPDATE t_oads_area_book SET lng_min=113.500000,lng_max=113.700000,lat_min=30.300000,lat_max=30.500000 WHERE id=1;

-- 推步留痕：三档逐级，一次推进/退回一行，只许追加。当前停在哪一步只按本表逐笔重数，
-- 屏上填的档、经办人记的档都不作数。退回（已核→待核）把上一回留痕原样保留，另起一行记退回。
CREATE TABLE IF NOT EXISTS t_oads_spot_walk_log (
  id bigint NOT NULL COMMENT '主键',
  spot_id bigint NOT NULL COMMENT '点位底册行',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码（当时冗余）',
  action varchar(16) NOT NULL COMMENT '动作 ADVANCE推进一步 RETURN已核退回待核（重走保留旧痕）',
  from_step int DEFAULT NULL COMMENT '动前步数 0待核 1已核 2压卡',
  to_step int DEFAULT NULL COMMENT '动后步数',
  operate_by varchar(64) DEFAULT NULL COMMENT '经办人（记账用，档不由他点）',
  act_time datetime(3) NOT NULL COMMENT '动作时刻',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_walk_spot_time (spot_id, act_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位三档推步留痕（append-only）';

-- 市里侧名册行（每日增量比对的对侧底影）。本局只读本侧，不回写市里数据。
CREATE TABLE IF NOT EXISTS t_oads_city_spot (
  id bigint NOT NULL COMMENT '主键',
  spot_no varchar(64) NOT NULL COMMENT '点位码',
  quarter_code varchar(8) NOT NULL COMMENT '季度码',
  city_area_name varchar(128) DEFAULT NULL COMMENT '市里记的路段全称（与本局冲突逐条出清单，不静默覆盖）',
  city_status int DEFAULT '0' COMMENT '市里情形 0在册 1已注销（市里注销而本局仍挂载，逐条列出）',
  row_version bigint NOT NULL COMMENT '市里侧行版本号（单调递增，供游标比较）',
  update_time datetime(3) NOT NULL COMMENT '市里侧该行最近变更时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_city_spot_quarter (spot_no, quarter_code),
  KEY idx_city_version (row_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市里侧名册行（增量比对对侧底影）';

-- 增量比对游标：全库唯一一行。游标之前的行不复核，游标之后的行才参与本次比较；
-- 游标随本次同步一并推进，且与本次冲突清单出自同一份计算、同一事务，别处不得自行调整。
CREATE TABLE IF NOT EXISTS t_oads_sync_cursor (
  id bigint NOT NULL COMMENT '主键（全库只容一行，固定 0）',
  last_version bigint DEFAULT '0' COMMENT '已比对到的市里侧行版本（之前不复核）',
  ran_at datetime(3) DEFAULT NULL COMMENT '上次同步时刻',
  run_remark varchar(500) DEFAULT NULL COMMENT '上次同步交代（比对行数/冲突条数）',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime(3) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市里增量比对游标（全库唯一）';

INSERT IGNORE INTO t_oads_sync_cursor (id, last_version, run_remark) VALUES (0, 0, '尚未同步：游标在起点，全量参与首次比较');

-- 初始档案数据（t_oads_area_book 预置两条段册种子）
-- area_id=0 为在册段册（主干道类），area_id=1 为已撤出的旧段册（次干道类），供挂接校验与新入册校验取用。
-- 段册代号与类别按路段类型给值。
INSERT IGNORE INTO t_oads_area_book (id, area_no, area_name, area_kind, lng_min, lng_max, lat_min, lat_max, road_name, status, del_flag, create_by, create_time)
VALUES (0, 'DU00', '澜原省江水市云桥区临江路段（主干道类，在册）', '主干道',
        114.000000, 114.200000, 30.000000, 30.200000,
        '澜原省—江水市—云桥区—临江街巷', 0, 0, 'seed', NOW()),
       (1, 'DU01', '南津区北岸段·并走撤出的旧段册（已撤出）', '次干道',
        113.500000, 113.700000, 30.300000, 30.500000,
        '澜原省—江水市—南津区—北岸街巷', 1, 0, 'seed', NOW());

