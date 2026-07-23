package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.common.dto.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증/계정 관련 API (회원가입, 로그인/토큰, 이메일 인증, 아이디/비밀번호 찾기).
 * 전부 /api/auth/** 경로라 SecurityConfig에서 permitAll로 열려있다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입/로그인/토큰 재발급/이메일 인증/아이디·비밀번호 찾기 (전부 인증 불필요)")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.ok(new MessageResponse("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/find-username")
    public ResponseEntity<FindUsernameResponse> findUsername(@Valid @RequestBody FindUsernameRequest request) {
        return ResponseEntity.ok(userService.findUsernameByEmail(request));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetRequestResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestRequest request) {
        return ResponseEntity.ok(passwordResetService.requestReset(request));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<MessageResponse> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다. 다시 로그인해주세요."));
    }
}
