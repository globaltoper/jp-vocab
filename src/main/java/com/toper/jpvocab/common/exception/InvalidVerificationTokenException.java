package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends ApiException {
    public InvalidVerificationTokenException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN", "인증 토큰이 유효하지 않거나 만료되었습니다.");
    }
}
