package com.toper.jpvocab.common.exception;

import org.springframework.http.HttpStatus;

public class SavedWordNotFoundException extends ApiException {
    public SavedWordNotFoundException(Long savedWordId) {
        super(HttpStatus.NOT_FOUND, "SAVED_WORD_NOT_FOUND", "저장 항목을 찾을 수 없습니다. (id=" + savedWordId + ")");
    }
}
