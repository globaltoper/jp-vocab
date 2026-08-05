import { Link } from "react-router-dom";
import type { JlptLevel } from "../api/types";

const LEVELS: { level: JlptLevel; label: string }[] = [
  { level: "N5", label: "입문" },
  { level: "N4", label: "기초" },
  { level: "N3", label: "중급" },
  { level: "N2", label: "중상급" },
  { level: "N1", label: "고급" },
];

export function HomePage() {
  return (
    <div className="home-page">
      <section className="home-hero">
        <div className="home-hero-text">
          <h1 className="home-hero-title">日本語単語帳</h1>
          <p className="home-hero-subtitle">
            제주 바다처럼 맑게, 감귤처럼 달콤하게 — 오늘의 일본어 단어를 만나보세요.
          </p>
        </div>
        <img
          className="home-hero-mascot"
          src="/dolharubang.webp"
          alt="일본어 책을 든 돌하르방 캐릭터"
          width={397}
          height={560}
          loading="eager"
        />
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
