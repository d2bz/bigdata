package com.sales.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HBaseOptionalConfig {

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum", matchIfMissing = false)
    public String hBaseConnectionStatus() {
        log.info("HBase is configured and will be initialized");
        return "HBase Enabled";
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.zookeeper.quorum", matchIfMissing = true, havingValue = "")
    public String hBaseDisabledStatus() {
        log.info("HBase is not configured - running in Redis-only mode");
        return "HBase Disabled";
    }
}
