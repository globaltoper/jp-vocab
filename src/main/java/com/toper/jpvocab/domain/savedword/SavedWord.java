package com.toper.jpvocab.domain.savedword;

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

@Entity
@Table(name = "user_saved_words", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_word", columnNames = {"user_id", "word_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public SavedWord(User user, Word word) {
        this.user = user;
        this.word = word;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.savedAt = LocalDateTime.now();
    }
}
