package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.domain.word.JlptLevel;
import com.toper.jpvocab.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 딕테이션(듣고 받아쓰기) API. random/attempt는 인증 선택, history는 인증 필수.
 */
@RestController
@RequestMapping("/api/dictation")
@RequiredArgsConstructor
@Tag(name = "딕테이션/타자연습", description = "듣고 받아쓰기·타자연습 문제 조회 및 채점 (일부 인증 선택, history는 인증 필수)")
public class DictationController {

    private final DictationService dictationService;

    @GetMapping("/random")
    public ResponseEntity<DictationSentenceResponse> getRandomSentence(
            @RequestParam(required = false) JlptLevel level) {
        return ResponseEntity.ok(dictationService.getRandomSentence(level));
    }

    // 타자 연습(부기능): 정답을 처음부터 보여주는 별도 엔드포인트.
    @GetMapping("/practice-random")
    public ResponseEntity<TypingPracticeSentenceResponse> getRandomSentenceForTyping(
            @RequestParam(required = false) JlptLevel level) {
        return ResponseEntity.ok(dictationService.getRandomSentenceForTyping(level));
    }

    @PostMapping("/{sentenceId}/attempt")
    public ResponseEntity<DictationAttemptResponse> submitAttempt(
            @PathVariable Long sentenceId, @Valid @RequestBody DictationAttemptRequest request) {
        return ResponseEntity.ok(dictationService.submitAttempt(sentenceId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DictationHistoryItemResponse>> getHistory() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(dictationService.getHistory(userId));
    }
}
