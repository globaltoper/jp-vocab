import { useCallback, useEffect, useRef, useState } from "react";
import type { KeyboardEvent } from "react";
import type { DictationAttemptResult, DictationHistoryItem, DictationSentence, JlptLevel } from "../api/types";
import { dictationApi } from "../api/endpoints";
import { ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useTTS } from "../hooks/useTTS";
import { useRomajiInput } from "../hooks/useRomajiInput";

const LEVELS: JlptLevel[] = ["N5", "N4", "N3", "N2", "N1"];

// 정답과 내가 쓴 것을 글자 단위로 비교해서 보여주는 간단한 비교 뷰.
// (완전한 문자열 정렬(diff) 알고리즘은 아니고, 같은 위치의 글자만 비교하는 방식이라
//  글자를 하나 빼먹거나 더 쓴 경우는 그 이후 글자가 전부 다르게 보일 수 있다.
//  그래도 "거의 맞았다 / 많이 다르다"를 한눈에 보여주는 용도로는 충분하다.)
// export하는 이유: 이 비교 로직만 따로 테스트하기 위해.
export function CharDiff({ typed, correct }: { typed: string; correct: string }) {
  const length = Math.max(typed.length, correct.length);
  const chars = Array.from({ length }, (_, i) => {
    const typedChar = typed[i];
    const correctChar = correct[i];
    const isMatch = typedChar !== undefined && typedChar === correctChar;
    return { key: i, display: correctChar ?? "", isMatch };
  });

  return (
    <p className="dictation-diff">
      {chars.map(({ key, display, isMatch }) => (
        <span key={key} className={isMatch ? "diff-match" : "diff-miss"}>
          {display}
        </span>
      ))}
    </p>
  );
}

