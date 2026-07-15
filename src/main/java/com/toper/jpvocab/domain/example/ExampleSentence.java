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

@Entity
@Table(name = "example_sentences")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "sentence_jp", nullable = false, length = 500)
    private String sentenceJp;

    @Column(name = "sentence_reading", nullable = false, length = 500)
    private String sentenceReading;

    @Column(name = "sentence_meaning", nullable = false, length = 500)
    private String sentenceMeaning;

    public ExampleSentence(Word word, String sentenceJp, String sentenceReading, String sentenceMeaning) {
        this.word = word;
        this.sentenceJp = sentenceJp;
        this.sentenceReading = sentenceReading;
        this.sentenceMeaning = sentenceMeaning;
    }
}
