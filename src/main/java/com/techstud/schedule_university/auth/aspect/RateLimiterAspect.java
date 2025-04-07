package com.techstud.schedule_university.auth.aspect;

import com.techstud.schedule_university.auth.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimiterAspect {
    private final ConcurrentHashMap<String, RateInfo> limitCache = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String key = resolveKey(rateLimited.keyType(), request);

        RateInfo limitInfo = limitCache.compute(key, (k, v) -> {
            if (v == null || isIntervalExpired(v)) {
                return new RateInfo(rateLimited.limit(), System.currentTimeMillis());
            }
            return v;
        });

        synchronized (limitInfo) {
            if (limitInfo.getRemaining() <= 0) {
                throw new RateLimitExceededException("Rate limit exceeded");
            }
            limitInfo.decrement();
        }

        try {
            return joinPoint.proceed();
        } finally {
            updateCache(key, limitInfo, rateLimited.interval());
        }
    }

    private String resolveKey(RateLimitKeyType keyType, HttpServletRequest request) {
        return switch (keyType) {
            case IP -> request.getRemoteAddr();
            case USER_ID -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                yield (auth != null && auth.isAuthenticated()) ?
                        auth.getName() :
                        "ANONYMOUS";
            }
        };
    }

    private boolean isIntervalExpired(RateInfo info) {
        return System.currentTimeMillis() - info.getStartTime() > 1000L;
    }

    private void updateCache(String key, RateInfo info, int interval) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - info.getStartTime() > interval * 1000L) {
            limitCache.remove(key);
        }
    }

    @Data
    private static class RateInfo {
        private int remaining;
        private final long startTime;

        public RateInfo(int remaining, long startTime) {
            this.remaining = remaining;
            this.startTime = startTime;
        }

        public void decrement() {
            remaining--;
        }
    }
}
