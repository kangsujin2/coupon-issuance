package com.my.tbd.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.tbd.dto.CouponIssueRequestDto;
import com.my.tbd.repository.RedisRepository;
import com.my.tbd.service.FCFSCouponIssueServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.my.tbd.util.RedisKeyGenerator.generateCouponIssueRequestQueueKey;

@RequiredArgsConstructor
@EnableScheduling
@Component
@Slf4j
public class CouponIssueListener {

    private final FCFSCouponIssueServiceV2 couponIssueService;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String issueRequestQueueKey = generateCouponIssueRequestQueueKey();

    @Scheduled(fixedDelay = 1000)
    public void issue() throws JsonProcessingException {
        while (hasCouponIssueTargets()) {
            CouponIssueRequestDto target = getIssueTarget();
            log.info("issue start : %s".formatted(target));
            couponIssueService.processCouponIssue(target.couponId(), target.userId());
            log.info("issue end : %s".formatted(target));
            removeIssuedTarget();

        }
    }

    private boolean hasCouponIssueTargets() {
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    private CouponIssueRequestDto getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponIssueRequestDto.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }
}
