package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class AlreadySavedException extends ApiException {
    public AlreadySavedException(Long wordId) {
        super(HttpStatus.CONFLICT, "ALREADY_SAVED", "이미 저장된 단어입니다. (wordId=" + wordId + ")");
    }
}
