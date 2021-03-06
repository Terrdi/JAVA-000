/**
 * 用户表
 **/
DROP TABLE IF EXISTS `geektime_user`;
CREATE TABLE `geektime_user` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户表id',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(50) NOT NULL COMMENT '用户密码',
    `email` varchar(50) DEFAULT NULL COMMENT '电子邮件',
    `phone` varchar(20) DEFAULT NULL COMMENT '注册手机号',
    `question` varchar(100) DEFAULT NULL COMMENT '找回密码问题',
    `answer` varchar(100) DEFAULT NULL COMMENT '找回密码答案',
    `role` int(4) NOT NULL COMMENT '角色0-管理员,1-普通用户',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '最后一次更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_name_unique` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**
 * 用户收货地址相关信息
 **/
DROP TABLE IF EXISTS `geektime_shipping`;
CREATE TABLE `geektime_shipping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `receiver_name` varchar(20) DEFAULT NULL COMMENT '收货姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货固定电话',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货移动电话',
  `receiver_province` varchar(20) DEFAULT NULL COMMENT '省份',
  `receiver_city` varchar(20) DEFAULT NULL COMMENT '城市',
  `receiver_district` varchar(20) DEFAULT NULL COMMENT '区/县',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `receiver_zip` varchar(6) DEFAULT NULL COMMENT '邮编',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;

/**
 * 商品表
 **/
 DROP TABLE IF EXISTS `geektime_product`;
 CREATE TABLE `geektime_product` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
   `name` varchar(100) NOT NULL COMMENT '商品名称',
   `main_image` varchar(500) DEFAULT NULL COMMENT '产品主图,url相对地址',
   `sub_images` text COMMENT '图片地址,json格式,扩展用',
   `detail` text COMMENT '商品详情(富文本)',
   `price` decimal(20,2) NOT NULL COMMENT '价格,单位-元保留两位小数',
   `stock` int(11) NOT NULL COMMENT '库存数量',
   `status` int(6) DEFAULT '1' COMMENT '商品状态.1-在售 2-下架 3-删除',
   `create_time` datetime DEFAULT NULL COMMENT '创建时间',
   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**
 * 订单表
 **/
DROP TABLE IF EXISTS `geektime_order`;
CREATE TABLE `geektime_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `shipping_id` int(11) DEFAULT NULL,
  `payment` decimal(20,2) DEFAULT NULL COMMENT '实际付款金额,单位是元,保留两位小数',
  `payment_type` int(4) DEFAULT NULL COMMENT '支付类型,1-在线支付 2-货到付款',
  `postage` int(10) DEFAULT NULL COMMENT '运费,单位是元',
  `status` int(10) DEFAULT NULL COMMENT '订单状态:0-已取消-10-未付款，20-已付款，40-已发货，50-交易成功，60-交易关闭',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `send_time` datetime DEFAULT NULL COMMENT '发货时间',
  `end_time` datetime DEFAULT NULL COMMENT '交易完成时间',
  `close_time` datetime DEFAULT NULL COMMENT '交易关闭时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no_index` (`order_no`) USING BTREE,
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**
 * 订单详情表
 **/
DROP TABLE IF EXISTS `geektime_order_item`;
CREATE TABLE `geektime_order_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单详情表id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `current_unit_price` decimal(20,2) DEFAULT NULL COMMENT '生成订单时的商品单价，单位是元,保留两位小数',
  `quantity` int(10) DEFAULT NULL COMMENT '商品数量',
  `total_price` decimal(20,2) DEFAULT NULL COMMENT '商品总价,单位是元,保留两位小数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `order_no_index` (`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
