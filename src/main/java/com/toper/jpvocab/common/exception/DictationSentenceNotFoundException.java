package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class DictationSentenceNotFoundException extends ApiException {
    public DictationSentenceNotFoundException() {
        super(HttpStatus.NOT_FOUND, "DICTATION_SENTENCE_NOT_FOUND", "딕테이션 문장을 찾을 수 없습니다.");
    }
}
