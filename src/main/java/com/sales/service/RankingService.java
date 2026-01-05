package com.sales.service;

import com.sales.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RankingService {

    @Autowired
    private RedisService redisService;

    private static final long RANKING_EXPIRE_TIME = 86400; // 24小时

    /**
     * 增加商品销售分数
     */
    public void addSalesScore(String productId, double score) {
        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        redisService.zadd(rankKey, score, productId);
        
        // 设置过期时间
        redisService.expire(rankKey, RANKING_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        
        log.info("Added sales score: productId={}, score={}", productId, score);
    }

    /**
     * 增加周销售分数
     */
    public void addWeeklySalesScore(String productId, double score) {
        String rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
        redisService.zadd(rankKey, score, productId);
        
        // 设置过期时间（7天）
        redisService.expire(rankKey, 7 * RANKING_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        
        log.info("Added weekly sales score: productId={}, score={}", productId, score);
    }

    /**
     * 增加月销售分数
     */
    public void addMonthlySalesScore(String productId, double score) {
        String rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
        redisService.zadd(rankKey, score, productId);
        
        // 设置过期时间（30天）
        redisService.expire(rankKey, 30 * RANKING_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        
        log.info("Added monthly sales score: productId={}, score={}", productId, score);
    }

    /**
     * 获取日销售排行榜
     */
    public Set<Object> getDailySalesRanking(int limit) {
        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        return redisService.zrevrange(rankKey, 0, limit - 1);
    }

    /**
     * 获取周销售排行榜
     */
    public Set<Object> getWeeklySalesRanking(int limit) {
        String rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
        return redisService.zrevrange(rankKey, 0, limit - 1);
    }

    /**
     * 获取月销售排行榜
     */
    public Set<Object> getMonthlySalesRanking(int limit) {
        String rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
        return redisService.zrevrange(rankKey, 0, limit - 1);
    }

    /**
     * 获取商品在日排行榜中的排名
     */
    public Long getDailyRank(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        Long rank = redisService.zrevrank(rankKey, productId);
        return rank != null ? rank + 1 : null; // 排名从1开始
    }

    /**
     * 获取商品在周排行榜中的排名
     */
    public Long getWeeklyRank(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
        Long rank = redisService.zrevrank(rankKey, productId);
        return rank != null ? rank + 1 : null;
    }

    /**
     * 获取商品在月排行榜中的排名
     */
    public Long getMonthlyRank(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
        Long rank = redisService.zrevrank(rankKey, productId);
        return rank != null ? rank + 1 : null;
    }

    /**
     * 获取商品日销售分数
     */
    public Double getDailyScore(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        return redisService.zscore(rankKey, productId);
    }

    /**
     * 获取商品周销售分数
     */
    public Double getWeeklyScore(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
        return redisService.zscore(rankKey, productId);
    }

    /**
     * 获取商品月销售分数
     */
    public Double getMonthlyScore(String productId) {
        String rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
        return redisService.zscore(rankKey, productId);
    }

    /**
     * 增加热门商品分数
     */
    public void addHotProductScore(String productId, double score) {
        String hotKey = RedisConfig.RedisKeys.HOT_PRODUCTS;
        redisService.zincrby(hotKey, score, productId);
        
        // 设置过期时间
        redisService.expire(hotKey, RANKING_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        
        log.info("Added hot product score: productId={}, score={}", productId, score);
    }

    /**
     * 获取热门商品排行榜
     */
    public Set<Object> getHotProducts(int limit) {
        String hotKey = RedisConfig.RedisKeys.HOT_PRODUCTS;
        return redisService.zrevrange(hotKey, 0, limit - 1);
    }

    /**
     * 获取热门商品排名
     */
    public Long getHotProductRank(String productId) {
        String hotKey = RedisConfig.RedisKeys.HOT_PRODUCTS;
        Long rank = redisService.zrevrank(hotKey, productId);
        return rank != null ? rank + 1 : null;
    }

    /**
     * 增加商品浏览量分数
     */
    public void addViewScore(String productId) {
        addHotProductScore(productId, 1.0);
    }

    /**
     * 增加商品加购分数
     */
    public void addCartScore(String productId) {
        addHotProductScore(productId, 2.0);
    }

    /**
     * 增加商品购买分数
     */
    public void addPurchaseScore(String productId, double amount) {
        addHotProductScore(productId, amount / 100.0); // 每100元1分
    }

    /**
     * 清除日排行榜
     */
    public void clearDailyRanking() {
        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        redisService.del(rankKey);
        log.info("Cleared daily sales ranking");
    }

    /**
     * 清除周排行榜
     */
    public void clearWeeklyRanking() {
        String rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
        redisService.del(rankKey);
        log.info("Cleared weekly sales ranking");
    }

    /**
     * 清除月排行榜
     */
    public void clearMonthlyRanking() {
        String rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
        redisService.del(rankKey);
        log.info("Cleared monthly sales ranking");
    }

    /**
     * 清除热门商品排行榜
     */
    public void clearHotProducts() {
        String hotKey = RedisConfig.RedisKeys.HOT_PRODUCTS;
        redisService.del(hotKey);
        log.info("Cleared hot products ranking");
    }

    /**
     * 批量增加销售分数
     */
    public void batchAddSalesScore(List<String> productIds, List<Double> scores) {
        if (productIds.size() != scores.size()) {
            log.error("Product IDs and scores size mismatch");
            return;
        }

        String rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
        for (int i = 0; i < productIds.size(); i++) {
            redisService.zadd(rankKey, scores.get(i), productIds.get(i));
        }
        
        redisService.expire(rankKey, RANKING_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        
        log.info("Batch added sales scores: count={}", productIds.size());
    }

    /**
     * 获取排行榜统计信息
     */
    public long getRankingSize(String rankingType) {
        String rankKey;
        switch (rankingType.toLowerCase()) {
            case "daily":
                rankKey = RedisConfig.RedisKeys.RANK_DAILY_SALE;
                break;
            case "weekly":
                rankKey = RedisConfig.RedisKeys.RANK_WEEKLY_SALE;
                break;
            case "monthly":
                rankKey = RedisConfig.RedisKeys.RANK_MONTHLY_SALE;
                break;
            case "hot":
                rankKey = RedisConfig.RedisKeys.HOT_PRODUCTS;
                break;
            default:
                log.error("Invalid ranking type: {}", rankingType);
                return 0;
        }
        
        return redisService.zcard(rankKey);
    }
}
