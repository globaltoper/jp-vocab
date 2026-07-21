package com.toper.jpvocab.domain.savedword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toper.jpvocab.common.exception.AlreadySavedException;
import com.toper.jpvocab.common.exception.ForbiddenException;
import com.toper.jpvocab.common.exception.SavedWordNotFoundException;
import com.toper.jpvocab.common.exception.WordNotFoundException;
import com.toper.jpvocab.domain.review.ReviewService;
import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.user.UserRepository;
import com.toper.jpvocab.domain.word.JlptLevel;
import com.toper.jpvocab.domain.word.Word;
import com.toper.jpvocab.domain.word.WordRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SavedWordService의 서비스 레이어 단위 테스트. 리포지토리는 전부 Mockito로 대체하고,
 * "이미 저장된 단어" / "존재하지 않는 단어" / "타인 소유 삭제 시도" 같은 예외 분기를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SavedWordServiceTest {

    @Mock
    private SavedWordRepository savedWordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WordRepository wordRepository;
    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private SavedWordService savedWordService;

    private static final Long USER_ID = 1L;
    private Word word;

    @BeforeEach
    void setUp() throws Exception {
        word = new Word("食べる", "たべる", "먹다", JlptLevel.N5, "동사");
        setId(word, 10L);
    }

    // 엔티티들의 id는 @GeneratedValue라 세터가 없다 - 테스트에서 리플렉션으로 강제 주입한다.
    private static void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("정상 저장: 단어가 존재하고 아직 저장한 적 없으면 저장되고 복습 스케줄도 생성된다")
    void save_success_createsReviewSchedule() {
        SaveWordRequest request = new SaveWordRequest(word.getId());
        when(wordRepository.findById(word.getId())).thenReturn(Optional.of(word));
        when(savedWordRepository.existsByUserIdAndWordId(USER_ID, word.getId())).thenReturn(false);
        User userRef = User.builder().username("tester").build();
        when(userRepository.getReferenceById(USER_ID)).thenReturn(userRef);
        when(savedWordRepository.save(any(SavedWord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavedWordResponse response = savedWordService.save(USER_ID, request);

        assertThat(response.wordId()).isEqualTo(word.getId());
        verify(reviewService, times(1)).createScheduleIfAbsent(userRef, word);
    }

    @Test
    @DisplayName("존재하지 않는 단어를 저장하려 하면 WordNotFoundException")
    void save_wordNotFound_throws() {
        SaveWordRequest request = new SaveWordRequest(999L);
        when(wordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savedWordService.save(USER_ID, request))
                .isInstanceOf(WordNotFoundException.class);

        verify(savedWordRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 저장한 단어를 다시 저장하려 하면 AlreadySavedException")
    void save_alreadySaved_throws() {
        SaveWordRequest request = new SaveWordRequest(word.getId());
        when(wordRepository.findById(word.getId())).thenReturn(Optional.of(word));
        when(savedWordRepository.existsByUserIdAndWordId(USER_ID, word.getId())).thenReturn(true);

        assertThatThrownBy(() -> savedWordService.save(USER_ID, request))
                .isInstanceOf(AlreadySavedException.class);

        verify(savedWordRepository, never()).save(any());
        verify(reviewService, never()).createScheduleIfAbsent(any(), any());
    }

    @Test
    @DisplayName("내 단어장 조회는 저장한 순서(최신순)대로 반환한다")
    void getSavedWords_returnsInSavedOrder() throws Exception {
        SavedWord saved = new SavedWord(User.builder().build(), word);
        setId(saved, 100L);
        when(savedWordRepository.findByUserIdOrderBySavedAtDesc(USER_ID)).thenReturn(List.of(saved));

        List<SavedWordListItemResponse> result = savedWordService.getSavedWords(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).wordId()).isEqualTo(word.getId());
    }

    @Test
    @DisplayName("본인이 저장한 단어는 정상 삭제되고 복습 스케줄도 함께 삭제된다")
    void delete_ownWord_success() throws Exception {
        User owner = User.builder().username("tester").build();
        setId(owner, USER_ID);
        SavedWord saved = new SavedWord(owner, word);
        setId(saved, 100L);
        when(savedWordRepository.findById(100L)).thenReturn(Optional.of(saved));

        savedWordService.delete(USER_ID, 100L);

        verify(reviewService, times(1)).deleteSchedule(USER_ID, word.getId());
        verify(savedWordRepository, times(1)).delete(saved);
    }

    @Test
    @DisplayName("존재하지 않는 savedWordId 삭제 시도는 SavedWordNotFoundException")
    void delete_notFound_throws() {
        when(savedWordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savedWordService.delete(USER_ID, 999L))
                .isInstanceOf(SavedWordNotFoundException.class);
    }

    @Test
    @DisplayName("타인이 저장한 단어를 삭제하려 하면 ForbiddenException, 실제 삭제는 일어나지 않는다")
    void delete_otherUsersWord_throwsForbidden() throws Exception {
        User owner = User.builder().username("owner").build();
        setId(owner, 999L); // 요청자(USER_ID=1)와 다른 소유자
        SavedWord saved = new SavedWord(owner, word);
        setId(saved, 100L);
        when(savedWordRepository.findById(100L)).thenReturn(Optional.of(saved));

        assertThatThrownBy(() -> savedWordService.delete(USER_ID, 100L))
                .isInstanceOf(ForbiddenException.class);

        verify(savedWordRepository, never()).delete(any());
        verify(reviewService, never()).deleteSchedule(anyLong(), anyLong());
    }
}
