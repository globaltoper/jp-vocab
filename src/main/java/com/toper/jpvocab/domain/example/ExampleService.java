package com.toper.jpvocab.domain.example;

import com.toper.jpvocab.domain.word.Word;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleSentenceRepository exampleSentenceRepository;
    private final ExampleSentenceWordRepository exampleSentenceWordRepository;

    @Transactional(readOnly = true)
    public List<ExampleResponse> getExamplesForWord(Word word) {
        List<ExampleSentence> sentences = exampleSentenceRepository.findByWordOrderByIdAsc(word);
        if (sentences.isEmpty()) {
            return List.of();
        }

        List<ExampleSentenceWord> links =
                exampleSentenceWordRepository.findByExampleSentenceInOrderByStartIndexAsc(sentences);

        Map<Long, List<ExampleSentenceWord>> linksBySentenceId = links.stream()
                .collect(Collectors.groupingBy(link -> link.getExampleSentence().getId()));

        return sentences.stream()
                .map(sentence -> toExampleResponse(sentence, linksBySentenceId.getOrDefault(sentence.getId(), List.of())))
                .toList();
    }

    private ExampleResponse toExampleResponse(ExampleSentence sentence, List<ExampleSentenceWord> links) {
        List<LinkedWordResponse> linkedWords = links.stream()
                .map(link -> new LinkedWordResponse(
                        link.getWord().getId(),
                        link.getWord().getExpression(),
                        link.getStartIndex(),
                        link.getEndIndex()))
                .toList();

        return new ExampleResponse(
                sentence.getId(),
                sentence.getSentenceJp(),
                sentence.getSentenceReading(),
                sentence.getSentenceMeaning(),
                linkedWords);
    }
}
