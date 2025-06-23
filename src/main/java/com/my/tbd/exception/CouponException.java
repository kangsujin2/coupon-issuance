package com.my.tbd.exception;

import jakarta.persistence.GeneratedValue;
import lombok.Getter;

@Getter
public class CouponException extends RuntimeException {

    private final ErrorCode errorCode;

    public CouponException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


}
