package com.my.tbd.service;

import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.entity.Coupon;
import com.my.tbd.entity.CouponIssue;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.CouponIssueRepository;
import com.my.tbd.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FCFSCouponIssueServiceV1 {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    public void issueCoupon(CouponIssueRequestDto requestDto) {
        Coupon coupon = couponRepository.findById(requestDto.couponId()).orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_EXIST));
        isCouponAlreadyIssued(requestDto.couponId(), requestDto.userId());
        if (coupon.isCouponAvailable()) {
            saveCouponIssue(requestDto.couponId(), requestDto.userId());
            coupon.issueCoupon();
        }
    }

    public CouponIssue saveCouponIssue(long couponId, long userId) {
        CouponIssue couponIssue = CouponIssue.builder().couponId(couponId).userId(userId).build();
        return couponIssueRepository.save(couponIssue);
    }

    public void isCouponAlreadyIssued(long couponId, long userId) {
        boolean hasUserReceivedCoupon = couponIssueRepository.existsByCouponIdAndUserId(couponId, userId);
        if (hasUserReceivedCoupon) {
            throw new CouponException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }

    }
}
