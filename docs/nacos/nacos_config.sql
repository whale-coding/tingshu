/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.200.6
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : 192.168.200.6:3306
 Source Schema         : nacos_config

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 13/10/2023 21:01:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_use` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `effect` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_schema` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfo_datagrouptenant`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES (1, 'common.yaml', 'DEFAULT_GROUP', 'mybatis-plus:\n  configuration:\n    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl\n  mapper-locations: classpath:mapper/*Mapper.xml\nfeign:\n  sentinel:\n    enabled: true\n  client:\n    config:\n      default:\n        readTimeout: 3000\n        connectTimeout: 1000\nspring:\n  main:\n    allow-bean-definition-overriding: true #当遇到同样Bean名字的时候，是否允许覆盖注册\n  cloud:\n    sentinel:\n      transport:\n        dashboard: 192.168.200.6:8858\n    openfeign:\n      lazy-attributes-resolution: true #开启懒加载，否则启动报错\n      client:\n        config:\n          default:\n            connectTimeout: 30000\n            readTimeout: 30000\n            loggerLevel: basic\n  zipkin:\n    base-url: http://192.168.200.6:9411\n    discovery-client-enabled: false\n    sender:\n      type: web\n  kafka:\n    bootstrap-servers: 192.168.200.6:9092\n    producer:\n      retries: 3  #设置大于0的值，则客户端会将发送失败的记录重新发送\n      acks: 1\n      batch-size: 16384\n      buffer-memory: 33554432\n      key-serializer: org.apache.kafka.common.serialization.StringSerializer\n      value-serializer: org.apache.kafka.common.serialization.StringSerializer\n    consumer:\n      group-id: ${spring.application.name}\n      enable-auto-commit: true\n      auto-offset-reset: earliest\n      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n  data:\n    redis:\n      host: 192.168.200.6\n      port: 6379\n      database: 0\n      timeout: 1800000\n      # password: #如果redis有密码需要设置\n      lettuce:\n        pool:\n          max-active: 20 #最大连接数\n          max-wait: -1    #最大阻塞等待时间(负数表示没限制)\n          max-idle: 5    #最大空闲\n          min-idle: 0     #最小空闲\n    mongodb:\n      host: 192.168.200.6\n      port: 27017\n      database: tingshu #指定操作的数据库\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: GMT+8\n  servlet:\n    multipart:\n      max-file-size: 10MB     #单个文件最大限制\n      max-request-size: 20MB  #多个文件最大限制\nmanagement:\n  zipkin:\n    tracing:\n      endpoint: http://192.168.200.6:9411/api/v2/spans\n  tracing:\n    sampling:\n      probability: 1.0 # 记录速率100%', '51bcf2f33894cf8aff3c68ccddb3969e', '2023-10-09 20:04:03', '2023-10-10 22:07:25', 'nacos', '192.168.200.1', '', '', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (2, 'server-gateway-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8500\nspring:\n  cloud:\n    openfeign:\n      lazy-attributes-resolution: true\n      client:\n        config:\n          default:\n            connectTimeout: 30000\n            readTimeout: 30000\n            loggerLevel: basic\n    gateway:\n      discovery:      #是否与服务发现组件进行结合，通过 serviceId(必须设置成大写) 转发到具体的服务实例。默认为false，设为true便开启通过服务中心的自动根据 serviceId 创建路由的功能。\n        locator:      #路由访问方式：http://Gateway_HOST:Gateway_PORT/大写的serviceId/**，其中微服务应用名默认大写访问。\n          enabled: true\n      routes:\n        - id: service-album\n          uri: lb://service-album\n          predicates:\n            - Path=/*/album/**\n        - id: service-user\n          uri: lb://service-user\n          predicates:\n            - Path=/*/user/**\n        - id: service-order\n          uri: lb://service-order\n          predicates:\n            - Path=/*/order/**\n        - id: service-live\n          uri: lb://service-live\n          predicates:\n            - Path=/*/live/**\n        - id: service-live-websocket\n          uri: lb:ws://service-live #ws://localhost:8507\n          predicates:\n            - Path=/websocket/**\n        - id: service-account\n          uri: lb://service-account\n          predicates:\n            - Path=/*/account/**\n        - id: service-comment\n          uri: lb://service-comment\n          predicates:\n            - Path=/*/comment/**\n        - id: service-dispatch\n          uri: lb://service-dispatch\n          predicates:\n            - Path=/*/dispatch/**\n        - id: service-payment\n          uri: lb://service-payment\n          predicates:\n            - Path=/*/payment/**\n        - id: service-system\n          uri: lb://service-system\n          predicates:\n            - Path=/*/system/**\n        - id: service-search\n          uri: lb://service-search\n          predicates:\n            - Path=/*/search/**\n        - id: service-search\n          uri: lb://service-system\n          predicates:\n            - Path=/*/system/**', 'f53ad797ed9933ef6c53440975227b4e', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (3, 'service-account-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8505\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_account?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\n', 'df0cc608ff2f1b2d09265e797b941288', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (4, 'service-album-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8501\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_album?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nminio:\n  endpointUrl: http://192.168.200.6:9000\n  accessKey: admin\n  secreKey: admin123456\n  bucketName: tingshu\nvod:\n  appId: 1255727855\n  secretId: AKIDTOFybJvQqCWnDdyDNRQJ54xkT8hBbxCK\n  secretKey: DKLH87saCmKZowS09IPRLE4pVCqQuKeu\n  region: ap-beijing\n  procedure: SimpleAesEncryptPreset #任务流\n  #tempPath: /root/tingshu/tempPath\n  tempPath: D:\\code\\workspace2023\\tingshu\\temp\n  playKey: wrTwwu8U3DRSRDgC8l7q  #播放加密key\n', '10cb8ad7c9892160a3faef1923c84cef', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (5, 'service-comment-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8508\nspring:\n  data:\n    mongodb:\n      host: 192.168.200.6\n      port: 27017\n      database: tingshu #指定操作的数据库\n', '90cdaed91defef147a9dbbe05328d9ae', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (6, 'service-dispatch-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8509\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_dispatch?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nxxl:\n  job:\n    admin:\n      # 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行\"执行器心跳注册\"和\"任务结果回调\"；为空则关闭自动注册\n      addresses: http://localhost:8080/xxl-job-admin\n      # 执行器通讯TOKEN [选填]：非空时启用\n    accessToken: default_token\n    executor:\n      # 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册\n      appname: dispatch-xxl-job-executor\n      # 执行器注册 [选填]：优先使用该配置作为注册地址，为空时使用内嵌服务 ”IP:PORT“ 作为注册地址。从而更灵活的支持容器类型执行器动态IP和动态映射端口问题。\n      address:\n      # 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 \"执行器注册\" 和 \"调度中心请求并触发任务\"；\n      ip:\n      # 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；\n      port: 9999\n      # 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；\n      logpath: /data/applogs/xxl-job/jobhandler\n      # 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；\n      logretentiondays: 30\n', '66d051bd9ae880c83b45d1c767d83ed9', '2023-10-09 20:04:03', '2023-10-13 20:23:16', 'nacos', '192.168.200.1', '', '', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (7, 'service-order-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8504\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_order?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\n', 'f16e0475d8dbd63b2f4629ac43ada108', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (8, 'service-payment-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8506\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_payment?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nwechat:\n  pay:\n    #小程序微信公众平台appId，不是微信服务号appId，与小程序授权登录appId一致\n    appid: wxcc651fcbab275e33\n    mchid: 1481962542\n    appkey: MXb72b9RfshXZD4FRGV5KLqmv5bx9LT9\n    #微信商户平台配置的，其他项目使用，不可更改\n    notifyUrl: http://gmall-prod.atguigu.cn/api/payment/weixin/notify\n', '0a2481971704bf7c6c7b731ff8d38abe', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (9, 'service-search-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8502\nspring:\n  elasticsearch:\n      uris: http://192.168.200.6:9200\n      username: elastic\n      password: 111111\n', 'cf045bfd3addf1d7168ddf57cabe3101', '2023-10-09 20:04:03', '2023-10-09 20:04:03', NULL, '192.168.200.1', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (10, 'service-user-dev.yaml', 'DEFAULT_GROUP', 'server:\n  port: 8503\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_user?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nwechat:\n  login:\n    #小程序授权登录\n    appId: wxcc651fcbab275e33  # 小程序微信公众平台appId\n    appSecret: 5f353399a2eae7ff6ceda383e924c5f6  # 小程序微信公众平台api秘钥\n', '82fd6048e310a8839f88d4a3679805ac', '2023-10-09 20:04:03', '2023-10-09 20:51:53', 'nacos', '192.168.200.1', '', '', '', '', '', 'yaml', '', '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfoaggr_datagrouptenantdatum`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `datum_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '增加租户字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfobeta_datagrouptenant`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_beta' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfotag_datagrouptenanttag`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_tag' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`  (
  `id` bigint NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE INDEX `uk_configtagrelation_configidtag`(`id` ASC, `tag_name` ASC, `tag_type` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_tag_relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '集群、各Group容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`  (
  `id` bigint UNSIGNED NOT NULL,
  `nid` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `op_type` char(10) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`nid`) USING BTREE,
  INDEX `idx_gmt_create`(`gmt_create` ASC) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE,
  INDEX `idx_did`(`data_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '多租户改造' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info` VALUES (0, 1, 'common.yaml', 'DEFAULT_GROUP', '', 'mybatis-plus:\n  configuration:\n    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl\n  mapper-locations: classpath:mapper/*Mapper.xml\nfeign:\n  sentinel:\n    enabled: true\n  client:\n    config:\n      default:\n        readTimeout: 3000\n        connectTimeout: 1000\nspring:\n  main:\n    allow-bean-definition-overriding: true #当遇到同样Bean名字的时候，是否允许覆盖注册\n  cloud:\n    sentinel:\n      transport:\n        dashboard: 192.168.200.6:8858\n    openfeign:\n      lazy-attributes-resolution: true #开启懒加载，否则启动报错\n      client:\n        config:\n          default:\n            connectTimeout: 30000\n            readTimeout: 30000\n            loggerLevel: basic\n  zipkin:\n    base-url: http://192.168.200.6:9411\n    discovery-client-enabled: false\n    sender:\n      type: web\n  kafka:\n    bootstrap-servers: 192.168.200.6:9092\n    producer:\n      retries: 3  #设置大于0的值，则客户端会将发送失败的记录重新发送\n      acks: 1\n      batch-size: 16384\n      buffer-memory: 33554432\n      key-serializer: org.apache.kafka.common.serialization.StringSerializer\n      value-serializer: org.apache.kafka.common.serialization.StringSerializer\n    consumer:\n      group-id: ${spring.application.name}\n      enable-auto-commit: true\n      auto-offset-reset: earliest\n      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n  data:\n    redis:\n      host: 192.168.200.6\n      port: 6379\n      database: 0\n      timeout: 1800000\n      # password: #如果redis有密码需要设置\n      lettuce:\n        pool:\n          max-active: 20 #最大连接数\n          max-wait: -1    #最大阻塞等待时间(负数表示没限制)\n          max-idle: 5    #最大空闲\n          min-idle: 0     #最小空闲\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: GMT+8\n  servlet:\n    multipart:\n      max-file-size: 10MB     #单个文件最大限制\n      max-request-size: 20MB  #多个文件最大限制\nmanagement:\n  zipkin:\n    tracing:\n      endpoint: http://192.168.200.6:9411/api/v2/spans\n  tracing:\n    sampling:\n      probability: 1.0 # 记录速率100%', '2bb45c342849ed6cc21ab07e84d69546', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 2, 'server-gateway-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8500\nspring:\n  cloud:\n    openfeign:\n      lazy-attributes-resolution: true\n      client:\n        config:\n          default:\n            connectTimeout: 30000\n            readTimeout: 30000\n            loggerLevel: basic\n    gateway:\n      discovery:      #是否与服务发现组件进行结合，通过 serviceId(必须设置成大写) 转发到具体的服务实例。默认为false，设为true便开启通过服务中心的自动根据 serviceId 创建路由的功能。\n        locator:      #路由访问方式：http://Gateway_HOST:Gateway_PORT/大写的serviceId/**，其中微服务应用名默认大写访问。\n          enabled: true\n      routes:\n        - id: service-album\n          uri: lb://service-album\n          predicates:\n            - Path=/*/album/**\n        - id: service-user\n          uri: lb://service-user\n          predicates:\n            - Path=/*/user/**\n        - id: service-order\n          uri: lb://service-order\n          predicates:\n            - Path=/*/order/**\n        - id: service-live\n          uri: lb://service-live\n          predicates:\n            - Path=/*/live/**\n        - id: service-live-websocket\n          uri: lb:ws://service-live #ws://localhost:8507\n          predicates:\n            - Path=/websocket/**\n        - id: service-account\n          uri: lb://service-account\n          predicates:\n            - Path=/*/account/**\n        - id: service-comment\n          uri: lb://service-comment\n          predicates:\n            - Path=/*/comment/**\n        - id: service-dispatch\n          uri: lb://service-dispatch\n          predicates:\n            - Path=/*/dispatch/**\n        - id: service-payment\n          uri: lb://service-payment\n          predicates:\n            - Path=/*/payment/**\n        - id: service-system\n          uri: lb://service-system\n          predicates:\n            - Path=/*/system/**\n        - id: service-search\n          uri: lb://service-search\n          predicates:\n            - Path=/*/search/**\n        - id: service-search\n          uri: lb://service-system\n          predicates:\n            - Path=/*/system/**', 'f53ad797ed9933ef6c53440975227b4e', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 3, 'service-account-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8505\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_account?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\n', 'df0cc608ff2f1b2d09265e797b941288', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 4, 'service-album-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8501\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_album?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nminio:\n  endpointUrl: http://192.168.200.6:9000\n  accessKey: admin\n  secreKey: admin123456\n  bucketName: tingshu\nvod:\n  appId: 1255727855\n  secretId: AKIDTOFybJvQqCWnDdyDNRQJ54xkT8hBbxCK\n  secretKey: DKLH87saCmKZowS09IPRLE4pVCqQuKeu\n  region: ap-beijing\n  procedure: SimpleAesEncryptPreset #任务流\n  #tempPath: /root/tingshu/tempPath\n  tempPath: D:\\code\\workspace2023\\tingshu\\temp\n  playKey: wrTwwu8U3DRSRDgC8l7q  #播放加密key\n', '10cb8ad7c9892160a3faef1923c84cef', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 5, 'service-comment-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8508\nspring:\n  data:\n    mongodb:\n      host: 192.168.200.6\n      port: 27017\n      database: tingshu #指定操作的数据库\n', '90cdaed91defef147a9dbbe05328d9ae', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 6, 'service-dispatch-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8509\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_dispatch?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: 123456\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nxxl:\n  job:\n    admin:\n      # 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行\"执行器心跳注册\"和\"任务结果回调\"；为空则关闭自动注册\n      addresses: http://localhost:8080/xxl-job-admin\n      # 执行器通讯TOKEN [选填]：非空时启用\n    accessToken: default_token\n    executor:\n      # 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册\n      appname: dispatch-xxl-job-executor\n      # 执行器注册 [选填]：优先使用该配置作为注册地址，为空时使用内嵌服务 ”IP:PORT“ 作为注册地址。从而更灵活的支持容器类型执行器动态IP和动态映射端口问题。\n      address:\n      # 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 \"执行器注册\" 和 \"调度中心请求并触发任务\"；\n      ip:\n      # 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；\n      port: 9999\n      # 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；\n      logpath: /data/applogs/xxl-job/jobhandler\n      # 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；\n      logretentiondays: 30\n', 'd9736dcd67f850be9c6b11ef7beed77d', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 7, 'service-order-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8504\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_order?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\n', 'f16e0475d8dbd63b2f4629ac43ada108', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 8, 'service-payment-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8506\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_payment?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nwechat:\n  pay:\n    #小程序微信公众平台appId，不是微信服务号appId，与小程序授权登录appId一致\n    appid: wxcc651fcbab275e33\n    mchid: 1481962542\n    appkey: MXb72b9RfshXZD4FRGV5KLqmv5bx9LT9\n    #微信商户平台配置的，其他项目使用，不可更改\n    notifyUrl: http://gmall-prod.atguigu.cn/api/payment/weixin/notify\n', '0a2481971704bf7c6c7b731ff8d38abe', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 9, 'service-search-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8502\nspring:\n  elasticsearch:\n      uris: http://192.168.200.6:9200\n      username: elastic\n      password: 111111\n', 'cf045bfd3addf1d7168ddf57cabe3101', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (0, 10, 'service-user-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8503\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_user?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nwechat:\n  login:\n    #小程序授权登录\n    appId: wxcc651fcbab275e33  # 小程序微信公众平台appId\n    appSecret: 5f353399a2eae7ff6ceda383e924c5f6  # 小程序微信公众平台api秘钥\n', '82fd6048e310a8839f88d4a3679805ac', '2023-10-09 12:04:02', '2023-10-09 20:04:03', NULL, '192.168.200.1', 'I', '', '');
INSERT INTO `his_config_info` VALUES (10, 11, 'service-user-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8503\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_user?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: root\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nwechat:\n  login:\n    #小程序授权登录\n    appId: wxcc651fcbab275e33  # 小程序微信公众平台appId\n    appSecret: 5f353399a2eae7ff6ceda383e924c5f6  # 小程序微信公众平台api秘钥\n', '82fd6048e310a8839f88d4a3679805ac', '2023-10-09 12:51:53', '2023-10-09 20:51:53', 'nacos', '192.168.200.1', 'U', '', '');
INSERT INTO `his_config_info` VALUES (1, 12, 'common.yaml', 'DEFAULT_GROUP', '', 'mybatis-plus:\n  configuration:\n    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl\n  mapper-locations: classpath:mapper/*Mapper.xml\nfeign:\n  sentinel:\n    enabled: true\n  client:\n    config:\n      default:\n        readTimeout: 3000\n        connectTimeout: 1000\nspring:\n  main:\n    allow-bean-definition-overriding: true #当遇到同样Bean名字的时候，是否允许覆盖注册\n  cloud:\n    sentinel:\n      transport:\n        dashboard: 192.168.200.6:8858\n    openfeign:\n      lazy-attributes-resolution: true #开启懒加载，否则启动报错\n      client:\n        config:\n          default:\n            connectTimeout: 30000\n            readTimeout: 30000\n            loggerLevel: basic\n  zipkin:\n    base-url: http://192.168.200.6:9411\n    discovery-client-enabled: false\n    sender:\n      type: web\n  kafka:\n    bootstrap-servers: 192.168.200.6:9092\n    producer:\n      retries: 3  #设置大于0的值，则客户端会将发送失败的记录重新发送\n      acks: 1\n      batch-size: 16384\n      buffer-memory: 33554432\n      key-serializer: org.apache.kafka.common.serialization.StringSerializer\n      value-serializer: org.apache.kafka.common.serialization.StringSerializer\n    consumer:\n      group-id: ${spring.application.name}\n      enable-auto-commit: true\n      auto-offset-reset: earliest\n      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n  data:\n    redis:\n      host: 192.168.200.6\n      port: 6379\n      database: 0\n      timeout: 1800000\n      # password: #如果redis有密码需要设置\n      lettuce:\n        pool:\n          max-active: 20 #最大连接数\n          max-wait: -1    #最大阻塞等待时间(负数表示没限制)\n          max-idle: 5    #最大空闲\n          min-idle: 0     #最小空闲\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: GMT+8\n  servlet:\n    multipart:\n      max-file-size: 10MB     #单个文件最大限制\n      max-request-size: 20MB  #多个文件最大限制\nmanagement:\n  zipkin:\n    tracing:\n      endpoint: http://192.168.200.6:9411/api/v2/spans\n  tracing:\n    sampling:\n      probability: 1.0 # 记录速率100%', '2bb45c342849ed6cc21ab07e84d69546', '2023-10-10 14:07:24', '2023-10-10 22:07:25', 'nacos', '192.168.200.1', 'U', '', '');
INSERT INTO `his_config_info` VALUES (6, 13, 'service-dispatch-dev.yaml', 'DEFAULT_GROUP', '', 'server:\n  port: 8509\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://192.168.200.6:3306/tingshu_dispatch?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=true\n    username: root\n    password: 123456\n    hikari:\n      connection-test-query: SELECT 1\n      connection-timeout: 60000\n      idle-timeout: 500000\n      max-lifetime: 540000\n      maximum-pool-size: 10\n      minimum-idle: 5\n      pool-name: GuliHikariPool\nxxl:\n  job:\n    admin:\n      # 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行\"执行器心跳注册\"和\"任务结果回调\"；为空则关闭自动注册\n      addresses: http://localhost:8080/xxl-job-admin\n      # 执行器通讯TOKEN [选填]：非空时启用\n    accessToken: default_token\n    executor:\n      # 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册\n      appname: dispatch-xxl-job-executor\n      # 执行器注册 [选填]：优先使用该配置作为注册地址，为空时使用内嵌服务 ”IP:PORT“ 作为注册地址。从而更灵活的支持容器类型执行器动态IP和动态映射端口问题。\n      address:\n      # 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 \"执行器注册\" 和 \"调度中心请求并触发任务\"；\n      ip:\n      # 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；\n      port: 9999\n      # 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；\n      logpath: /data/applogs/xxl-job/jobhandler\n      # 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；\n      logretentiondays: 30\n', 'd9736dcd67f850be9c6b11ef7beed77d', '2023-10-13 12:23:16', '2023-10-13 20:23:16', 'nacos', '192.168.200.1', 'U', '', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE INDEX `uk_role_permission`(`role` ASC, `resource` ASC, `action` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE INDEX `idx_user_role`(`username` ASC, `role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '租户容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_info_kptenantid`(`kp` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'tenant_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);

SET FOREIGN_KEY_CHECKS = 1;
