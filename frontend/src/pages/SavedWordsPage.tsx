import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { SavedWordItem } from "../api/types";
import { savedWordApi } from "../api/endpoints";
import { ApiError } from "../api/client";

export function SavedWordsPage() {
  const [savedWords, setSavedWords] = useState<SavedWordItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadSavedWords = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await savedWordApi.list();
      setSavedWords(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "저장 목록을 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSavedWords();
  }, [loadSavedWords]);

  async function handleDelete(savedWordId: number) {
    try {
      await savedWordApi.remove(savedWordId);
      setSavedWords((prev) => prev.filter((item) => item.id !== savedWordId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "삭제에 실패했습니다.");
    }
  }

  return (
    <div className="saved-words-page">
      <h1>내 단어장</h1>
      {isLoading && <p>불러오는 중...</p>}
      {error && <p className="form-error">{error}</p>}
      {!isLoading && savedWords.length === 0 && <p>저장한 단어가 없습니다.</p>}

      <ul className="saved-words-list">
        {savedWords.map((item) => (
          <li key={item.id} className="saved-word-item">
            <Link to={`/words/${item.wordId}`} className="saved-word-link">
              <span className="word-level-badge small">{item.level}</span>
              <span className="saved-word-expression">{item.expression}</span>
              <span className="saved-word-furigana">{item.furigana}</span>
              <span className="saved-word-meaning">{item.meaning}</span>
            </Link>
            <button type="button" className="delete-button" onClick={() => handleDelete(item.id)}>
              삭제
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
