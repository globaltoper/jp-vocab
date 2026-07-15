package com.toper.jpvocab.domain.word;

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
import lombok.Setter;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String expression;

    @Column(nullable = false, length = 100)
    private String furigana;

    @Column(nullable = false, length = 255)
    private String meaning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JlptLevel level;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Word(String expression, String furigana, String meaning, JlptLevel level, String partOfSpeech) {
        this.expression = expression;
        this.furigana = furigana;
        this.meaning = meaning;
        this.level = level;
        this.partOfSpeech = partOfSpeech;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
