package com.sales.service;

import com.sales.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SessionService {

    @Autowired
    private RedisService redisService;

    private static final long SESSION_EXPIRE_TIME = 1800; // 30分钟

    /**
     * 创建用户会话
     */
    public void createSession(String sessionId, String userId, String username, String loginIp) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        
        // 构建会话信息
        String sessionData = "{\"user_id\":\"" + userId + 
                           "\",\"username\":\"" + username + 
                           "\",\"login_time\":" + System.currentTimeMillis() / 1000 + 
                           ",\"login_ip\":\"" + loginIp + "\"}";
        
        redisService.set(sessionKey, sessionData, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        // 建立用户Token映射
        String tokenKey = RedisConfig.RedisKeys.TOKEN_PREFIX + userId;
        redisService.set(tokenKey, sessionId, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        // 添加到在线用户集合
        redisService.sadd(RedisConfig.RedisKeys.ONLINE_USERS, userId);
        redisService.expire(RedisConfig.RedisKeys.ONLINE_USERS, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        log.info("Session created: sessionId={}, userId={}, username={}", sessionId, userId, username);
    }

    /**
     * 获取会话信息
     */
    public String getSession(String sessionId) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        return (String) redisService.get(sessionKey);
    }

    /**
     * 验证会话是否有效
     */
    public boolean validateSession(String sessionId) {
        String sessionData = getSession(sessionId);
        if (sessionData == null) {
            return false;
        }
        
        // 延长会话过期时间
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        redisService.expire(sessionKey, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        return true;
    }

    /**
     * 销毁会话
     */
    public void destroySession(String sessionId) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        String sessionData = (String) redisService.get(sessionKey);
        
        if (sessionData != null) {
            // 解析用户ID
            String userId = extractUserId(sessionData);
            if (userId != null) {
                // 删除Token映射
                String tokenKey = RedisConfig.RedisKeys.TOKEN_PREFIX + userId;
                redisService.del(tokenKey);
                
                // 从在线用户集合中移除
                redisService.srem(RedisConfig.RedisKeys.ONLINE_USERS, userId);
            }
        }
        
        // 删除会话
        redisService.del(sessionKey);
        
        log.info("Session destroyed: sessionId={}", sessionId);
    }

    /**
     * 根据用户ID获取会话
     */
    public String getSessionByUserId(String userId) {
        String tokenKey = RedisConfig.RedisKeys.TOKEN_PREFIX + userId;
        return (String) redisService.get(tokenKey);
    }

    /**
     * 刷新会话过期时间
     */
    public void refreshSession(String sessionId) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        redisService.expire(sessionKey, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        // 同时刷新Token过期时间
        String sessionData = getSession(sessionId);
        if (sessionData != null) {
            String userId = extractUserId(sessionData);
            if (userId != null) {
                String tokenKey = RedisConfig.RedisKeys.TOKEN_PREFIX + userId;
                redisService.expire(tokenKey, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
            }
        }
        
        log.debug("Session refreshed: sessionId={}", sessionId);
    }

    /**
     * 获取在线用户数量
     */
    public long getOnlineUserCount() {
        Set<Object> onlineUsers = redisService.smembers(RedisConfig.RedisKeys.ONLINE_USERS);
        return onlineUsers != null ? onlineUsers.size() : 0;
    }

    /**
     * 获取所有在线用户
     */
    public Set<Object> getOnlineUsers() {
        return redisService.smembers(RedisConfig.RedisKeys.ONLINE_USERS);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        return redisService.sismember(RedisConfig.RedisKeys.ONLINE_USERS, userId);
    }

    /**
     * 强制用户下线
     */
    public void forceLogout(String userId) {
        String sessionId = getSessionByUserId(userId);
        if (sessionId != null) {
            destroySession(sessionId);
        }
        
        log.info("User forced logout: userId={}", userId);
    }

    /**
     * 更新会话信息
     */
    public void updateSession(String sessionId, Map<String, Object> updates) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        String sessionData = (String) redisService.get(sessionKey);
        
        if (sessionData != null) {
            // 这里应该解析JSON并更新，简化处理
            log.info("Session updated: sessionId={}, updates={}", sessionId, updates);
        }
    }

    /**
     * 获取会话剩余时间
     */
    public long getSessionTtl(String sessionId) {
        String sessionKey = RedisConfig.RedisKeys.SESSION_PREFIX + sessionId;
        return redisService.getExpire(sessionKey);
    }

    /**
     * 批量销毁会话
     */
    public void batchDestroySessions(String... sessionIds) {
        for (String sessionId : sessionIds) {
            destroySession(sessionId);
        }
        
        log.info("Batch sessions destroyed: count={}", sessionIds.length);
    }

    /**
     * 清理过期会话（通常由Redis自动处理，这里提供手动清理接口）
     */
    public void cleanupExpiredSessions() {
        // Redis会自动清理过期的键，这里主要是记录日志
        log.info("Cleanup expired sessions task executed");
    }

    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats() {
        // 这里可以实现更复杂的统计逻辑
        long onlineCount = getOnlineUserCount();
        
        return Map.of(
            "onlineUserCount", onlineCount,
            "sessionExpireTime", SESSION_EXPIRE_TIME
        );
    }

    /**
     * 从会话数据中提取用户ID
     */
    private String extractUserId(String sessionData) {
        try {
            if (sessionData.contains("\"user_id\":")) {
                int start = sessionData.indexOf("\"user_id\":\"") + 11;
                int end = sessionData.indexOf("\"", start);
                if (end > start) {
                    return sessionData.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract user ID from session data: {}", sessionData, e);
        }
        return null;
    }

    /**
     * 从会话数据中提取用户名
     */
    private String extractUsername(String sessionData) {
        try {
            if (sessionData.contains("\"username\":")) {
                int start = sessionData.indexOf("\"username\":\"") + 12;
                int end = sessionData.indexOf("\"", start);
                if (end > start) {
                    return sessionData.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract username from session data: {}", sessionData, e);
        }
        return null;
    }

    /**
     * 从会话数据中提取登录时间
     */
    private Long extractLoginTime(String sessionData) {
        try {
            if (sessionData.contains("\"login_time\":")) {
                int start = sessionData.indexOf("\"login_time\":") + 13;
                int end = sessionData.indexOf(",", start);
                if (end == -1) {
                    end = sessionData.indexOf("}", start);
                }
                if (end > start) {
                    String timeStr = sessionData.substring(start, end);
                    return Long.parseLong(timeStr.trim());
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract login time from session data: {}", sessionData, e);
        }
        return null;
    }
}
