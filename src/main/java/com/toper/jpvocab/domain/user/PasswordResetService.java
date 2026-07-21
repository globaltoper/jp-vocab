package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.common.exception.InvalidPasswordResetTokenException;
import com.toper.jpvocab.common.exception.UserNotFoundByEmailException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MockEmailService mockEmailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.password-reset-expiration-minutes}")
    private long expirationMinutes;

    @Transactional
    public PasswordResetRequestResponse requestReset(PasswordResetRequestRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundByEmailException::new);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        tokenRepository.save(new PasswordResetToken(user, token, expiresAt));
        mockEmailService.sendPasswordReset(user.getEmail(), token);

        return new PasswordResetRequestResponse("비밀번호 재설정 메일을 보냈습니다. (mock: 아래 token을 바로 쓰세요)", token);
    }

    @Transactional
    public void confirmReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (!resetToken.isValid()) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        resetToken.markUsed();

        // 비밀번호가 바뀌었으니 기존에 발급된 리프레시 토큰은 전부 무효화한다.
        // (탈취된 세션이 있었다면 이 시점에 전부 강제 로그아웃된다)
        refreshTokenService.revokeAllForUser(user.getId());
    }
}
