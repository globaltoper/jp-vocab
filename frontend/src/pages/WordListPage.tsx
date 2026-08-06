import { useCallback, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import type { JlptLevel, WordPage } from "../api/types";
import { wordApi } from "../api/endpoints";
import { ApiError } from "../api/client";

const LEVELS: JlptLevel[] = ["N5", "N4", "N3", "N2", "N1"];
const PAGE_SIZE = 20;

export function WordListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const levelParam = searchParams.get("level");
  const level = LEVELS.includes(levelParam as JlptLevel) ? (levelParam as JlptLevel) : "";
  const keyword = searchParams.get("q") ?? "";
  const page = Number(searchParams.get("page") ?? "0");

  // 입력창은 즉시 반응해야 하지만 요청은 타이핑이 멈춘 뒤에 보내야 한다.
  const [inputValue, setInputValue] = useState(keyword);
  const [data, setData] = useState<WordPage | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 주소창의 검색어가 (뒤로가기 등으로) 바뀌면 입력창도 따라간다.
  useEffect(() => {
    setInputValue(keyword);
  }, [keyword]);

  // 한 글자 칠 때마다 요청하면 서버가 과부하되고 화면도 깜빡인다. 300ms 쉬면 그때 보낸다.
  useEffect(() => {
    const timer = setTimeout(() => {
      if (inputValue === keyword) return;
      const next = new URLSearchParams(searchParams);
      if (inputValue.trim()) next.set("q", inputValue.trim());
      else next.delete("q");
      next.set("page", "0"); // 검색어가 바뀌면 1페이지부터 다시
      setSearchParams(next, { replace: true });
    }, 300);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inputValue]);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await wordApi.getList(level || undefined, page, PAGE_SIZE, keyword);
      setData(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "단어를 불러오지 못했습니다.");
      setData(null);
    } finally {
      setIsLoading(false);
    }
  }, [level, page, keyword]);

  useEffect(() => {
    load();
  }, [load]);

  function updateParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(key, value);
    else next.delete(key);
    if (key !== "page") next.set("page", "0");
    setSearchParams(next);
  }

  const totalPages = data?.totalPages ?? 0;

  return (
    <div className="word-list-page">
      <h1 className="word-list-title">단어 목록</h1>

      <div className="word-list-controls">
        <input
          type="search"
          className="word-search-input"
          placeholder="단어 · 읽기 · 뜻으로 검색 (예: 食, たべ, 먹다)"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          aria-label="단어 검색"
        />
        <div className="word-level-tabs" role="group" aria-label="레벨 필터">
          <button
            type="button"
            className={level === "" ? "level-tab active" : "level-tab"}
            onClick={() => updateParam("level", "")}
          >
            전체
          </button>
          {LEVELS.map((lv) => (
            <button
              key={lv}
              type="button"
              className={level === lv ? "level-tab active" : "level-tab"}
              onClick={() => updateParam("level", lv)}
            >
              {lv}
            </button>
          ))}
        </div>
      </div>

      {error && <p className="form-error">{error}</p>}

      {data && (
        <p className="word-list-count">
          {data.totalElements.toLocaleString()}개
          {keyword && <> · &ldquo;{keyword}&rdquo; 검색 결과</>}
        </p>
      )}

      {isLoading && !data && <p>불러오는 중...</p>}

      {data && data.content.length === 0 && (
        <p className="word-list-empty">조건에 맞는 단어가 없습니다.</p>
      )}

      {data && data.content.length > 0 && (
        <ul className="word-list">
          {data.content.map((word) => (
            <li key={word.id}>
              <Link to={`/words/${word.id}`} className="word-list-item">
                <span className="word-level-badge small">{word.level}</span>
                <span className="word-list-expression">{word.expression}</span>
                <span className="word-list-furigana">{word.furigana}</span>
                <span className="word-list-meaning">{word.meaning}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {totalPages > 1 && (
        <div className="word-list-pager">
          <button
            type="button"
            className="button-quiet"
            disabled={page <= 0}
            onClick={() => updateParam("page", String(page - 1))}
          >
            ← 이전
          </button>
          <span className="word-list-pageinfo">
            {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            className="button-quiet"
            disabled={page >= totalPages - 1}
            onClick={() => updateParam("page", String(page + 1))}
          >
            다음 →
          </button>
        </div>
      )}
    </div>
  );
}
