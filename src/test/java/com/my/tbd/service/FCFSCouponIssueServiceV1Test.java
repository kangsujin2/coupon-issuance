package com.my.tbd.service;

import com.my.tbd.controller.CouponController;
import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.entity.Coupon;
import com.my.tbd.entity.CouponIssue;
import com.my.tbd.entity.CouponType;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.CouponIssueRepository;
import com.my.tbd.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
class FCFSCouponIssueServiceV1Test {

    @Autowired
    FCFSCouponIssueServiceV1 couponIssueService;
    @Autowired
    CouponIssueRepository couponIssueRepository;
    @Autowired
    CouponRepository couponRepository;

    @Test
    void issueCoupon() throws InterruptedException {

        int threadCnt = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCnt);

        for (int i=0; i<threadCnt; i++) {
            CouponIssueRequestDto dto = new CouponIssueRequestDto(i, 1);
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    couponIssueService.issueCoupon(dto);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();

                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

    }

    @Test
    void shouldIssueCoupon_whenNoCouponIssueExists() {
        long couponId = 1L;
        long userId = 1L;
        CouponIssue couponIssue = couponIssueService.saveCouponIssue(couponId, userId);
        Assertions.assertTrue(couponIssueRepository.findById(couponIssue.getId()).isPresent());
    }

    @Test
    void shouldIssueCoupon_whenQuantityDateAndDuplicateChecksPass() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(100)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        //when
        CouponIssueRequestDto dto = new CouponIssueRequestDto(coupon.getId(), userId);
        couponIssueService.issueCoupon(dto);

        //then
        Coupon couponResult = couponRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(couponResult.getIssuedQuantity(), 1);

        boolean couponIssueResult = couponIssueRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
        Assertions.assertTrue(couponIssueResult);
    }

    @Test
    void shouldThrowException_whenIssueQuantityExceedsTotalQuantity() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(100)
                .issuedQuantity(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        //when
        CouponIssueRequestDto dto = new CouponIssueRequestDto(coupon.getId(), userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.issueCoupon(dto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    void shouldThrowException_whenIssueDateInvalid() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(100)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
        couponRepository.save(coupon);

        //when
        CouponIssueRequestDto dto = new CouponIssueRequestDto(coupon.getId(), userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.issueCoupon(dto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    void shouldThrowException_whenCouponAlreadyIssued() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(100)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
        couponRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueRepository.save(couponIssue);

        //when
        CouponIssueRequestDto dto = new CouponIssueRequestDto(coupon.getId(), userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.issueCoupon(dto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }




}