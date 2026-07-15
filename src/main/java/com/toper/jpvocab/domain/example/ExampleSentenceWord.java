package com.toper.jpvocab.domain.example;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 예문 문자열(sentence_jp) 안에서 클릭 가능한 단어 구간 매핑.
 * startIndex/endIndex는 sentence_jp 기준 0-based, end exclusive.
 */
@Entity
@Table(name = "example_sentence_words")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleSentenceWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "example_sentence_id", nullable = false)
    private ExampleSentence exampleSentence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "start_index", nullable = false)
    private Integer startIndex;

    @Column(name = "end_index", nullable = false)
    private Integer endIndex;

    public ExampleSentenceWord(ExampleSentence exampleSentence, Word word, Integer startIndex, Integer endIndex) {
        this.exampleSentence = exampleSentence;
        this.word = word;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
}
