package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 주의: 실서비스에서는 "이 이메일로 가입된 계정이 있는지"를 알려주는 것 자체가 계정 존재 여부를 노출하는
 * 보안 리스크(계정 스캐닝)라서, 보통은 이 경우에도 200 + "이메일을 확인해주세요" 같은 중립적인 응답을 준다.
 * 이 프로젝트는 데모/테스트 편의를 위해 명확한 에러로 알려주는 방식을 택했다.
 */
public class UserNotFoundByEmailException extends ApiException {
    public UserNotFoundByEmailException() {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 이메일로 가입된 계정을 찾을 수 없습니다.");
    }
}
