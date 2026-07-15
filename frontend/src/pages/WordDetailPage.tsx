import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { WordDetail } from "../api/types";
import { wordApi, savedWordApi } from "../api/endpoints";
import { ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useTTS } from "../hooks/useTTS";
import { ExampleSentenceView } from "../components/ExampleSentenceView";

export function WordDetailPage() {
  const { wordId } = useParams<{ wordId: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { speak } = useTTS();

  const [word, setWord] = useState<WordDetail | null>(null);
  const [showMeaning, setShowMeaning] = useState(false);
  const [showFurigana, setShowFurigana] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);

  const loadWord = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);
    setShowMeaning(false);
    setShowFurigana(false);
    try {
      const data = await wordApi.getDetail(id);
      setWord(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "단어를 불러오지 못했습니다.");
      setWord(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const id = Number(wordId);
    if (!Number.isNaN(id)) {
      loadWord(id);
    }
  }, [wordId, loadWord]);

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

  if (isLoading) return <p>불러오는 중...</p>;
  if (error) return <p className="form-error">{error}</p>;
  if (!word) return null;

  return (
    <div className="word-detail-page">
      <button type="button" className="back-button" onClick={() => navigate(-1)}>
        ← 뒤로
      </button>

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
        </div>
        {saveMessage && <p className="save-message">{saveMessage}</p>}
      </div>

      <section className="examples-section">
        <h2>예문</h2>
        {word.examples.length === 0 && <p>등록된 예문이 없습니다.</p>}
        {word.examples.map((example) => (
          <ExampleSentenceView key={example.id} example={example} />
        ))}
      </section>
    </div>
  );
}
