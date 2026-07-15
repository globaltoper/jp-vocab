package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인 예외의 베이스 클래스. status/code/message를 함께 들고 다닌다.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
