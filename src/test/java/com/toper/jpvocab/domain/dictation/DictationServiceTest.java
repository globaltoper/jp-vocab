package com.toper.jpvocab.domain.dictation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toper.jpvocab.common.exception.DictationSentenceNotFoundException;
import com.toper.jpvocab.domain.user.User;
import com.toper.jpvocab.domain.user.UserRepository;
import com.toper.jpvocab.domain.word.JlptLevel;
import com.toper.jpvocab.security.UserPrincipal;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 딕테이션 서비스 단위 테스트. 특히 "비로그인은 채점만 하고 기록은 저장 안 함 / 로그인은 기록까지 저장"
 * 이라는 인증 선택(optional-auth) 분기를 SecurityContextHolder를 직접 조작해서 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class DictationServiceTest {

    @Mock
    private DictationSentenceRepository sentenceRepository;
    @Mock
    private DictationAttemptRepository attemptRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DictationService dictationService;

    @AfterEach
    void clearSecurityContext() {
        // 정적 SecurityContextHolder를 건드리는 테스트라, 다음 테스트에 영향 안 주도록 매번 초기화한다.
        SecurityContextHolder.clearContext();
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    private DictationSentence sentence(String reading) throws Exception {
        DictationSentence s = new DictationSentence("今日はいい天気ですね。", reading, "오늘은 날씨가 좋네요.", JlptLevel.N5);
        setId(s, 1L);
        return s;
    }

    @Test
    @DisplayName("레벨 지정 없이 랜덤 문장 조회 시 sentenceReading(정답)이 응답에 노출되지 않는다")
    void getRandomSentence_doesNotExposeReading() throws Exception {
        DictationSentence s = sentence("きょうはいいてんきですね");
        when(sentenceRepository.findRandomSentence(null)).thenReturn(s);

        DictationSentenceResponse response = dictationService.getRandomSentence(null);

        assertThat(response.sentenceJp()).isEqualTo(s.getSentenceJp());
        // DictationSentenceResponse에는 sentenceReading 필드 자체가 없다(컴파일 타임에 이미 보장되지만,
        // "왜 없는지"를 테스트 이름으로 문서화해두는 목적).
    }

    @Test
    @DisplayName("해당 레벨에 문장이 하나도 없으면 DictationSentenceNotFoundException")
    void getRandomSentence_none_throws() {
        when(sentenceRepository.findRandomSentence("N1")).thenReturn(null);

        assertThatThrownBy(() -> dictationService.getRandomSentence(JlptLevel.N1))
                .isInstanceOf(DictationSentenceNotFoundException.class);
    }

    @Test
    @DisplayName("비로그인 상태로 제출하면 채점은 되지만 기록은 저장되지 않는다(saved=false)")
    void submitAttempt_anonymous_scoresButDoesNotSave() throws Exception {
        DictationSentence s = sentence("きょうはいいてんきですね");
        when(sentenceRepository.findById(1L)).thenReturn(Optional.of(s));
        SecurityContextHolder.clearContext(); // 명시적으로 "비로그인" 상태

        DictationAttemptRequest request = new DictationAttemptRequest("きょうはいいてんきですね", 60000L);
        DictationAttemptResponse response = dictationService.submitAttempt(1L, request);

        assertThat(response.accuracyPercent()).isEqualTo(100);
        assertThat(response.saved()).isFalse();
        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 상태로 제출하면 채점 결과가 DictationAttempt로 저장된다(saved=true)")
    void submitAttempt_loggedIn_savesAttempt() throws Exception {
        DictationSentence s = sentence("きょうはいいてんきですね");
        when(sentenceRepository.findById(1L)).thenReturn(Optional.of(s));

        User userRef = User.builder().username("tester").build();
        setId(userRef, 42L);
        when(userRepository.getReferenceById(42L)).thenReturn(userRef);

        UserPrincipal principal = new UserPrincipal(42L, "tester", "encoded");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        DictationAttemptRequest request = new DictationAttemptRequest("きょうはいいてんきですね", 60000L);
        DictationAttemptResponse response = dictationService.submitAttempt(1L, request);

        assertThat(response.saved()).isTrue();
        verify(attemptRepository, times(1)).save(any(DictationAttempt.class));
    }

    @Test
    @DisplayName("존재하지 않는 문장 id로 제출하면 DictationSentenceNotFoundException")
    void submitAttempt_sentenceNotFound_throws() {
        when(sentenceRepository.findById(999L)).thenReturn(Optional.empty());

        DictationAttemptRequest request = new DictationAttemptRequest("なんでもいい", 1000L);

        assertThatThrownBy(() -> dictationService.submitAttempt(999L, request))
                .isInstanceOf(DictationSentenceNotFoundException.class);
    }

    @Test
    @DisplayName("CPM은 (입력 글자 수 / 경과 분)으로 계산된다 - 10글자를 1분에 입력하면 10 CPM")
    void submitAttempt_calculatesCpmCorrectly() throws Exception {
        DictationSentence s = sentence("あいうえおかきくけこ"); // 정답 10자
        when(sentenceRepository.findById(1L)).thenReturn(Optional.of(s));
        SecurityContextHolder.clearContext();

        DictationAttemptRequest request = new DictationAttemptRequest("あいうえおかきくけこ", 60_000L);
        DictationAttemptResponse response = dictationService.submitAttempt(1L, request);

        assertThat(response.cpm()).isEqualTo(10);
    }

    @Test
    @DisplayName("elapsedMs가 0이면 CPM은 0으로 처리된다(0으로 나누기 방지)")
    void submitAttempt_zeroElapsed_cpmIsZero() throws Exception {
        DictationSentence s = sentence("あいうえお");
        when(sentenceRepository.findById(1L)).thenReturn(Optional.of(s));
        SecurityContextHolder.clearContext();

        DictationAttemptRequest request = new DictationAttemptRequest("あいうえお", 0L);
        DictationAttemptResponse response = dictationService.submitAttempt(1L, request);

        assertThat(response.cpm()).isEqualTo(0);
    }

    @Test
    @DisplayName("딕테이션 기록 조회는 최근 20개까지 최신순으로 반환한다(리포지토리 위임 확인)")
    void getHistory_delegatesToRepository() {
        when(attemptRepository.findTop20ByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of());

        List<DictationHistoryItemResponse> result = dictationService.getHistory(42L);

        assertThat(result).isEmpty();
        verify(attemptRepository, times(1)).findTop20ByUserIdOrderByCreatedAtDesc(42L);
    }
}
