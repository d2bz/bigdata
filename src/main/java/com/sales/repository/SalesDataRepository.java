package com.sales.repository;

import com.sales.config.HBaseConfig;
import com.sales.entity.SalesData;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class SalesDataRepository extends BaseHBaseRepository {

    private static final TableName TABLE_NAME = HBaseConfig.TableNames.SALES_DATA;

    public void save(SalesData salesData) throws IOException {
        String rowKey = SalesData.generateRowKey(salesData.getDate(), salesData.getProductId(), salesData.getCategoryId());
        Put put = createPut(rowKey);
        
        // 每日销售数据
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_DATE, formatDate(salesData.getDate()));
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_PRODUCT_ID, salesData.getProductId());
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_CATEGORY_ID, salesData.getCategoryId());
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_SALE_COUNT, salesData.getSaleCount());
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_SALE_AMOUNT, 
                 salesData.getSaleAmount() != null ? salesData.getSaleAmount().doubleValue() : null);
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_REFUND_COUNT, salesData.getRefundCount());
        addColumn(put, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_REFUND_AMOUNT, 
                 salesData.getRefundAmount() != null ? salesData.getRefundAmount().doubleValue() : null);
        
        // 小时级数据
        if (salesData.getHourlySales() != null) {
            for (Map.Entry<Integer, Long> entry : salesData.getHourlySales().entrySet()) {
                String qualifier = "hour_" + String.format("%02d", entry.getKey());
                addColumn(put, HBaseConfig.ColumnFamilies.CF_HOURLY, qualifier, entry.getValue());
            }
        }
        
        // 区域销售数据
        if (salesData.getRegionSales() != null) {
            for (Map.Entry<String, Long> entry : salesData.getRegionSales().entrySet()) {
                String qualifier = "region_" + entry.getKey();
                addColumn(put, HBaseConfig.ColumnFamilies.CF_REGION, qualifier, entry.getValue());
            }
        }
        
        if (salesData.getRegionAmounts() != null) {
            for (Map.Entry<String, BigDecimal> entry : salesData.getRegionAmounts().entrySet()) {
                String qualifier = "region_amount_" + entry.getKey();
                addColumn(put, HBaseConfig.ColumnFamilies.CF_REGION, qualifier, 
                         entry.getValue() != null ? entry.getValue().doubleValue() : null);
            }
        }
        
        putData(TABLE_NAME, put);
        log.info("Sales data saved: {}", rowKey);
    }

    public SalesData findById(String rowKey) throws IOException {
        Get get = createGet(rowKey);
        Result result = getData(TABLE_NAME, get);
        
        if (result.isEmpty()) {
            return null;
        }
        
        return mapToSalesData(result);
    }

    public List<SalesData> findByDateRange(LocalDate startDate, LocalDate endDate) throws IOException {
        Scan scan = createScan();
        
        // 使用前缀过滤器扫描日期范围
        String startPrefix = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endPrefix = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        scan.setStartRow(Bytes.toBytes(startPrefix));
        scan.setStopRow(Bytes.toBytes(endPrefix));
        
        List<Result> results = scanData(TABLE_NAME, scan);
        List<SalesData> salesDataList = new ArrayList<>();
        
        for (Result result : results) {
            salesDataList.add(mapToSalesData(result));
        }
        
        return salesDataList;
    }

    public List<SalesData> findByDate(LocalDate date) throws IOException {
        String prefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Scan scan = createScan();
        scan.setFilter(new PrefixFilter(Bytes.toBytes(prefix)));
        
        List<Result> results = scanData(TABLE_NAME, scan);
        List<SalesData> salesDataList = new ArrayList<>();
        
        for (Result result : results) {
            salesDataList.add(mapToSalesData(result));
        }
        
        return salesDataList;
    }

    public List<SalesData> findByProductAndDateRange(String productId, LocalDate startDate, LocalDate endDate) throws IOException {
        Scan scan = createScan();
        
        // 构建行键前缀：日期_商品ID
        String startPrefix = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" + productId;
        String endPrefix = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" + productId;
        
        scan.setStartRow(Bytes.toBytes(startPrefix));
        scan.setStopRow(Bytes.toBytes(endPrefix));
        
        List<Result> results = scanData(TABLE_NAME, scan);
        List<SalesData> salesDataList = new ArrayList<>();
        
        for (Result result : results) {
            salesDataList.add(mapToSalesData(result));
        }
        
        return salesDataList;
    }

    public List<SalesData> findByCategoryAndDateRange(String categoryId, LocalDate startDate, LocalDate endDate) throws IOException {
        Scan scan = createScan();
        
        // 构建行键前缀：日期_C品类ID
        String startPrefix = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_C" + categoryId;
        String endPrefix = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_C" + categoryId;
        
        scan.setStartRow(Bytes.toBytes(startPrefix));
        scan.setStopRow(Bytes.toBytes(endPrefix));
        
        List<Result> results = scanData(TABLE_NAME, scan);
        List<SalesData> salesDataList = new ArrayList<>();
        
        for (Result result : results) {
            salesDataList.add(mapToSalesData(result));
        }
        
        return salesDataList;
    }

    public List<SalesData> findTopSellingProducts(LocalDate date, int limit) throws IOException {
        String prefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Scan scan = createScan();
        scan.setFilter(new PrefixFilter(Bytes.toBytes(prefix)));
        scan.setLimit(limit);
        
        List<Result> results = scanData(TABLE_NAME, scan);
        List<SalesData> salesDataList = new ArrayList<>();
        
        for (Result result : results) {
            SalesData salesData = mapToSalesData(result);
            // 只返回商品级别的数据
            if (salesData.getProductId() != null && !salesData.getProductId().isEmpty()) {
                salesDataList.add(salesData);
            }
        }
        
        // 按销售数量排序（这里需要在内存中排序，实际项目中可以考虑使用协处理器）
        salesDataList.sort((a, b) -> Long.compare(b.getSaleCount(), a.getSaleCount()));
        
        return salesDataList.subList(0, Math.min(limit, salesDataList.size()));
    }

    public SalesData findDailyTotal(LocalDate date) throws IOException {
        String rowKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_TOTAL";
        return findById(rowKey);
    }

    public void incrementSales(String productId, String categoryId, LocalDate date, 
                              Long quantity, BigDecimal amount) throws IOException {
        String rowKey = SalesData.generateRowKey(date, productId, categoryId);
        
        // 增加销售数量和金额
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_DAILY, 
                            HBaseConfig.Columns.SALES_SALE_COUNT, 
                            quantity != null ? quantity : 0L);
        
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_DAILY, 
                            HBaseConfig.Columns.SALES_SALE_AMOUNT, 
                            amount != null ? amount.longValue() : 0L);
        
        log.info("Sales incremented: {} -> {} units, {} amount", rowKey, quantity, amount);
    }

    public void incrementRefund(String productId, String categoryId, LocalDate date, 
                               Long quantity, BigDecimal amount) throws IOException {
        String rowKey = SalesData.generateRowKey(date, productId, categoryId);
        
        // 增加退货数量和金额
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_DAILY, 
                            HBaseConfig.Columns.SALES_REFUND_COUNT, 
                            quantity != null ? quantity : 0L);
        
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_DAILY, 
                            HBaseConfig.Columns.SALES_REFUND_AMOUNT, 
                            amount != null ? amount.longValue() : 0L);
        
        log.info("Refund incremented: {} -> {} units, {} amount", rowKey, quantity, amount);
    }

    public void incrementHourlySales(String productId, LocalDate date, Integer hour, Long quantity) throws IOException {
        String rowKey = SalesData.generateRowKey(date, productId, null);
        String qualifier = "hour_" + String.format("%02d", hour);
        
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_HOURLY, 
                            qualifier, 
                            quantity != null ? quantity : 0L);
        
        log.info("Hourly sales incremented: {} -> hour {}: {} units", rowKey, hour, quantity);
    }

    public void incrementRegionSales(String productId, LocalDate date, String region, Long quantity) throws IOException {
        String rowKey = SalesData.generateRowKey(date, productId, null);
        String qualifier = "region_" + region;
        
        incrementColumnValue(TABLE_NAME, rowKey, 
                            HBaseConfig.ColumnFamilies.CF_REGION, 
                            qualifier, 
                            quantity != null ? quantity : 0L);
        
        log.info("Region sales incremented: {} -> region {}: {} units", rowKey, region, quantity);
    }

    private SalesData mapToSalesData(Result result) {
        SalesData.SalesDataBuilder builder = SalesData.builder();
        
        String rowKey = Bytes.toString(result.getRow());
        builder.rowKey(rowKey);
        
        // 每日销售数据
        builder.productId(getString(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_PRODUCT_ID));
        builder.categoryId(getString(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_CATEGORY_ID));
        builder.saleCount(getLong(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_SALE_COUNT));
        builder.refundCount(getLong(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_REFUND_COUNT));
        
        Double saleAmount = getDouble(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_SALE_AMOUNT);
        if (saleAmount != null) {
            builder.saleAmount(BigDecimal.valueOf(saleAmount));
        }
        
        Double refundAmount = getDouble(result, HBaseConfig.ColumnFamilies.CF_DAILY, HBaseConfig.Columns.SALES_REFUND_AMOUNT);
        if (refundAmount != null) {
            builder.refundAmount(BigDecimal.valueOf(refundAmount));
        }
        
        // 小时级数据
        Map<Integer, Long> hourlySales = new java.util.HashMap<>();
        for (int i = 0; i < 24; i++) {
            String qualifier = "hour_" + String.format("%02d", i);
            Long value = getLong(result, HBaseConfig.ColumnFamilies.CF_HOURLY, qualifier);
            if (value != null && value > 0) {
                hourlySales.put(i, value);
            }
        }
        builder.hourlySales(hourlySales);
        
        // 区域销售数据
        Map<String, Long> regionSales = new java.util.HashMap<>();
        Map<String, BigDecimal> regionAmounts = new java.util.HashMap<>();
        
        // 扫描所有区域相关的列
        for (org.apache.hadoop.hbase.Cell cell : result.rawCells()) {
            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
            if (qualifier.startsWith("region_") && !qualifier.startsWith("region_amount_")) {
                String region = qualifier.substring(7); // 去掉"region_"前缀
                Long value = Bytes.toLong(CellUtil.cloneValue(cell));
                regionSales.put(region, value);
            } else if (qualifier.startsWith("region_amount_")) {
                String region = qualifier.substring(14); // 去掉"region_amount_"前缀
                Double value = Bytes.toDouble(CellUtil.cloneValue(cell));
                regionAmounts.put(region, BigDecimal.valueOf(value));
            }
        }
        
        builder.regionSales(regionSales);
        builder.regionAmounts(regionAmounts);
        
        // 构建对象并解析行键
        SalesData salesData = builder.build();
        salesData.parseRowKey(rowKey);
        
        return salesData;
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
