import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { reviewApi } from "../api/endpoints";

export function Navbar() {
  const { isAuthenticated, username, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [dueCount, setDueCount] = useState(0);

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
        <Link to="/">단어 카드</Link>
        {isAuthenticated && <Link to="/saved">내 단어장</Link>}
        {isAuthenticated && (
          <Link to="/review">복습{dueCount > 0 ? ` (${dueCount})` : ""}</Link>
        )}
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
