package com.toper.jpvocab.domain.example;

import com.toper.jpvocab.domain.word.Word;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleSentenceRepository extends JpaRepository<ExampleSentence, Long> {

    List<ExampleSentence> findByWordOrderByIdAsc(Word word);
}
