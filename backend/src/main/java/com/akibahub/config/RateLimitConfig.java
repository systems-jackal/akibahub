package com.akibahub.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private final int capacity;
    private final Duration refillPeriod;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitConfig(@Value("${rate-limit.auth.capacity:10}") int capacity,
                           @Value("${rate-limit.auth.refill-period-seconds:60}") int refillPeriodSeconds) {
        this.capacity = capacity;
        this.refillPeriod = Duration.ofSeconds(refillPeriodSeconds);
    }

    public Bucket resolveBucket(String clientIp) {
        return buckets.computeIfAbsent(clientIp, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        // simple refill: full capacity refilled every refillPeriod
        Bandwidth limit = Bandwidth.simple(capacity, refillPeriod);
        return Bucket.builder().addLimit(limit).build();
    }
}