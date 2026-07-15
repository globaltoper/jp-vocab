import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { ReviewWord } from "../api/types";
import { reviewApi } from "../api/endpoints";
import { ApiError } from "../api/client";
import { useTTS } from "../hooks/useTTS";

export function ReviewPage() {
  const { speak } = useTTS();

  const [queue, setQueue] = useState<ReviewWord[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showFurigana, setShowFurigana] = useState(false);
  const [showMeaning, setShowMeaning] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [completedCount, setCompletedCount] = useState(0);

  const loadDueReviews = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await reviewApi.getDue();
      setQueue(data);
      setCurrentIndex(0);
      setCompletedCount(0);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "복습 목록을 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDueReviews();
  }, [loadDueReviews]);

  const currentWord = queue[currentIndex];

  async function handleResult(remembered: boolean) {
    if (!currentWord) return;
    try {
      await reviewApi.submitResult(currentWord.wordId, remembered);
      setCompletedCount((c) => c + 1);
      setShowFurigana(false);
      setShowMeaning(false);
      setCurrentIndex((i) => i + 1);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "복습 결과 제출에 실패했습니다.");
    }
  }

  if (isLoading) return <p>불러오는 중...</p>;
  if (error) return <p className="form-error">{error}</p>;

  if (queue.length === 0) {
    return (
      <div className="review-page review-empty">
        <h1>오늘의 복습</h1>
        <p>오늘 복습할 단어가 없습니다. 단어를 저장하면 복습 목록에 자동으로 추가됩니다.</p>
        <Link to="/">단어 카드로 이동</Link>
      </div>
    );
  }

  if (currentIndex >= queue.length) {
    return (
      <div className="review-page review-empty">
        <h1>오늘의 복습 완료! 🎉</h1>
        <p>{completedCount}개 단어를 복습했습니다.</p>
        <Link to="/">단어 카드로 이동</Link>
      </div>
    );
  }

  return (
    <div className="review-page">
      <p className="review-progress">
        {currentIndex + 1} / {queue.length}
      </p>

      <div className="word-card">
        <span className="word-level-badge">{currentWord.level}</span>
        <h1 className="word-expression">{currentWord.expression}</h1>

        <button type="button" className="tts-button" onClick={() => speak(currentWord.expression)}>
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

        {showFurigana && <p className="word-furigana">{currentWord.furigana}</p>}
        {showMeaning && (
          <p className="word-meaning">
            {currentWord.meaning} <span className="word-pos">({currentWord.partOfSpeech})</span>
          </p>
        )}

        <div className="review-actions">
          <button type="button" className="review-forgot" onClick={() => handleResult(false)}>
            아직 몰라요
          </button>
          <button type="button" className="review-remembered" onClick={() => handleResult(true)}>
            기억해요
          </button>
        </div>
      </div>
    </div>
  );
}
