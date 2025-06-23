package com.my.tbd.controller;

import com.my.tbd.component.DistributeLockExecutor;
import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.service.FCFSCouponIssueServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/coupon")
public class CouponController {

    private final FCFSCouponIssueServiceV2 couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;


    @PostMapping
    public void issueCoupon(@RequestBody CouponIssueRequestDto requestDto) {
        /*
         1. synchronized
        synchronized (this) {
            couponIssueService.issueCoupon(requestDto);
        }
         2. Redis distribute lock
        distributeLockExecutor.execute("Lock_" + requestDto.couponId(), 10000, 10000, () -> {
            couponIssueService.issueCoupon(requestDto);
        });
         */

        couponIssueService.processCouponRequest(requestDto);

    }

}
