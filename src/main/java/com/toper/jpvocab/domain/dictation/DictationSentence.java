package com.toper.jpvocab.domain.dictation;

import com.toper.jpvocab.domain.word.JlptLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 딕테이션(듣고 받아쓰기) 전용 문장. domain/example의 예문(단어별 예문)과는 별개의 문장 풀이다.
 * 딕테이션은 "이 단어를 설명하는 예문"이 아니라 "받아쓰기 연습에 적당한 길이/난이도의 문장"이 필요해서 분리했다.
 */
@Entity
@Table(name = "dictation_sentences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DictationSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sentence_jp", nullable = false, length = 500)
    private String sentenceJp;

    // 받아쓰기 정답 비교 기준이 되는 후리가나(히라가나) 전체 읽기.
    @Column(name = "sentence_reading", nullable = false, length = 500)
    private String sentenceReading;

    @Column(name = "sentence_meaning", nullable = false, length = 500)
    private String sentenceMeaning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JlptLevel level;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DictationSentence(String sentenceJp, String sentenceReading, String sentenceMeaning, JlptLevel level) {
        this.sentenceJp = sentenceJp;
        this.sentenceReading = sentenceReading;
        this.sentenceMeaning = sentenceMeaning;
        this.level = level;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
