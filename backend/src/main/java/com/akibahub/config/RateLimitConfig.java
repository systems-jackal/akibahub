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

    private final int authCapacity;
    private final Duration authRefillPeriod;
    private final int paymentsCapacity;
    private final Duration paymentsRefillPeriod;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> paymentBuckets = new ConcurrentHashMap<>();

    public RateLimitConfig(@Value("${rate-limit.auth.capacity:10}") int authCapacity,
                           @Value("${rate-limit.auth.refill-period-seconds:60}") int authRefillPeriodSeconds,
                           @Value("${rate-limit.payments.capacity:20}") int paymentsCapacity,
                           @Value("${rate-limit.payments.refill-period-seconds:60}") int paymentsRefillPeriodSeconds) {
        this.authCapacity = authCapacity;
        this.authRefillPeriod = Duration.ofSeconds(authRefillPeriodSeconds);
        this.paymentsCapacity = paymentsCapacity;
        this.paymentsRefillPeriod = Duration.ofSeconds(paymentsRefillPeriodSeconds);
    }

    public Bucket resolveAuthBucket(String clientIp) {
        return authBuckets.computeIfAbsent(clientIp, k -> createBucket(authCapacity, authRefillPeriod));
    }

    public Bucket resolvePaymentBucket(String clientIp) {
        return paymentBuckets.computeIfAbsent(clientIp, k -> createBucket(paymentsCapacity, paymentsRefillPeriod));
    }

    /** @deprecated use {@link #resolveAuthBucket(String)} */
    public Bucket resolveBucket(String clientIp) {
        return resolveAuthBucket(clientIp);
    }

    private Bucket createBucket(int capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.simple(capacity, refillPeriod);
        return Bucket.builder().addLimit(limit).build();
    }
}
