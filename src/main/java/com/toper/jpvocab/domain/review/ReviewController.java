package com.toper.jpvocab.domain.review;

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
import org.springframework.web.bind.annotation.RestController;

/**
 * 복습 스케줄(라이트너 박스) API. 전부 인증 필요.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "복습", description = "라이트너 박스 기반 복습 스케줄 조회/제출 (인증 필수)")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/due")
    public ResponseEntity<List<ReviewWordResponse>> getDueReviews() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(reviewService.getDueReviews(userId));
    }

    @GetMapping("/due/count")
    public ResponseEntity<ReviewDueCountResponse> getDueCount() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(reviewService.getDueCount(userId));
    }

    @PostMapping("/{wordId}/result")
    public ResponseEntity<ReviewWordResponse> submitResult(
            @PathVariable Long wordId, @Valid @RequestBody ReviewResultRequest request) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(reviewService.submitResult(userId, wordId, request.remembered()));
    }
}
