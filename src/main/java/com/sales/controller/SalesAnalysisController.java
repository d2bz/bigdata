package com.sales.controller;

import com.sales.service.SalesAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class SalesAnalysisController {

    @Autowired
    private SalesAnalysisService salesAnalysisService;

    /**
     * 获取实时销售看板数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<SalesAnalysisService.DashboardData> getDashboardData() {
        try {
            SalesAnalysisService.DashboardData dashboardData = salesAnalysisService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (IOException e) {
            log.error("Failed to get dashboard data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取日销售数据
     */
    @GetMapping("/daily/{date}")
    public ResponseEntity<com.sales.entity.SalesData> getDailySalesData(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            com.sales.entity.SalesData salesData = salesAnalysisService.getDailySalesData(date);
            if (salesData != null) {
                return ResponseEntity.ok(salesData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Failed to get daily sales data: date={}", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取日期范围内的销售数据
     */
    @GetMapping("/range")
    public ResponseEntity<List<com.sales.entity.SalesData>> getSalesDataByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<com.sales.entity.SalesData> salesDataList = salesAnalysisService.getSalesDataByDateRange(startDate, endDate);
            return ResponseEntity.ok(salesDataList);
        } catch (IOException e) {
            log.error("Failed to get sales data by date range: {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品销售数据
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<com.sales.entity.SalesData>> getProductSalesData(
            @PathVariable String productId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<com.sales.entity.SalesData> salesDataList = salesAnalysisService.getProductSalesData(productId, startDate, endDate);
            return ResponseEntity.ok(salesDataList);
        } catch (IOException e) {
            log.error("Failed to get product sales data: productId={}, {} to {}", productId, startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取品类销售数据
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<com.sales.entity.SalesData>> getCategorySalesData(
            @PathVariable String categoryId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<com.sales.entity.SalesData> salesDataList = salesAnalysisService.getCategorySalesData(categoryId, startDate, endDate);
            return ResponseEntity.ok(salesDataList);
        } catch (IOException e) {
            log.error("Failed to get category sales data: categoryId={}, {} to {}", categoryId, startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热销商品排行
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<com.sales.entity.SalesData>> getTopSellingProducts(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<com.sales.entity.SalesData> topProducts = salesAnalysisService.getTopSellingProducts(date, limit);
            return ResponseEntity.ok(topProducts);
        } catch (IOException e) {
            log.error("Failed to get top selling products: date={}, limit={}", date, limit, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取销售趋势数据
     */
    @GetMapping("/trend")
    public ResponseEntity<List<SalesAnalysisService.TrendData>> getSalesTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<SalesAnalysisService.TrendData> trendData = salesAnalysisService.getSalesTrend(startDate, endDate);
            return ResponseEntity.ok(trendData);
        } catch (IOException e) {
            log.error("Failed to get sales trend: {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取品类销售分析
     */
    @GetMapping("/category-analysis")
    public ResponseEntity<List<SalesAnalysisService.CategoryAnalysis>> getCategoryAnalysis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            List<SalesAnalysisService.CategoryAnalysis> categoryAnalysis = salesAnalysisService.getCategoryAnalysis(date);
            return ResponseEntity.ok(categoryAnalysis);
        } catch (IOException e) {
            log.error("Failed to get category analysis: date={}", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取区域销售分析
     */
    @GetMapping("/region-analysis")
    public ResponseEntity<Map<String, Long>> getRegionAnalysis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            Map<String, Long> regionAnalysis = salesAnalysisService.getRegionAnalysis(date);
            return ResponseEntity.ok(regionAnalysis);
        } catch (IOException e) {
            log.error("Failed to get region analysis: date={}", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取小时销售分析
     */
    @GetMapping("/hourly-analysis")
    public ResponseEntity<Map<Integer, Long>> getHourlyAnalysis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            Map<Integer, Long> hourlyAnalysis = salesAnalysisService.getHourlyAnalysis(date);
            return ResponseEntity.ok(hourlyAnalysis);
        } catch (IOException e) {
            log.error("Failed to get hourly analysis: date={}", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成销售报表
     */
    @GetMapping("/report")
    public ResponseEntity<SalesAnalysisService.SalesReport> generateSalesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            SalesAnalysisService.SalesReport report = salesAnalysisService.generateSalesReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IOException e) {
            log.error("Failed to generate sales report: {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 记录销售数据（内部接口）
     */
    @PostMapping("/record-sales")
    public ResponseEntity<Void> recordSales(
            @RequestParam String productId,
            @RequestParam String categoryId,
            @RequestParam Long quantity,
            @RequestParam java.math.BigDecimal amount) {
        try {
            salesAnalysisService.recordSales(productId, categoryId, quantity, amount);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to record sales: productId={}, quantity={}, amount={}", productId, quantity, amount, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 记录退货数据（内部接口）
     */
    @PostMapping("/record-refund")
    public ResponseEntity<Void> recordRefund(
            @RequestParam String productId,
            @RequestParam String categoryId,
            @RequestParam Long quantity,
            @RequestParam java.math.BigDecimal amount) {
        try {
            salesAnalysisService.recordRefund(productId, categoryId, quantity, amount);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to record refund: productId={}, quantity={}, amount={}", productId, quantity, amount, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
