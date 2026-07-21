package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.common.exception.InvalidRefreshTokenException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리프레시 토큰 발급/검증/회전(rotate)/폐기를 담당.
 * "회전"이란: 리프레시 토큰을 한 번 쓸 때마다 기존 것은 폐기하고 새 걸 발급하는 것.
 * 이렇게 하면 만약 리프레시 토큰이 탈취당해도, 진짜 사용자가 먼저 써버리면
 * 탈취범이 갖고 있던 토큰은 자동으로 무효가 된다(재사용 감지 효과).
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Transactional
    public RefreshToken issue(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshExpirationDays);
        return refreshTokenRepository.save(new RefreshToken(user, token, expiresAt));
    }

    /**
     * 기존 리프레시 토큰을 검증하고, 유효하면 폐기한 뒤 같은 사용자에게 새 리프레시 토큰을 발급한다.
     */
    @Transactional
    public RefreshToken rotate(String oldToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        existing.revoke();
        return issue(existing.getUser());
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(RefreshToken::revoke);
    }

    /**
     * 비밀번호 변경 등 "모든 기기에서 로그아웃"이 필요한 상황에서 사용.
     */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
