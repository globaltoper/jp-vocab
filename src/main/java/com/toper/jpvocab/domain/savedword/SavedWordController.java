package com.toper.jpvocab.domain.savedword;

import com.toper.jpvocab.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 저장 단어(내 단어장) API. 전부 인증 필요 (SecurityConfig에서 anyRequest().authenticated() 로 보호됨).
 */
@RestController
@RequestMapping("/api/saved-words")
@RequiredArgsConstructor
@Tag(name = "저장 단어함", description = "내 단어장 저장/조회/삭제 (인증 필수)")
public class SavedWordController {

    private final SavedWordService savedWordService;

    @PostMapping
    public ResponseEntity<SavedWordResponse> save(@Valid @RequestBody SaveWordRequest request) {
        Long userId = SecurityUtils.requireCurrentUserId();
        SavedWordResponse response = savedWordService.save(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SavedWordListItemResponse>> getSavedWords() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(savedWordService.getSavedWords(userId));
    }

    @DeleteMapping("/{savedWordId}")
    public ResponseEntity<Void> delete(@PathVariable Long savedWordId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        savedWordService.delete(userId, savedWordId);
        return ResponseEntity.noContent().build();
    }
}
