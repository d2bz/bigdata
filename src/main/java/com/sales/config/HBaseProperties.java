package com.sales.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperties {

    private Zookeeper zookeeper;

    private Master master;

    private Regionserver regionserver;

    public String getZookeeperQuorum() {
        return zookeeper != null ? zookeeper.getQuorum() : null;
    }

    public int getZookeeperPropertyClientPort() {
        if (zookeeper == null || zookeeper.getProperty() == null) {
            return 0;
        }
        return zookeeper.getProperty().getClientPort();
    }

    public int getMasterPort() {
        return master != null ? master.getPort() : 0;
    }

    public int getRegionserverPort() {
        return regionserver != null ? regionserver.getPort() : 0;
    }

    @Data
    public static class Zookeeper {

        private String quorum;

        private Property property;

        @Data
        public static class Property {

            private int clientPort;
        }
    }

    @Data
    public static class Master {

        private int port;
    }

    @Data
    public static class Regionserver {

        private int port;
    }
}
