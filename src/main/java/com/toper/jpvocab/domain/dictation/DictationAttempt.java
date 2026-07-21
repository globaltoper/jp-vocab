package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인한 사용자의 딕테이션 시도 기록 (정확도/타이핑 속도 추이를 나중에 보여주기 위함).
 */
@Entity
@Table(name = "dictation_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DictationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id", nullable = false)
    private DictationSentence sentence;

    @Column(name = "accuracy_percent", nullable = false)
    private int accuracyPercent;

    // 분당 입력한 글자 수 (Characters Per Minute). 일본어는 단어 경계가 명확하지 않아 WPM보다 CPM이 더 적절.
    @Column(nullable = false)
    private int cpm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DictationAttempt(User user, DictationSentence sentence, int accuracyPercent, int cpm) {
        this.user = user;
        this.sentence = sentence;
        this.accuracyPercent = accuracyPercent;
        this.cpm = cpm;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
