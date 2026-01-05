package com.sales.controller;

import com.sales.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * 增加商品销售分数
     */
    @PostMapping("/sales/add")
    public ResponseEntity<Void> addSalesScore(
            @RequestParam String productId,
            @RequestParam double score) {
        try {
            rankingService.addSalesScore(productId, score);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add sales score: productId={}, score={}", productId, score, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加周销售分数
     */
    @PostMapping("/weekly/add")
    public ResponseEntity<Void> addWeeklySalesScore(
            @RequestParam String productId,
            @RequestParam double score) {
        try {
            rankingService.addWeeklySalesScore(productId, score);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add weekly sales score: productId={}, score={}", productId, score, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加月销售分数
     */
    @PostMapping("/monthly/add")
    public ResponseEntity<Void> addMonthlySalesScore(
            @RequestParam String productId,
            @RequestParam double score) {
        try {
            rankingService.addMonthlySalesScore(productId, score);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add monthly sales score: productId={}, score={}", productId, score, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取日销售排行榜
     */
    @GetMapping("/daily")
    public ResponseEntity<Set<Object>> getDailySalesRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Set<Object> ranking = rankingService.getDailySalesRanking(limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Failed to get daily sales ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取周销售排行榜
     */
    @GetMapping("/weekly")
    public ResponseEntity<Set<Object>> getWeeklySalesRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Set<Object> ranking = rankingService.getWeeklySalesRanking(limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Failed to get weekly sales ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取月销售排行榜
     */
    @GetMapping("/monthly")
    public ResponseEntity<Set<Object>> getMonthlySalesRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Set<Object> ranking = rankingService.getMonthlySalesRanking(limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Failed to get monthly sales ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品在日排行榜中的排名
     */
    @GetMapping("/daily/{productId}/rank")
    public ResponseEntity<Long> getDailyRank(@PathVariable String productId) {
        try {
            Long rank = rankingService.getDailyRank(productId);
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            log.error("Failed to get daily rank: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品在周排行榜中的排名
     */
    @GetMapping("/weekly/{productId}/rank")
    public ResponseEntity<Long> getWeeklyRank(@PathVariable String productId) {
        try {
            Long rank = rankingService.getWeeklyRank(productId);
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            log.error("Failed to get weekly rank: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品在月排行榜中的排名
     */
    @GetMapping("/monthly/{productId}/rank")
    public ResponseEntity<Long> getMonthlyRank(@PathVariable String productId) {
        try {
            Long rank = rankingService.getMonthlyRank(productId);
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            log.error("Failed to get monthly rank: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品日销售分数
     */
    @GetMapping("/daily/{productId}/score")
    public ResponseEntity<Double> getDailyScore(@PathVariable String productId) {
        try {
            Double score = rankingService.getDailyScore(productId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            log.error("Failed to get daily score: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品周销售分数
     */
    @GetMapping("/weekly/{productId}/score")
    public ResponseEntity<Double> getWeeklyScore(@PathVariable String productId) {
        try {
            Double score = rankingService.getWeeklyScore(productId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            log.error("Failed to get weekly score: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品月销售分数
     */
    @GetMapping("/monthly/{productId}/score")
    public ResponseEntity<Double> getMonthlyScore(@PathVariable String productId) {
        try {
            Double score = rankingService.getMonthlyScore(productId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            log.error("Failed to get monthly score: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热门商品排行榜
     */
    @GetMapping("/hot")
    public ResponseEntity<Set<Object>> getHotProducts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Set<Object> hotProducts = rankingService.getHotProducts(limit);
            return ResponseEntity.ok(hotProducts);
        } catch (Exception e) {
            log.error("Failed to get hot products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热门商品排名
     */
    @GetMapping("/hot/{productId}/rank")
    public ResponseEntity<Long> getHotProductRank(@PathVariable String productId) {
        try {
            Long rank = rankingService.getHotProductRank(productId);
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            log.error("Failed to get hot product rank: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加商品浏览量分数
     */
    @PostMapping("/{productId}/view")
    public ResponseEntity<Void> addViewScore(@PathVariable String productId) {
        try {
            rankingService.addViewScore(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add view score: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加商品加购分数
     */
    @PostMapping("/{productId}/cart")
    public ResponseEntity<Void> addCartScore(@PathVariable String productId) {
        try {
            rankingService.addCartScore(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add cart score: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加商品购买分数
     */
    @PostMapping("/{productId}/purchase")
    public ResponseEntity<Void> addPurchaseScore(
            @PathVariable String productId,
            @RequestParam double amount) {
        try {
            rankingService.addPurchaseScore(productId, amount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add purchase score: productId={}, amount={}", productId, amount, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除日排行榜
     */
    @DeleteMapping("/daily")
    public ResponseEntity<Void> clearDailyRanking() {
        try {
            rankingService.clearDailyRanking();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear daily ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除周排行榜
     */
    @DeleteMapping("/weekly")
    public ResponseEntity<Void> clearWeeklyRanking() {
        try {
            rankingService.clearWeeklyRanking();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear weekly ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除月排行榜
     */
    @DeleteMapping("/monthly")
    public ResponseEntity<Void> clearMonthlyRanking() {
        try {
            rankingService.clearMonthlyRanking();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear monthly ranking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清除热门商品排行榜
     */
    @DeleteMapping("/hot")
    public ResponseEntity<Void> clearHotProducts() {
        try {
            rankingService.clearHotProducts();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear hot products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量增加销售分数
     */
    @PostMapping("/sales/batch")
    public ResponseEntity<Void> batchAddSalesScore(
            @RequestBody List<String> productIds,
            @RequestBody List<Double> scores) {
        try {
            rankingService.batchAddSalesScore(productIds, scores);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to batch add sales scores", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取排行榜统计信息
     */
    @GetMapping("/stats/{rankingType}")
    public ResponseEntity<Long> getRankingSize(@PathVariable String rankingType) {
        try {
            long size = rankingService.getRankingSize(rankingType);
            return ResponseEntity.ok(size);
        } catch (Exception e) {
            log.error("Failed to get ranking size: type={}", rankingType, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
