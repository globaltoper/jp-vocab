package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.security.JwtTokenProvider;
import com.toper.jpvocab.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        // 실패 시 BadCredentialsException -> GlobalExceptionHandler에서 401 INVALID_CREDENTIALS로 변환
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(principal.getUserId(), principal.getUsername());

        return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpirationSeconds());
    }
}
