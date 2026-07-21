package com.toper.jpvocab.domain.user;

/**
 * 회원가입 시 "어떻게 알게 되셨나요?" 응답.
 */
public enum ReferralSource {
    SEARCH,       // 검색
    SNS,          // SNS
    FRIEND,       // 지인 추천
    AD,           // 광고
    OTHER         // 기타
}