export function DictationPage() {
  const { isAuthenticated } = useAuth();
  const { speak, preload, isSpeaking } = useTTS();
  const { inputRef, hiragana, reset: resetInput } = useRomajiInput();

  const [level, setLevel] = useState<JlptLevel | "">("");
  const [sentence, setSentence] = useState<DictationSentence | null>(null);
  const [result, setResult] = useState<DictationAttemptResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<DictationHistoryItem[]>([]);
  const [showHistory, setShowHistory] = useState(false);

  const attemptStartRef = useRef<number | null>(null);

  const loadRandomSentence = useCallback(async (selectedLevel: JlptLevel | "") => {
    setIsLoading(true);
    setError(null);
    setResult(null);
    attemptStartRef.current = null;
    resetInput();
    try {
      const data = await dictationApi.getRandom(selectedLevel || undefined);
      setSentence(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "문장을 불러오지 못했습니다.");
      setSentence(null);
    } finally {
      setIsLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    loadRandomSentence(level);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [level]);

  // 첫 글자를 입력하는 순간부터 시간을 재기 시작한다 (타이핑 속도 계산 기준).
  useEffect(() => {
    if (hiragana.length > 0 && attemptStartRef.current === null) {
      attemptStartRef.current = performance.now();
    }
  }, [hiragana]);

  // 문장이 로딩되면 재생 버튼을 누르기 전에 미리 음성을 합성해둔다.
  // 사용자가 실제로 "듣기"를 누를 때는 이미 캐시에 있어서 거의 즉시 재생된다.
  useEffect(() => {
    if (sentence) {
      preload(sentence.sentenceJp, 1);
      preload(sentence.sentenceJp, 0.7);
    }
  }, [sentence, preload]);

  function handlePlay(rate: number) {
    if (!sentence) return;
    speak(sentence.sentenceJp, rate);
  }

  async function handleSubmit() {
    if (!sentence || isSubmitting) return;
    const elapsedMs = attemptStartRef.current ? Math.round(performance.now() - attemptStartRef.current) : 0;

    setIsSubmitting(true);
    setError(null);
    try {
      const data = await dictationApi.submitAttempt(sentence.id, hiragana, elapsedMs);
      setResult(data);
      if (isAuthenticated) {
        loadHistory();
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "채점에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  const loadHistory = useCallback(async () => {
    try {
      const data = await dictationApi.getHistory();
      setHistory(data);
    } catch {
      // 기록 조회 실패는 조용히 무시한다 - 연습 자체에는 영향 없는 부가 기능이라서.
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      loadHistory();
    }
  }, [isAuthenticated, loadHistory]);

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter" && !result) {
      e.preventDefault();
      handleSubmit();
    }
  }

  return (
    <div className="dictation-page">
      <h1>딕테이션 연습</h1>
      <p className="dictation-intro">
        문장을 듣고, 들리는 대로 로마자로 입력하세요. 입력하는 즉시 히라가나로 자동 변환됩니다. 마침표・쉼표는{" "}
        <code>.</code> <code>,</code> 키를 그대로 입력하면 됩니다.
      </p>

      <div className="level-filter">
        <label>
          레벨:
          <select value={level} onChange={(e) => setLevel(e.target.value as JlptLevel | "")}>
            <option value="">전체</option>
            {LEVELS.map((lv) => (
              <option key={lv} value={lv}>
                {lv}
              </option>
            ))}
          </select>
        </label>
      </div>

      {isLoading && <p>불러오는 중...</p>}
      {error && <p className="form-error">{error}</p>}

      {sentence && !isLoading && (
        <div className="dictation-card">
          <span className="word-level-badge">{sentence.level}</span>

          <div className="dictation-play-row">
            <button type="button" className="tts-button" onClick={() => handlePlay(1)} disabled={isSpeaking}>
              {isSpeaking ? "재생 준비 중..." : "🔊 듣기"}
            </button>
            <button
              type="button"
              className="tts-button dictation-slow-button"
              onClick={() => handlePlay(0.7)}
              disabled={isSpeaking}
            >
              {isSpeaking ? "재생 준비 중..." : "🐢 천천히 듣기"}
            </button>
          </div>

          <input
            ref={inputRef}
            type="text"
            className="dictation-input"
            placeholder="로마자로 입력하세요 (예: konnichiwa)"
            disabled={result !== null}
            onKeyDown={handleKeyDown}
            autoComplete="off"
            autoCapitalize="off"
            autoCorrect="off"
            spellCheck={false}
          />

          {!result && (
            <button
              type="button"
              className="dictation-submit-button"
              onClick={handleSubmit}
              disabled={isSubmitting || hiragana.length === 0}
            >
              {isSubmitting ? "채점 중..." : "제출"}
            </button>
          )}

          {result && (
            <div className="dictation-result">
              <p className="dictation-score">
                정확도 <strong>{result.accuracyPercent}%</strong> · 속도{" "}
                <strong>{result.cpm}</strong> CPM
                {!result.saved && <span className="dictation-unsaved-note"> (로그인하면 기록이 저장됩니다)</span>}
              </p>

              <div className="dictation-answer">
                <p className="dictation-answer-label">내가 쓴 답</p>
                <p className="dictation-typed">{hiragana || "(입력 없음)"}</p>
                <p className="dictation-answer-label">정답</p>
                <CharDiff typed={hiragana} correct={result.correctReading} />
                <p className="dictation-sentence-jp">{result.sentenceJp}</p>
                <p className="dictation-sentence-meaning">{result.sentenceMeaning}</p>
              </div>

              <button type="button" className="next-word-button" onClick={() => loadRandomSentence(level)}>
                다음 문장 →
              </button>
            </div>
          )}
        </div>
      )}

      {isAuthenticated && history.length > 0 && (
        <div className="dictation-history">
          <button type="button" className="link-button" onClick={() => setShowHistory((v) => !v)}>
            {showHistory ? "최근 기록 숨기기" : "최근 기록 보기"}
          </button>
          {showHistory && (
            <ul className="dictation-history-list">
              {history.map((item) => (
                <li key={item.id}>
                  <span className="dictation-history-sentence">{item.sentenceJp}</span>
                  <span className="dictation-history-score">
                    {item.accuracyPercent}% · {item.cpm} CPM
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
