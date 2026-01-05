package com.sales.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperties {
    
    private String zookeeperQuorum;
    
    private int zookeeperPropertyClientPort;
    
    private int masterPort;
    
    private int regionserverPort;
    
    public String getZookeeperQuorum() {
        return zookeeperQuorum;
    }
    
    public int getZookeeperPropertyClientPort() {
        return zookeeperPropertyClientPort;
    }
    
    public int getMasterPort() {
        return masterPort;
    }
    
    public int getRegionserverPort() {
        return regionserverPort;
    }
}
