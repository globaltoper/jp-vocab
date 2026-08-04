import { useCallback, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import type { JlptLevel, WordCard } from "../api/types";
import { wordApi, savedWordApi } from "../api/endpoints";
import { ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useTTS } from "../hooks/useTTS";

const LEVELS: JlptLevel[] = ["N5", "N4", "N3", "N2", "N1"];

export function WordCardPage() {
  const { isAuthenticated } = useAuth();
  const { speak } = useTTS();

  const [searchParams] = useSearchParams();
  const levelFromUrl = searchParams.get("level");
  const initialLevel = LEVELS.includes(levelFromUrl as JlptLevel) ? (levelFromUrl as JlptLevel) : "";

  const [level, setLevel] = useState<JlptLevel | "">(initialLevel);
  const [word, setWord] = useState<WordCard | null>(null);
  const [showMeaning, setShowMeaning] = useState(false);
  const [showFurigana, setShowFurigana] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);

  const loadRandomWord = useCallback(async (selectedLevel: JlptLevel | "") => {
    setIsLoading(true);
    setError(null);
    setSaveMessage(null);
    setShowMeaning(false);
    setShowFurigana(false);
    try {
      const data = await wordApi.getRandom(selectedLevel || undefined);
      setWord(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "단어를 불러오지 못했습니다.");
      setWord(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRandomWord(level);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [level]);

  async function handleSave() {
    if (!word) return;
    setSaveMessage(null);
    try {
      await savedWordApi.save(word.id);
      setWord({ ...word, isSaved: true });
      setSaveMessage("단어장에 저장했습니다.");
    } catch (err) {
      setSaveMessage(err instanceof ApiError ? err.message : "저장에 실패했습니다.");
    }
  }

  return (
    <div className="word-card-page">
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

      {word && !isLoading && (
        <div className="word-card">
          <span className="word-level-badge">{word.level}</span>
          <h1 className="word-expression">{word.expression}</h1>

          <button type="button" className="tts-button" onClick={() => speak(word.expression)}>
            🔊 발음 듣기
          </button>

          <div className="word-toggle-row">
            <button type="button" onClick={() => setShowFurigana((v) => !v)}>
              {showFurigana ? "후리가나 숨기기" : "후리가나 보기"}
            </button>
            <button type="button" onClick={() => setShowMeaning((v) => !v)}>
              {showMeaning ? "뜻 숨기기" : "뜻 보기"}
            </button>
          </div>

          {showFurigana && <p className="word-furigana">{word.furigana}</p>}
          {showMeaning && (
            <p className="word-meaning">
              {word.meaning} <span className="word-pos">({word.partOfSpeech})</span>
            </p>
          )}

          <div className="word-actions">
            {isAuthenticated ? (
              <button type="button" onClick={handleSave} disabled={word.isSaved}>
                {word.isSaved ? "저장됨" : "단어장에 저장"}
              </button>
            ) : (
              <Link to="/login">로그인하고 저장하기</Link>
            )}
            <Link to={`/words/${word.id}`}>예문 보기</Link>
          </div>

          {saveMessage && <p className="save-message">{saveMessage}</p>}

          <button type="button" className="next-word-button" onClick={() => loadRandomWord(level)}>
            다음 단어 →
          </button>
        </div>
      )}
    </div>
  );
}
