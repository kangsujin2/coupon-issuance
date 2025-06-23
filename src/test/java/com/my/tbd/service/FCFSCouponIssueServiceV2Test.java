package com.my.tbd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.entity.Coupon;
import com.my.tbd.entity.CouponType;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.CouponRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestKey;
import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

class FCFSCouponIssueServiceV2Test {
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    FCFSCouponIssueServiceV2 couponIssueService;
    @Autowired
    CouponRepository couponRepository;

//    @BeforeEach
//    void clear() {
//        Collection<String> redisKeys = redisTemplate.keys("*");
//        redisTemplate.delete(redisKeys);
//    }

    @Test
    void issueCoupon() throws InterruptedException {
        // test code
        redisTemplate.opsForSet().add("test", "1");

        int threadCnt = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCnt);

        for (int i=0; i<threadCnt; i++) {
            CouponIssueRequestDto dto = new CouponIssueRequestDto(i, 1);
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    couponIssueService.processCouponRequest(dto);
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
        long couponId = 1;
        long userId = 1;
        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(couponId, userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.processCouponRequest(requestDto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

    @Test
    void shouldThrowException_whenIssueQuantityExceedsTotalQuantity() {
        long userId = 1000;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(10)
                .issuedQuantity(10)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        IntStream.range(0, coupon.getTotalQuantity()).forEach(idx -> {
            redisTemplate.opsForSet().add(generateCouponIssueRequestKey(coupon.getId()), String.valueOf(idx));
        });

        //
        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(coupon.getId(), userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.processCouponRequest(requestDto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    void shouldThrowException_whenCouponAlreadyIssued() {
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);
        redisTemplate.opsForSet().add(generateCouponIssueRequestKey(coupon.getId()), String.valueOf(userId));

        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(coupon.getId(), userId);
        CouponException exception = Assertions.assertThrows(CouponException.class, () -> {
            couponIssueService.processCouponRequest(requestDto);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    void shouldRecordCouponIssue() {
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(coupon.getId(), userId);
        couponIssueService.processCouponRequest(requestDto);

        Boolean isSaved = redisTemplate.opsForSet().isMember(generateCouponIssueRequestKey(coupon.getId()), String.valueOf(userId));
        Assertions.assertTrue(isSaved);
    }

    @Test
    void shouldEnqueueCouponIssueOnSuccess() throws JsonProcessingException, JsonProcessingException {
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("test")
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(coupon.getId(), userId);
        couponIssueService.processCouponRequest(requestDto);

        String savedIssueRequest = redisTemplate.opsForList().leftPop(generateCouponIssueRequestQueueKey());
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(requestDto), savedIssueRequest);
    }



}