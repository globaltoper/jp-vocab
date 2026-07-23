package com.toper.jpvocab.domain.word;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 단어 API. 인증은 선택(로그인 시 isSaved 정확히 계산, 비로그인이면 false).
 */
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
@Tag(name = "단어", description = "단어 카드 조회 API (인증 선택)")
public class WordController {

    private final WordService wordService;

    @GetMapping("/random")
    public ResponseEntity<WordCardResponse> getRandomWord(
            @RequestParam(required = false) JlptLevel level) {
        return ResponseEntity.ok(wordService.getRandomWord(level));
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<WordDetailResponse> getWordDetail(@PathVariable Long wordId) {
        return ResponseEntity.ok(wordService.getWordDetail(wordId));
    }

    @GetMapping
    public ResponseEntity<WordPageResponse> getWords(
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(wordService.getWords(level, page, size));
    }
}
