package com.my.tbd.service;

import com.my.tbd.entity.Coupon;
import com.my.tbd.entity.CouponRedis;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponRepository couponRepository;

    @Cacheable(cacheNames = "coupon")
    public CouponRedis getCouponCache(long couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_EXIST));
        return new CouponRedis(coupon);
    }
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedis getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedis putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedis putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
