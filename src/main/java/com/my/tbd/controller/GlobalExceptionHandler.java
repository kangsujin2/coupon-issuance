package com.my.tbd.controller;

import com.my.tbd.dto.CouponIssueResponseDto;
import com.my.tbd.exception.CouponException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponException exception) {
        return new CouponIssueResponseDto(false, exception.getErrorCode().getMessage());
    }
}
