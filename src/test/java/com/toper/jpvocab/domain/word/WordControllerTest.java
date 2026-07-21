package com.toper.jpvocab.domain.word;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toper.jpvocab.common.exception.WordNotFoundException;
import com.toper.jpvocab.config.SecurityConfig;
import com.toper.jpvocab.security.JwtAuthenticationFilter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * WordController에 대한 웹 레이어 슬라이스 테스트.
 *
 * addFilters = false는 MockMvc에 필터를 "등록"하지 않을 뿐, 관련 빈을 만드는 과정 자체를 막지는
 * 않는다. 게다가 @WebMvcTest는 기본적으로 jakarta.servlet.Filter 구현체는 무조건 컴포넌트
 * 스캔에 포함시키기 때문에, SecurityConfig만 빼면 JwtAuthenticationFilter(OncePerRequestFilter
 * 구현체 - Filter의 일종)가 여전히 딸려 들어와서 자신의 의존성(JwtTokenProvider 등)을 찾다가
 * 컨텍스트 로딩이 실패한다. 그래서 SecurityConfig와 JwtAuthenticationFilter를 둘 다 명시적으로
 * 스캔 대상에서 제외해야 한다. 이 테스트의 목적은 "컨트롤러가 서비스 응답을 올바른 HTTP 상태/JSON으로
 * 변환하는가"이지 인증/인가 자체가 아니라서, 시큐리티 관련 빈 전체를 끌고 올 필요가 없다.
 * (인증 자체의 동작은 SecurityUtils/JwtTokenProvider를 대상으로 한 별도 단위 테스트에서 다룬다.)
 */
@WebMvcTest(
        controllers = WordController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
class WordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WordService wordService;

    @Test
    @DisplayName("GET /api/words/random - 정상 응답")
    void getRandomWord_success() throws Exception {
        WordCardResponse response = new WordCardResponse(
                1L, "食べる", "たべる", "먹다", JlptLevel.N5, "동사", false);
        when(wordService.getRandomWord(isNull())).thenReturn(response);

        mockMvc.perform(get("/api/words/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expression").value("食べる"))
                .andExpect(jsonPath("$.level").value("N5"))
                .andExpect(jsonPath("$.isSaved").value(false));
    }

    @Test
    @DisplayName("GET /api/words/random?level=N3 - 레벨 파라미터가 서비스로 그대로 전달된다")
    void getRandomWord_withLevelParam_passesLevelToService() throws Exception {
        WordCardResponse response = new WordCardResponse(
                2L, "経験", "けいけん", "경험", JlptLevel.N3, "명사", false);
        when(wordService.getRandomWord(eq(JlptLevel.N3))).thenReturn(response);

        mockMvc.perform(get("/api/words/random").param("level", "N3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("N3"));
    }

    @Test
    @DisplayName("GET /api/words/{id} - 존재하지 않는 단어면 404와 에러 코드를 반환한다")
    void getWordDetail_notFound_returns404() throws Exception {
        when(wordService.getWordDetail(999L)).thenThrow(new WordNotFoundException(999L));

        mockMvc.perform(get("/api/words/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORD_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/words - 페이지네이션 파라미터 기본값(page=0, size=20) 적용")
    void getWords_defaultPagination() throws Exception {
        WordPageResponse response = new WordPageResponse(List.of(), 0, 20, 0L, 0);
        when(wordService.getWords(isNull(), eq(0), eq(20))).thenReturn(response);

        mockMvc.perform(get("/api/words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("GET /api/words?page=2&size=10 - 커스텀 페이지네이션 파라미터가 서비스로 전달된다")
    void getWords_customPagination() throws Exception {
        WordPageResponse response = new WordPageResponse(List.of(), 2, 10, 25L, 3);
        when(wordService.getWords(isNull(), eq(2), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/words").param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3));
    }
}
