package com.toper.jpvocab.domain.example;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleSentenceWordRepository extends JpaRepository<ExampleSentenceWord, Long> {

    List<ExampleSentenceWord> findByExampleSentenceInOrderByStartIndexAsc(List<ExampleSentence> exampleSentences);
}
