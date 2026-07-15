package com.toper.jpvocab.domain.review;

import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.word.Word;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 라이트너(Leitner) 박스 방식의 복습 스케줄. 단어를 저장하면 자동으로 box 1로 생성된다.
 */
@Entity
@Table(name = "review_schedule", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_word", columnNames = {"user_id", "word_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewSchedule {

    public static final int MIN_BOX_LEVEL = 1;
    public static final int MAX_BOX_LEVEL = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "box_level", nullable = false)
    private int boxLevel;

    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ReviewSchedule(User user, Word word) {
        this.user = user;
        this.word = word;
        this.boxLevel = MIN_BOX_LEVEL;
        this.nextReviewAt = LocalDateTime.now();
    }

    public void markRemembered() {
        this.boxLevel = Math.min(this.boxLevel + 1, MAX_BOX_LEVEL);
        this.lastReviewedAt = LocalDateTime.now();
        this.nextReviewAt = lastReviewedAt.plusDays(LeitnerIntervals.daysFor(this.boxLevel));
    }

    public void markForgotten() {
        this.boxLevel = MIN_BOX_LEVEL;
        this.lastReviewedAt = LocalDateTime.now();
        this.nextReviewAt = lastReviewedAt.plusDays(LeitnerIntervals.daysFor(this.boxLevel));
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
