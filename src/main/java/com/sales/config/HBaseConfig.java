package com.sales.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "hbase.zookeeper.quorum", matchIfMissing = false)
@EnableConfigurationProperties(HBaseProperties.class)
public class HBaseConfig {

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum")
    public org.apache.hadoop.conf.Configuration hBaseConfiguration(HBaseProperties hBaseProperties) {
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        
        String zookeeperQuorum = hBaseProperties.getZookeeperQuorum();
        log.info("Reading HBase configuration - zookeeperQuorum: {}", zookeeperQuorum);
        
        if (zookeeperQuorum != null && !zookeeperQuorum.trim().isEmpty()) {
            config.set("hbase.zookeeper.quorum", zookeeperQuorum);
            log.info("Setting HBase zookeeper quorum to: {}", zookeeperQuorum);
        } else {
            log.warn("HBase zookeeper quorum is null or empty, skipping configuration");
        }
        
        int clientPort = hBaseProperties.getZookeeperPropertyClientPort();
        if (clientPort > 0) {
            config.set("hbase.zookeeper.property.clientPort", String.valueOf(clientPort));
            log.info("Setting HBase zookeeper client port to: {}", clientPort);
        }
        
        int masterPort = hBaseProperties.getMasterPort();
        if (masterPort > 0) {
            config.set("hbase.master.port", String.valueOf(masterPort));
            log.info("Setting HBase master port to: {}", masterPort);
        }
        
        int regionServerPort = hBaseProperties.getRegionserverPort();
        if (regionServerPort > 0) {
            config.set("hbase.regionserver.port", String.valueOf(regionServerPort));
            log.info("Setting HBase regionserver port to: {}", regionServerPort);
        }
        
        config.set("hbase.client.ipc.pool.type", "thread-local");
        config.set("hbase.client.ipc.pool.size", "10");
        config.set("hbase.client.pause", "100");
        config.set("hbase.client.retries.number", "3");
        config.set("hbase.client.scanner.timeout.period", "60000");
        config.set("hbase.rpc.timeout", "60000");
        
        // 添加连接超时配置
        config.set("zookeeper.session.timeout", "90000");
        config.set("zookeeper.connection.timeout", "15000");
        
        log.info("HBase configuration initialized with zookeeper: {}", zookeeperQuorum);
        return config;
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum")
    public Connection hBaseConnection(HBaseProperties hBaseProperties) {
        try {
            org.apache.hadoop.conf.Configuration config = hBaseConfiguration(hBaseProperties);
            Connection connection = ConnectionFactory.createConnection(config);
            log.info("HBase connection established successfully");
            return connection;
        } catch (Exception e) {
            log.error("Failed to establish HBase connection. Application will continue in Redis-only mode.", e);
            // 返回null而不是抛出异常，让应用继续运行
            return null;
        }
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum")
    public ExecutorService hBaseExecutorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum")
    public Admin hBaseAdmin(HBaseProperties hBaseProperties) {
        try {
            Connection connection = hBaseConnection(hBaseProperties);
            if (connection == null) {
                log.warn("HBase connection is null, skipping Admin initialization");
                return null;
            }
            Admin admin = connection.getAdmin();
            log.info("HBase admin initialized successfully");
            return admin;
        } catch (Exception e) {
            log.error("Failed to initialize HBase admin", e);
            return null;
        }
    }

    // 表名常量
    public static class TableNames {
        public static final TableName PRODUCT_INFO = TableName.valueOf("product_info");
        public static final TableName ORDER_HISTORY = TableName.valueOf("order_history");
        public static final TableName USER_PROFILE = TableName.valueOf("user_profile");
        public static final TableName SALES_DATA = TableName.valueOf("sales_data");
    }

    // 列族常量
    public static class ColumnFamilies {
        public static final String CF_BASE = "cf_base";
        public static final String CF_DETAIL = "cf_detail";
        public static final String CF_STOCK = "cf_stock";
        public static final String CF_STAT = "cf_stat";
        public static final String CF_ADDRESS = "cf_address";
        public static final String CF_ITEMS = "cf_items";
        public static final String CF_LOGISTICS = "cf_logistics";
        public static final String CF_ACCOUNT = "cf_account";
        public static final String CF_BEHAVIOR = "cf_behavior";
        public static final String CF_DAILY = "cf_daily";
        public static final String CF_HOURLY = "cf_hourly";
        public static final String CF_REGION = "cf_region";
    }

    // 列名常量
    public static class Columns {
        // 商品基本信息
        public static final String PRODUCT_NAME = "name";
        public static final String PRODUCT_CATEGORY = "category";
        public static final String PRODUCT_BRAND = "brand";
        public static final String PRODUCT_PRICE = "price";
        public static final String PRODUCT_COST = "cost";
        public static final String PRODUCT_STATUS = "status";
        public static final String PRODUCT_CREATE_TIME = "create_time";
        
        // 商品详细信息
        public static final String PRODUCT_DESCRIPTION = "description";
        public static final String PRODUCT_SPEC = "spec";
        public static final String PRODUCT_IMAGES = "images";
        public static final String PRODUCT_TAGS = "tags";
        
        // 库存信息
        public static final String PRODUCT_TOTAL_STOCK = "total_stock";
        public static final String PRODUCT_WAREHOUSE_STOCK = "warehouse_stock";
        public static final String PRODUCT_SAFE_STOCK = "safe_stock";
        public static final String PRODUCT_LOCK_STOCK = "lock_stock";
        
        // 统计信息
        public static final String PRODUCT_VIEW_COUNT = "view_count";
        public static final String PRODUCT_SALE_COUNT = "sale_count";
        public static final String PRODUCT_COLLECT_COUNT = "collect_count";
        public static final String PRODUCT_UPDATE_TIME = "update_time";
        
        // 订单基本信息
        public static final String ORDER_USER_ID = "user_id";
        public static final String ORDER_TOTAL_AMOUNT = "total_amount";
        public static final String ORDER_DISCOUNT_AMOUNT = "discount_amount";
        public static final String ORDER_ACTUAL_AMOUNT = "actual_amount";
        public static final String ORDER_STATUS = "status";
        public static final String ORDER_PAY_METHOD = "pay_method";
        public static final String ORDER_CREATE_TIME = "create_time";
        public static final String ORDER_PAY_TIME = "pay_time";
        public static final String ORDER_DELIVER_TIME = "deliver_time";
        public static final String ORDER_COMPLETE_TIME = "complete_time";
        
        // 收货信息
        public static final String ORDER_RECEIVER = "receiver";
        public static final String ORDER_PHONE = "phone";
        public static final String ORDER_ADDRESS = "address";
        public static final String ORDER_POSTCODE = "postcode";
        
        // 物流信息
        public static final String ORDER_EXPRESS_COMPANY = "express_company";
        public static final String ORDER_EXPRESS_NO = "express_no";
        public static final String ORDER_LOGISTICS_INFO = "logistics_info";
        
        // 用户基本信息
        public static final String USER_USERNAME = "username";
        public static final String USER_NICKNAME = "nickname";
        public static final String USER_PHONE = "phone";
        public static final String USER_EMAIL = "email";
        public static final String USER_GENDER = "gender";
        public static final String USER_BIRTHDAY = "birthday";
        public static final String USER_REGISTER_TIME = "register_time";
        public static final String USER_STATUS = "status";
        
        // 用户账户信息
        public static final String USER_LEVEL = "level";
        public static final String USER_POINTS = "points";
        public static final String USER_BALANCE = "balance";
        public static final String USER_GROWTH_VALUE = "growth_value";
        
        // 用户行为信息
        public static final String USER_LAST_LOGIN = "last_login";
        public static final String USER_LAST_LOGIN_IP = "last_login_ip";
        public static final String USER_LOGIN_COUNT = "login_count";
        public static final String USER_TOTAL_ORDER_AMOUNT = "total_order_amount";
        
        // 销售数据
        public static final String SALES_DATE = "date";
        public static final String SALES_PRODUCT_ID = "product_id";
        public static final String SALES_CATEGORY_ID = "category_id";
        public static final String SALES_SALE_COUNT = "sale_count";
        public static final String SALES_SALE_AMOUNT = "sale_amount";
        public static final String SALES_REFUND_COUNT = "refund_count";
        public static final String SALES_REFUND_AMOUNT = "refund_amount";
    }
}
