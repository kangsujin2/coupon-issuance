package com.my.tbd.entity;


import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;

public record CouponRedis(
        Long id,
        CouponType couponType,
        Integer totalQuantity,
        boolean isDateAvailable,
        boolean isQuantityAvailable

) {
    public CouponRedis(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.isDateAvailable(),
                coupon.isQuantityAvailable()
        );
    }

    public void isCouponAvailable() {
        if (!isQuantityAvailable) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!isDateAvailable) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }
    }
}
