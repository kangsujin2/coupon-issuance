package com.my.tbd.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.my.tbd.exception.CouponException;
import com.my.tbd.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private DiscountType discountType;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    private Integer totalQuantity;
    private int issuedQuantity;
    private BigDecimal discountValue;
    private String description;

    public boolean isQuantityAvailable() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean isDateAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public boolean isCouponAvailable() {
        if (!isQuantityAvailable()) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!isDateAvailable()) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }
        return true;
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return endDate.isBefore(now) || !isQuantityAvailable();
    }

    public void issueCoupon() {
        issuedQuantity++;
    }

}
