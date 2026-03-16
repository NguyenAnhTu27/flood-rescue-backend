package com.floodrescue.module.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptLimiter {

    private static class AttemptWindow {
        private int failures;
        private long windowStartEpochSeconds;
        private long blockedUntilEpochSeconds;
    }

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Value("${app.auth.login-rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.auth.login-rate-limit.window-seconds:900}")
    private int windowSeconds;

    @Value("${app.auth.login-rate-limit.block-seconds:900}")
    private int blockSeconds;

    @Value("${app.auth.login-rate-limit.max-tracked-keys:10000}")
    private int maxTrackedKeys;

    private void cleanupExpiredEntries(long nowEpochSeconds) {
        if (attempts.size() < maxTrackedKeys) {
            return;
        }
        attempts.entrySet().removeIf(entry -> {
            AttemptWindow state = entry.getValue();
            synchronized (state) {
                boolean windowExpired = state.windowStartEpochSeconds + windowSeconds < nowEpochSeconds;
                boolean blockExpired = state.blockedUntilEpochSeconds <= nowEpochSeconds;
                return windowExpired && blockExpired;
            }
        });
    }

    public boolean isBlocked(String key) {
        long now = Instant.now().getEpochSecond();
        cleanupExpiredEntries(now);

        AttemptWindow state = attempts.get(key);
        if (state == null) {
            return false;
        }

        synchronized (state) {
            if (state.blockedUntilEpochSeconds > now) {
                return true;
            }
            if (state.windowStartEpochSeconds + windowSeconds < now) {
                attempts.remove(key);
            }
            return false;
        }
    }

    public long getRetryAfterSeconds(String key) {
        AttemptWindow state = attempts.get(key);
        if (state == null) {
            return 0;
        }
        long now = Instant.now().getEpochSecond();
        synchronized (state) {
            return Math.max(0, state.blockedUntilEpochSeconds - now);
        }
    }

    public void recordFailure(String key) {
        long now = Instant.now().getEpochSecond();
        cleanupExpiredEntries(now);

        AttemptWindow state = attempts.computeIfAbsent(key, ignored -> {
            AttemptWindow created = new AttemptWindow();
            created.windowStartEpochSeconds = now;
            return created;
        });

        synchronized (state) {
            if (state.windowStartEpochSeconds + windowSeconds < now) {
                state.windowStartEpochSeconds = now;
                state.failures = 0;
                state.blockedUntilEpochSeconds = 0;
            }

            state.failures++;
            if (state.failures >= maxAttempts) {
                state.blockedUntilEpochSeconds = now + blockSeconds;
                state.failures = 0;
                state.windowStartEpochSeconds = now;
            }
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}