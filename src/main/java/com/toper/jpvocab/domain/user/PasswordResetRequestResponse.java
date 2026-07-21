package com.toper.jpvocab.domain.user;

/**
 * mock 단계라 token을 응답에 바로 실어 보낸다. 실제 메일 발송으로 바꾸면 이 필드는 빼야 한다.
 */
public record PasswordResetRequestResponse(String message, String token) {
}
