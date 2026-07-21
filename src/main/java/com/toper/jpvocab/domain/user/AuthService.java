package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.security.JwtTokenProvider;
import com.toper.jpvocab.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 실패 시 BadCredentialsException -> GlobalExceptionHandler에서 401 INVALID_CREDENTIALS로 변환
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User userRef = userRepository.getReferenceById(principal.getUserId());

        String accessToken = jwtTokenProvider.createToken(principal.getUserId(), principal.getUsername());
        RefreshToken refreshToken = refreshTokenService.issue(userRef);

        return new LoginResponse(
                accessToken, refreshToken.getToken(), "Bearer", jwtTokenProvider.getExpirationSeconds());
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰 + 새 리프레시 토큰을 발급한다(토큰 회전).
     */
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken rotated = refreshTokenService.rotate(request.refreshToken());
        User user = rotated.getUser();

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername());

        return new LoginResponse(
                accessToken, rotated.getToken(), "Bearer", jwtTokenProvider.getExpirationSeconds());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
