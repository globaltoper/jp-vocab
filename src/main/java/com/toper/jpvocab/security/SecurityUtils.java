package com.toper.jpvocab.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 로그인 상태(토큰 유효)면 현재 사용자 id를 반환하고, 비로그인이면 empty를 반환한다.
     * word 조회처럼 "인증 선택" 엔드포인트에서 사용한다.
     */
    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal.getUserId());
    }

    /**
     * 인증이 필수인 엔드포인트에서 사용. Security 설정상 이 지점에 도달했다면 인증된 사용자다.
     */
    public static Long requireCurrentUserId() {
        return getCurrentUserId().orElseThrow(() -> new IllegalStateException("인증된 사용자가 없습니다."));
    }
}
