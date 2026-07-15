package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class WordNotFoundException extends ApiException {
    public WordNotFoundException(Long wordId) {
        super(HttpStatus.NOT_FOUND, "WORD_NOT_FOUND", "해당 단어를 찾을 수 없습니다. (id=" + wordId + ")");
    }

    public WordNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "WORD_NOT_FOUND", message);
    }
}
