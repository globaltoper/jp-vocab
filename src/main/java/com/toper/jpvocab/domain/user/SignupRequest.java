package com.toper.jpvocab.domain.user;

import com.toper.jpvocab.domain.word.JlptLevel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignupRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(min = 3, max = 50, message = "아이디는 3~50자여야 합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        // --- 아래는 전부 선택 입력 항목 ---

        @Past(message = "생일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @Pattern(regexp = "^[0-9-]{9,20}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,

        // 회원가입 필수 동의. false면 400 INVALID_INPUT.
        @AssertTrue(message = "이용약관에 동의해야 가입할 수 있습니다.")
        boolean termsAgreed,

        // 추천인 아이디. 존재하지 않는 아이디를 넣어도 가입은 그대로 진행되고 추천인만 비워진다.
        String referrerUsername,

        JlptLevel targetLevel,
        JlptLevel currentLevel,

        @Min(value = 1, message = "하루 목표 단어 수는 1개 이상이어야 합니다.")
        @Max(value = 500, message = "하루 목표 단어 수는 500개 이하로 입력해주세요.")
        Integer dailyGoalCount,

        ReferralSource referralSource
) {
}
