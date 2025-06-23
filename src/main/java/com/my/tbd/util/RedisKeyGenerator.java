package com.my.tbd.util;

public class RedisKeyGenerator {

    public static String generateCouponIssueRequestKey(long couponId) {
        return "coupon:issue:couponId=%s:".formatted(couponId);
    }

    public static String generateCouponIssueRequestQueueKey() {
        return "coupon:issue";
    }
}
