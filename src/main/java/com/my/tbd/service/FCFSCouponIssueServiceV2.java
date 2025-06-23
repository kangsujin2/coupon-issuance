package com.my.tbd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.tbd.component.DistributeLockExecutor;
import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.entity.Coupon;
import com.my.tbd.entity.CouponIssue;
import com.my.tbd.entity.CouponRedis;
import com.my.tbd.entity.event.CouponIssueCompleteEvent;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import com.my.tbd.repository.CouponIssueRepository;
import com.my.tbd.repository.CouponRepository;
import com.my.tbd.repository.RedisRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestKey;
import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestQueueKey;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FCFSCouponIssueServiceV2 {

    private final CouponRepository couponRepository;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponValidator couponValidator;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponCacheService couponCacheService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void processCouponRequest(CouponIssueRequestDto requestDto) {
        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        CouponRedis coupon = couponCacheService.getCouponLocalCache(couponId);
        coupon.isCouponAvailable();

        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponValidator.canIssueCoupon(couponId, userId, coupon.totalQuantity());
            enqueueCouponRequest(couponId, userId);
        });
    }

    private void enqueueCouponRequest(long couponId, long userId) {
        CouponIssueRequestDto issueRequest = new CouponIssueRequestDto(couponId, userId);

        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sAdd(generateCouponIssueRequestKey(couponId), String.valueOf(userId)); // set
            redisRepository.rPush(generateCouponIssueRequestQueueKey(), value); // list
        } catch (JsonProcessingException e) {
            throw new CouponException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST);
        }
    }

    public void processCouponIssue(long couponId, long userId) {
        Coupon coupon = findCouponWithLock(couponId);
        saveCouponIssue(couponId, userId);
        coupon.issueCoupon();
        publishCouponEvent(coupon);
    }

    public Coupon findCouponWithLock(long couponId) {
        return couponRepository.findCouponWithLock(couponId).orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_EXIST));
    }

    private CouponIssue saveCouponIssue(long couponId, long userId) {
        CouponIssue couponIssue = CouponIssue.builder().couponId(couponId).userId(userId).build();
        return couponIssueRepository.save(couponIssue);
    }

    private void publishCouponEvent(Coupon coupon) {
        if (coupon.isIssueComplete()) {
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }

}
