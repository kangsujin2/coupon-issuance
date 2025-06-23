package com.my.tbd.service;

import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.RedisRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestKey;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponValidator {

    private final RedisRepository redisRepository;

    public void canIssueCoupon(long couponId, long userId, Integer totalQuantity) {
        if (!isCouponAlreadyIssued(couponId, userId)) {
            throw new CouponException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }
        if (!isQuantityAvailable(totalQuantity, couponId)) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
    }

    public boolean isCouponAlreadyIssued(long couponId, long userId) {
        return !redisRepository.sIsMember(generateCouponIssueRequestKey(couponId), String.valueOf(userId));
    }

    public boolean isQuantityAvailable(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) return true;
        return totalQuantity > redisRepository.sCard(generateCouponIssueRequestKey(couponId));
    }

}
