package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않거나 만료되었습니다. 다시 로그인해주세요.");
    }
}
