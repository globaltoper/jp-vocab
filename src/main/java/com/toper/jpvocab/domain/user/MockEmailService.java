package com.toper.jpvocab.domain.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 실제 메일 서버(SMTP 등) 없이, "이런 메일을 보냈을 것"을 로그로만 남기는 mock 구현.
 * 나중에 진짜 메일 발송이 필요해지면 이 클래스 내부만 JavaMailSender 등으로 교체하면 되고,
 * 이 클래스를 호출하는 쪽(EmailVerificationService, PasswordResetService 등)은 손댈 필요 없다.
 */
@Slf4j
@Service
public class MockEmailService {

    public void sendEmailVerification(String toEmail, String token) {
        log.info("[MOCK EMAIL] to={} subject=이메일 인증 body=아래 토큰으로 인증을 완료하세요: {}", toEmail, token);
    }

    public void sendUsernameReminder(String toEmail, String username) {
        log.info("[MOCK EMAIL] to={} subject=아이디 찾기 body=회원님의 아이디는 '{}' 입니다.", toEmail, username);
    }

    public void sendPasswordReset(String toEmail, String token) {
        log.info("[MOCK EMAIL] to={} subject=비밀번호 재설정 body=아래 토큰으로 비밀번호를 재설정하세요: {}", toEmail, token);
    }
}
