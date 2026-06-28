-- =============================================
-- 会员积分系统数据库表结构
-- =============================================

-- 会员表
DROP TABLE IF EXISTS `sys_member`;
CREATE TABLE `sys_member` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `level` int(1) DEFAULT 0 COMMENT '会员等级：0-普通用户，1-VIP，2-SVIP',
  `points` int(11) DEFAULT 0 COMMENT '积分余额',
  `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间',
  `last_signin_time` datetime DEFAULT NULL COMMENT '最后签到时间',
  `signin_count` int(11) DEFAULT 0 COMMENT '连续签到天数',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 积分记录表
DROP TABLE IF EXISTS `sys_points_log`;
CREATE TABLE `sys_points_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `change_points` int(11) DEFAULT 0 COMMENT '变动积分（正数为增加，负数为减少）',
  `before_points` int(11) DEFAULT 0 COMMENT '变动前积分',
  `after_points` int(11) DEFAULT 0 COMMENT '变动后积分',
  `type` int(1) DEFAULT 0 COMMENT '类型：0-签到，1-消费获取，2-兑换商品，3-兑换优惠券，4-兑换门票/酒店，5-系统赠送',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `related_id` varchar(32) DEFAULT NULL COMMENT '关联ID（订单ID、兑换记录ID等）',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 优惠券表
DROP TABLE IF EXISTS `sys_coupon`;
CREATE TABLE `sys_coupon` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(100) DEFAULT NULL COMMENT '优惠券名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `min_amount` decimal(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
  `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `points_required` int(11) DEFAULT 0 COMMENT '兑换所需积分',
  `require_level` int(1) DEFAULT 0 COMMENT '所需会员等级：0-普通，1-VIP，2-SVIP',
  `total_count` int(11) DEFAULT 0 COMMENT '总数量',
  `remain_count` int(11) DEFAULT 0 COMMENT '剩余数量',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `valid_days` int(11) DEFAULT NULL COMMENT '有效天数（自领取之日起）',
  `status` int(1) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
DROP TABLE IF EXISTS `sys_coupon_user`;
CREATE TABLE `sys_coupon_user` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `coupon_id` varchar(32) DEFAULT NULL COMMENT '优惠券ID',
  `coupon_name` varchar(100) DEFAULT NULL COMMENT '优惠券名称',
  `min_amount` decimal(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
  `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `status` int(1) DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `receive_time` datetime DEFAULT NULL COMMENT '领取时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `use_order_id` varchar(32) DEFAULT NULL COMMENT '使用的订单ID',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 商品表（积分商城）
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `description` text COMMENT '描述',
  `images` text COMMENT '商品图片（多个图片用逗号分隔）',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '商品价格（元）',
  `points_required` int(11) DEFAULT 0 COMMENT '兑换所需积分',
  `stock` int(11) DEFAULT 0 COMMENT '库存',
  `type` int(1) DEFAULT 0 COMMENT '类型：0-实物商品，1-纪念品，2-虚拟商品',
  `status` int(1) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 兑换记录表
DROP TABLE IF EXISTS `sys_exchange_record`;
CREATE TABLE `sys_exchange_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `type` int(1) DEFAULT 0 COMMENT '类型：0-商品，1-优惠券，2-景点门票，3-酒店',
  `related_id` varchar(32) DEFAULT NULL COMMENT '关联ID（商品ID、优惠券ID等）',
  `related_name` varchar(255) DEFAULT NULL COMMENT '关联名称',
  `points_used` int(11) DEFAULT 0 COMMENT '使用积分',
  `amount` decimal(10,2) DEFAULT 0.00 COMMENT '抵扣金额（仅景点/酒店）',
  `status` int(1) DEFAULT 0 COMMENT '状态：0-待处理，1-已完成，2-已取消',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址（实物商品）',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `receiver` varchar(100) DEFAULT NULL COMMENT '收货人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换记录表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 插入示例优惠券
INSERT INTO `sys_coupon` (`id`, `name`, `description`, `min_amount`, `discount_amount`, `points_required`, `require_level`, `total_count`, `remain_count`, `valid_days`, `status`, `sort`) VALUES
('1', '满100减10', '普通用户专享优惠券', 100.00, 10.00, 50, 0, 1000, 1000, 30, 1, 1),
('2', '满100减20', 'VIP专享优惠券', 100.00, 20.00, 80, 1, 500, 500, 30, 1, 2),
('3', '满100减30', 'SVIP专享优惠券', 100.00, 30.00, 100, 2, 300, 300, 30, 1, 3);

-- 插入示例商品
INSERT INTO `sys_product` (`id`, `name`, `description`, `images`, `price`, `points_required`, `stock`, `type`, `status`, `sort`) VALUES
('1', '精美冰箱贴', '旅游景点精美冰箱贴纪念品', 'https://example.com/images/magnet.jpg', 20.00, 200, 100, 1, 1, 1),
('2', '定制帆布袋', '环保定制帆布袋', 'https://example.com/images/bag.jpg', 35.00, 350, 50, 1, 1, 2),
('3', '精美书签套装', '景点纪念书签套装', 'https://example.com/images/bookmark.jpg', 15.00, 150, 200, 1, 1, 3);

-- =============================================
-- 旅途印记相关表
-- =============================================

-- 旅游分享表
DROP TABLE IF EXISTS `sys_travel_share`;
CREATE TABLE `sys_travel_share` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `images` text COMMENT '图片（多个用逗号分隔）',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  `location` varchar(200) DEFAULT NULL COMMENT '地点',
  `type` int(1) DEFAULT 0 COMMENT '类型：0-普通分享，1-心得体会，2-旅游攻略',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游分享表';

-- 旅游分享点赞表
DROP TABLE IF EXISTS `sys_travel_share_like`;
CREATE TABLE `sys_travel_share_like` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `share_id` varchar(32) DEFAULT NULL COMMENT '分享ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游分享点赞表';
