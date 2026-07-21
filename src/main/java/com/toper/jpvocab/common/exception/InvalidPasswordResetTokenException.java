package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends ApiException {
    public InvalidPasswordResetTokenException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_RESET_TOKEN", "비밀번호 재설정 토큰이 유효하지 않거나 만료되었습니다.");
    }
}
