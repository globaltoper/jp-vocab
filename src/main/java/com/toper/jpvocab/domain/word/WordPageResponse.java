package com.toper.jpvocab.domain.word;

import java.util.List;
import org.springframework.data.domain.Page;

public record WordPageResponse(
        List<WordSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static WordPageResponse from(Page<Word> wordPage) {
        List<WordSummaryResponse> content = wordPage.getContent().stream()
                .map(WordSummaryResponse::from)
                .toList();
        return new WordPageResponse(
                content, wordPage.getNumber(), wordPage.getSize(),
                wordPage.getTotalElements(), wordPage.getTotalPages());
    }
}
