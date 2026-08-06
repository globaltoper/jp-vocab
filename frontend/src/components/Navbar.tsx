import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { reviewApi } from "../api/endpoints";
import { useVoicePreference } from "../hooks/useVoicePreference";

export function Navbar() {
  const { isAuthenticated, username, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [dueCount, setDueCount] = useState(0);
  const { voice, setVoice } = useVoicePreference();

  useEffect(() => {
    if (!isAuthenticated) {
      setDueCount(0);
      return;
    }
    reviewApi
      .getDueCount()
      .then((data) => setDueCount(data.count))
      .catch(() => setDueCount(0));
  }, [isAuthenticated, location.pathname]);

  async function handleLogout() {
    await logout();
    navigate("/");
  }

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        日本語単語帳
      </Link>
      <nav className="navbar-links">
        <Link to="/words">단어 카드</Link>
        <Link to="/words/list">단어 목록</Link>
        <Link to="/dictation">딕테이션</Link>
        <Link to="/typing-practice">타자 연습</Link>
        {isAuthenticated && <Link to="/saved">내 단어장</Link>}
        {isAuthenticated && (
          <Link to="/review">복습{dueCount > 0 ? ` (${dueCount})` : ""}</Link>
        )}
        <div className="voice-toggle" role="group" aria-label="발음 목소리 선택">
          <button
            type="button"
            className={voice === "FEMALE" ? "voice-toggle-button active" : "voice-toggle-button"}
            onClick={() => setVoice("FEMALE")}
            title="四国めたん"
          >
            여성 음성
          </button>
          <button
            type="button"
            className={voice === "MALE" ? "voice-toggle-button active" : "voice-toggle-button"}
            onClick={() => setVoice("MALE")}
            title="玄野武宏"
          >
            남성 음성
          </button>
        </div>
        {isAuthenticated ? (
          <>
            <span className="navbar-username">{username}님</span>
            <button type="button" onClick={handleLogout} className="link-button">
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link to="/login">로그인</Link>
            <Link to="/signup">회원가입</Link>
          </>
        )}
      </nav>
    </header>
  );
}
