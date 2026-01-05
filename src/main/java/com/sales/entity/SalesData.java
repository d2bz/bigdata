package com.sales.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String rowKey;            // 行键 (日期_商品ID 或 日期_品类ID)
    private LocalDate date;            // 日期
    
    // 每日销售数据
    private String productId;          // 商品ID
    private String categoryId;         // 品类ID
    private Long saleCount;            // 销售数量
    private BigDecimal saleAmount;     // 销售金额
    private Long refundCount;          // 退货数量
    private BigDecimal refundAmount;   // 退货金额
    
    // 小时级数据 (hour_00 到 hour_23)
    private Map<Integer, Long> hourlySales; // 小时销量
    
    // 区域销售数据
    private Map<String, Long> regionSales;  // 区域销量 {region: count}
    private Map<String, BigDecimal> regionAmounts; // 区域销售额 {region: amount}
    
    // 统计字段
    private Long totalViewCount;       // 总浏览数
    private Long totalCartCount;       // 总加购数
    private BigDecimal avgOrderAmount; // 平均订单金额
    private Double conversionRate;     // 转化率
    
    // 计算字段
    private BigDecimal netAmount;      // 净销售额 (销售金额 - 退货金额)
    private Long netCount;             // 净销售数量 (销售数量 - 退货数量)
    private BigDecimal refundRate;     // 退货率
    
    // 获取净销售额
    public BigDecimal getNetAmount() {
        if (saleAmount == null) {
            saleAmount = BigDecimal.ZERO;
        }
        if (refundAmount == null) {
            refundAmount = BigDecimal.ZERO;
        }
        return saleAmount.subtract(refundAmount);
    }
    
    // 获取净销售数量
    public Long getNetCount() {
        if (saleCount == null) {
            saleCount = 0L;
        }
        if (refundCount == null) {
            refundCount = 0L;
        }
        return saleCount - refundCount;
    }
    
    // 计算退货率
    public BigDecimal getRefundRate() {
        if (saleCount == null || saleCount == 0) {
            return BigDecimal.ZERO;
        }
        if (refundCount == null) {
            refundCount = 0L;
        }
        return new BigDecimal(refundCount)
                .divide(new BigDecimal(saleCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    // 获取最畅销的小时
    public Integer getBestHour() {
        if (hourlySales == null || hourlySales.isEmpty()) {
            return null;
        }
        
        return hourlySales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    // 获取最畅销的区域
    public String getBestRegion() {
        if (regionSales == null || regionSales.isEmpty()) {
            return null;
        }
        
        return regionSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    // 获取总销量
    public Long getTotalSales() {
        if (hourlySales == null) {
            return getNetCount();
        }
        
        return hourlySales.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }
    
    // 销售数据类型枚举
    public enum DataType {
        PRODUCT("product", "商品"),
        CATEGORY("category", "品类"),
        TOTAL("total", "总计");
        
        private final String code;
        private final String desc;
        
        DataType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static DataType fromCode(String code) {
            for (DataType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return PRODUCT;
        }
    }
    
    // 获取数据类型
    public DataType getDataType() {
        if (productId != null && !productId.isEmpty()) {
            return DataType.PRODUCT;
        } else if (categoryId != null && !categoryId.isEmpty()) {
            return DataType.CATEGORY;
        } else {
            return DataType.TOTAL;
        }
    }
    
    // 生成行键
    public static String generateRowKey(LocalDate date, String productId, String categoryId) {
        StringBuilder sb = new StringBuilder(date.toString());
        
        if (productId != null && !productId.isEmpty()) {
            sb.append("_").append(productId);
        } else if (categoryId != null && !categoryId.isEmpty()) {
            sb.append("_C").append(categoryId);
        } else {
            sb.append("_TOTAL");
        }
        
        return sb.toString();
    }
    
    // 解析行键
    public void parseRowKey(String rowKey) {
        this.rowKey = rowKey;
        
        String[] parts = rowKey.split("_");
        if (parts.length >= 1) {
            this.date = LocalDate.parse(parts[0]);
        }
        
        if (parts.length >= 2) {
            String identifier = parts[1];
            if (identifier.startsWith("C")) {
                this.categoryId = identifier.substring(1);
            } else if (!identifier.equals("TOTAL")) {
                this.productId = identifier;
            }
        }
    }
}
