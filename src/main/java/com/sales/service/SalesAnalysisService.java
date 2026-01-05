package com.sales.service;

import com.sales.entity.SalesData;
import com.sales.repository.SalesDataRepository;
import com.sales.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SalesAnalysisService {

    @Autowired
    private SalesDataRepository salesDataRepository;

    @Autowired
    private RankingService rankingService;

    /**
     * 记录销售数据
     */
    public void recordSales(String productId, String categoryId, Long quantity, BigDecimal amount) throws IOException {
        LocalDate today = LocalDate.now();
        
        // 增加销售数据
        salesDataRepository.incrementSales(productId, categoryId, today, quantity, amount);
        
        // 增加日销售排行榜分数
        rankingService.addSalesScore(productId, amount.doubleValue());
        
        // 增加热门商品分数
        rankingService.addPurchaseScore(productId, amount.doubleValue());
        
        // 记录小时级销售数据
        int currentHour = LocalDateTime.now().getHour();
        salesDataRepository.incrementHourlySales(productId, today, currentHour, quantity);
        
        log.info("Sales recorded: productId={}, quantity={}, amount={}", productId, quantity, amount);
    }

    /**
     * 记录退货数据
     */
    public void recordRefund(String productId, String categoryId, Long quantity, BigDecimal amount) throws IOException {
        LocalDate today = LocalDate.now();
        
        // 增加退货数据
        salesDataRepository.incrementRefund(productId, categoryId, today, quantity, amount);
        
        log.info("Refund recorded: productId={}, quantity={}, amount={}", productId, quantity, amount);
    }

    /**
     * 获取日销售数据
     */
    public SalesData getDailySalesData(LocalDate date) throws IOException {
        return salesDataRepository.findDailyTotal(date);
    }

    /**
     * 获取日期范围内的销售数据
     */
    public List<SalesData> getSalesDataByDateRange(LocalDate startDate, LocalDate endDate) throws IOException {
        return salesDataRepository.findByDateRange(startDate, endDate);
    }

    /**
     * 获取商品销售数据
     */
    public List<SalesData> getProductSalesData(String productId, LocalDate startDate, LocalDate endDate) throws IOException {
        return salesDataRepository.findByProductAndDateRange(productId, startDate, endDate);
    }

    /**
     * 获取品类销售数据
     */
    public List<SalesData> getCategorySalesData(String categoryId, LocalDate startDate, LocalDate endDate) throws IOException {
        return salesDataRepository.findByCategoryAndDateRange(categoryId, startDate, endDate);
    }

    /**
     * 获取热销商品排行
     */
    public List<SalesData> getTopSellingProducts(LocalDate date, int limit) throws IOException {
        return salesDataRepository.findTopSellingProducts(date, limit);
    }

    /**
     * 获取实时销售看板数据
     */
    public DashboardData getDashboardData() throws IOException {
        LocalDate today = LocalDate.now();
        SalesData dailySales = getDailySalesData(today);
        
        if (dailySales == null) {
            // 如果没有今日数据，返回默认值
            return DashboardData.builder()
                    .totalAmount(BigDecimal.ZERO)
                    .orderCount(0L)
                    .userCount(0L)
                    .avgPrice(BigDecimal.ZERO)
                    .build();
        }

        // 计算平均订单金额
        BigDecimal avgPrice = dailySales.getSaleCount() > 0 ? 
                dailySales.getSaleAmount().divide(new BigDecimal(dailySales.getSaleCount()), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;

        return DashboardData.builder()
                .totalAmount(dailySales.getNetAmount())
                .orderCount(dailySales.getNetCount())
                .userCount(getTodayUserCount())
                .avgPrice(avgPrice)
                .build();
    }

    /**
     * 获取销售趋势数据
     */
    public List<TrendData> getSalesTrend(LocalDate startDate, LocalDate endDate) throws IOException {
        List<SalesData> salesDataList = getSalesDataByDateRange(startDate, endDate);
        
        return salesDataList.stream()
                .filter(data -> data.getProductId() == null || data.getProductId().isEmpty()) // 只取总计数据
                .map(data -> TrendData.builder()
                        .date(data.getDate())
                        .amount(data.getNetAmount())
                        .count(data.getNetCount())
                        .build())
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .toList();
    }

    /**
     * 获取品类销售分析
     */
    public List<CategoryAnalysis> getCategoryAnalysis(LocalDate date) throws IOException {
        List<SalesData> salesDataList = salesDataRepository.findByDate(date);
        
        Map<String, CategoryAnalysis> categoryMap = new HashMap<>();
        
        for (SalesData data : salesDataList) {
            if (data.getCategoryId() != null && !data.getCategoryId().isEmpty()) {
                CategoryAnalysis analysis = categoryMap.computeIfAbsent(data.getCategoryId(), 
                        k -> CategoryAnalysis.builder()
                                .categoryId(k)
                                .totalAmount(BigDecimal.ZERO)
                                .totalCount(0L)
                                .build());
                
                analysis.setTotalAmount(analysis.getTotalAmount().add(data.getNetAmount()));
                analysis.setTotalCount(analysis.getTotalCount() + data.getNetCount());
            }
        }
        
        return categoryMap.values().stream()
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .toList();
    }

    /**
     * 获取区域销售分析
     */
    public Map<String, Long> getRegionAnalysis(LocalDate date) throws IOException {
        SalesData dailySales = getDailySalesData(date);
        
        if (dailySales != null && dailySales.getRegionSales() != null) {
            return dailySales.getRegionSales();
        }
        
        return new HashMap<>();
    }

    /**
     * 获取小时销售分析
     */
    public Map<Integer, Long> getHourlyAnalysis(LocalDate date) throws IOException {
        SalesData dailySales = getDailySalesData(date);
        
        if (dailySales != null && dailySales.getHourlySales() != null) {
            return dailySales.getHourlySales();
        }
        
        return new HashMap<>();
    }

    /**
     * 生成销售报表
     */
    public SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) throws IOException {
        List<SalesData> salesDataList = getSalesDataByDateRange(startDate, endDate);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long totalCount = 0L;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        Long totalRefundCount = 0L;
        
        for (SalesData data : salesDataList) {
            if (data.getProductId() == null || data.getProductId().isEmpty()) { // 只统计总计数据
                totalAmount = totalAmount.add(data.getNetAmount());
                totalCount = totalCount + data.getNetCount();
                totalRefundAmount = totalRefundAmount.add(data.getRefundAmount() != null ? data.getRefundAmount() : BigDecimal.ZERO);
                totalRefundCount = totalRefundCount + (data.getRefundCount() != null ? data.getRefundCount() : 0L);
            }
        }
        
        BigDecimal avgOrderAmount = totalCount > 0 ? 
                totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        BigDecimal refundRate = totalAmount.compareTo(BigDecimal.ZERO) > 0 ? 
                totalRefundAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) : 
                BigDecimal.ZERO;
        
        return SalesReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .totalRefundAmount(totalRefundAmount)
                .totalRefundCount(totalRefundCount)
                .avgOrderAmount(avgOrderAmount)
                .refundRate(refundRate)
                .build();
    }

    /**
     * 获取今日用户数量（简化实现）
     */
    private Long getTodayUserCount() {
        // 这里应该从Redis或数据库获取今日活跃用户数
        // 简化实现，返回在线用户数
        try {
            return rankingService.getRankingSize("hot");
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 看板数据
     */
    @lombok.Data
    @lombok.Builder
    public static class DashboardData {
        private BigDecimal totalAmount;
        private Long orderCount;
        private Long userCount;
        private BigDecimal avgPrice;
    }

    /**
     * 趋势数据
     */
    @lombok.Data
    @lombok.Builder
    public static class TrendData {
        private LocalDate date;
        private BigDecimal amount;
        private Long count;
    }

    /**
     * 品类分析
     */
    @lombok.Data
    @lombok.Builder
    public static class CategoryAnalysis {
        private String categoryId;
        private BigDecimal totalAmount;
        private Long totalCount;
    }

    /**
     * 销售报表
     */
    @lombok.Data
    @lombok.Builder
    public static class SalesReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalAmount;
        private Long totalCount;
        private BigDecimal totalRefundAmount;
        private Long totalRefundCount;
        private BigDecimal avgOrderAmount;
        private BigDecimal refundRate;
    }
}
