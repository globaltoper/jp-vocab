package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.common.exception.InvalidVerificationTokenException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final MockEmailService mockEmailService;

    @Value("${app.email-verification-expiration-hours}")
    private long expirationHours;

    /**
     * 회원가입 직후 호출된다. 토큰을 만들어 저장하고, (mock) 이메일을 "보낸다".
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
        tokenRepository.save(new EmailVerificationToken(user, token, expiresAt));
        mockEmailService.sendEmailVerification(user.getEmail(), token);
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (verificationToken.isExpired()) {
            throw new InvalidVerificationTokenException();
        }

        verificationToken.getUser().verifyEmail();
        tokenRepository.delete(verificationToken);
    }
}
