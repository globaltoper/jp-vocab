package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.common.exception.UserNotFoundByEmailException;
import com.toper.jpvocab.common.exception.UsernameAlreadyExistsException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final MockEmailService mockEmailService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        // 추천인은 "있으면 연결, 없거나 잘못 입력해도 가입은 그대로 진행" - 가입을 막을 정도의 항목은 아니라서.
        User referrer = null;
        if (request.referrerUsername() != null && !request.referrerUsername().isBlank()) {
            referrer = userRepository.findByUsername(request.referrerUsername()).orElse(null);
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .birthDate(request.birthDate())
                .phoneNumber(request.phoneNumber())
                .termsAgreed(request.termsAgreed())
                .termsAgreedAt(LocalDateTime.now())
                .referrer(referrer)
                .targetLevel(request.targetLevel())
                .currentLevel(request.currentLevel())
                .dailyGoalCount(request.dailyGoalCount())
                .referralSource(request.referralSource())
                .build();

        User saved = userRepository.save(user);
        emailVerificationService.sendVerificationEmail(saved);
        return SignupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FindUsernameResponse findUsernameByEmail(FindUsernameRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundByEmailException::new);

        mockEmailService.sendUsernameReminder(user.getEmail(), user.getUsername());
        return new FindUsernameResponse(user.getUsername());
    }
}
