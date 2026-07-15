package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends ApiException {
    public ReviewNotFoundException(Long wordId) {
        super(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND",
                "복습 스케줄을 찾을 수 없습니다. 먼저 단어를 저장해주세요. (wordId=" + wordId + ")");
    }
}
