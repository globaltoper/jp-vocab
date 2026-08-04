import { Link } from "react-router-dom";
import type { JlptLevel } from "../api/types";

const LEVELS: { level: JlptLevel; label: string }[] = [
  { level: "N5", label: "입문" },
  { level: "N4", label: "기초" },
  { level: "N3", label: "중급" },
  { level: "N2", label: "중상급" },
  { level: "N1", label: "고급" },
];

function DolharubangSilhouette() {
  return (
    <svg
      className="dolharubang-silhouette"
      viewBox="0 0 100 140"
      aria-hidden="true"
      focusable="false"
    >
      <ellipse cx="50" cy="24" rx="34" ry="10" />
      <path d="M20 26 Q50 2 80 26 L74 34 Q50 16 26 34 Z" />
      <path d="M28 34 Q22 80 18 122 Q50 140 82 122 Q78 80 72 34 Q50 48 28 34 Z" />
      <circle cx="40" cy="54" r="6" />
      <circle cx="60" cy="54" r="6" />
      <ellipse cx="50" cy="100" rx="12" ry="8" />
    </svg>
  );
}

export function HomePage() {
  return (
    <div className="home-page">
      <section className="home-hero">
        <DolharubangSilhouette />
        <h1 className="home-hero-title">日本語単語帳</h1>
        <p className="home-hero-subtitle">
          제주 바다처럼 맑게, 감귤처럼 달콤하게 — 오늘의 일본어 단어를 만나보세요.
        </p>
      </section>

      <Link to="/words" className="home-random-card">
        <span className="home-random-title">전체 랜덤으로 시작하기</span>
        <span className="home-random-desc">레벨 상관없이 아무 단어나 바로 만나보기</span>
      </Link>

      <h2 className="home-section-title">레벨별로 학습하기</h2>
      <div className="home-category-grid">
        {LEVELS.map(({ level, label }) => (
          <Link key={level} to={`/words?level=${level}`} className="home-category-card">
            <span className="home-category-level">{level}</span>
            <span className="home-category-label">{label}</span>
          </Link>
        ))}
      </div>

      <h2 className="home-section-title">다른 학습 모드</h2>
      <div className="home-secondary-links">
        <Link to="/dictation" className="home-secondary-link">
          🎧 딕테이션
        </Link>
        <Link to="/typing-practice" className="home-secondary-link">
          ⌨️ 타자 연습
        </Link>
        <Link to="/review" className="home-secondary-link">
          📚 복습
        </Link>
      </div>
    </div>
  );
}
