package com.toper.jpvocab.domain.user;

/**
 * mock 단계라 username을 응답에 바로 실어 보낸다. 실제 메일 발송으로 바꾸면
 * 이 필드는 빼고 "이메일을 확인해주세요" 메시지만 응답하도록 바꿔야 한다(계정 존재 여부 노출 방지).
 */
public record FindUsernameResponse(String username) {
}
