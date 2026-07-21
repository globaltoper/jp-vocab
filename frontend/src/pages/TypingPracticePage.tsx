import { useCallback, useEffect, useRef, useState } from "react";
import type { JlptLevel, TypingPracticeSentence } from "../api/types";
import { dictationApi } from "../api/endpoints";
import { ApiError } from "../api/client";
import { useRomajiInput } from "../hooks/useRomajiInput";

const LEVELS: JlptLevel[] = ["N5", "N4", "N3", "N2", "N1"];

interface SessionStats {
  attempts: number;
  totalCpm: number;
  bestCpm: number;
}

const EMPTY_STATS: SessionStats = { attempts: 0, totalCpm: 0, bestCpm: 0 };

// 목표 텍스트를 한 글자씩 렌더링하면서, 지금까지 입력한 글자와 비교해 색을 입힌다.
// (타자 연습 사이트들의 표준 UX: 목표 문장을 미리 다 보여주고 맞은 글자/틀린 글자/아직 안 친 글자를 구분)
// export하는 이유: 이 비교 로직만 따로 테스트하기 위해 (테스트에서 페이지 전체를 렌더링할 필요 없이).
export function TypingTarget({ target, typed }: { target: string; typed: string }) {
  return (
    <p className="typing-target">
      {target.split("").map((char, i) => {
        let className = "typing-pending";
        if (i < typed.length) {
          className = typed[i] === char ? "typing-correct" : "typing-incorrect";
        } else if (i === typed.length) {
          className = "typing-cursor";
        }
        return (
          <span key={i} className={className}>
            {char}
          </span>
        );
      })}
    </p>
  );
}

export function TypingPracticePage() {
  const { inputRef, hiragana, reset: resetInput } = useRomajiInput();

  const [level, setLevel] = useState<JlptLevel | "">("");
  const [sentence, setSentence] = useState<TypingPracticeSentence | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [completedCpm, setCompletedCpm] = useState<number | null>(null);
  const [stats, setStats] = useState<SessionStats>(EMPTY_STATS);

  const startTimeRef = useRef<number | null>(null);

  const loadRandomSentence = useCallback(async (selectedLevel: JlptLevel | "") => {
    setIsLoading(true);
    setError(null);
    setCompletedCpm(null);
    startTimeRef.current = null;
    resetInput();
    try {
      const data = await dictationApi.getRandomForTyping(selectedLevel || undefined);
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

  useEffect(() => {
    if (!sentence || completedCpm !== null) return;

    if (hiragana.length > 0 && startTimeRef.current === null) {
      startTimeRef.current = performance.now();
    }

    // 목표 텍스트와 정확히 일치하면 자동으로 완료 처리한다.
    if (hiragana === sentence.sentenceReading) {
      const elapsedMs = startTimeRef.current ? performance.now() - startTimeRef.current : 0;
      const minutes = Math.max(elapsedMs, 1) / 60000;
      const cpm = Math.round(sentence.sentenceReading.length / minutes);
      setCompletedCpm(cpm);
      setStats((s) => ({
        attempts: s.attempts + 1,
        totalCpm: s.totalCpm + cpm,
        bestCpm: Math.max(s.bestCpm, cpm),
      }));
    }
  }, [hiragana, sentence, completedCpm]);

  const averageCpm = stats.attempts > 0 ? Math.round(stats.totalCpm / stats.attempts) : 0;

  return (
    <div className="typing-practice-page">
      <h1>일본어 타자 연습</h1>
      <p className="dictation-intro">
        화면에 보이는 문장을 로마자로 빠르고 정확하게 입력해보세요. 마침표는 <code>.</code>, 쉼표는{" "}
        <code>,</code>를 입력하면 그대로 「。」「、」로 바뀝니다. 딕테이션에서 빠르게 받아쓰려면 타자
        속도가 뒷받침돼야 합니다.
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

      {stats.attempts > 0 && (
        <div className="typing-stats">
          <span>완료 {stats.attempts}문장</span>
          <span>평균 {averageCpm} CPM</span>
          <span>최고 {stats.bestCpm} CPM</span>
        </div>
      )}

      {isLoading && <p>불러오는 중...</p>}
      {error && <p className="form-error">{error}</p>}

      {sentence && !isLoading && (
        <div className="typing-card">
          <span className="word-level-badge">{sentence.level}</span>
          <p className="dictation-sentence-jp">{sentence.sentenceJp}</p>

          <TypingTarget target={sentence.sentenceReading} typed={hiragana} />

          <input
            ref={inputRef}
            type="text"
            className="dictation-input"
            placeholder="로마자로 입력하세요"
            disabled={completedCpm !== null}
            autoComplete="off"
            autoCapitalize="off"
            autoCorrect="off"
            spellCheck={false}
          />

          {completedCpm !== null ? (
            <div className="typing-complete">
              <p className="dictation-score">
                완료! <strong>{completedCpm}</strong> CPM
              </p>
              <p className="dictation-sentence-meaning">{sentence.sentenceMeaning}</p>
              <button type="button" className="next-word-button" onClick={() => loadRandomSentence(level)}>
                다음 문장 →
              </button>
            </div>
          ) : (
            <button type="button" className="link-button" onClick={() => loadRandomSentence(level)}>
              건너뛰기
            </button>
          )}
        </div>
      )}
    </div>
  );
}
