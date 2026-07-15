package com.toper.jpvocab.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "아이디는 필수입니다.") @Size(min = 3, max = 50, message = "아이디는 3~50자여야 합니다.") String username,
        @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.") String password,
        @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email
) {
}
