import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { authApi } from "../api/endpoints";
import { ApiError } from "../api/client";

export function FindUsernamePage() {
  const [email, setEmail] = useState("");
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setResult(null);
    setIsSubmitting(true);
    try {
      const response = await authApi.findUsername(email);
      setResult(response.username);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "아이디를 찾지 못했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <h1>아이디 찾기</h1>
      <form onSubmit={handleSubmit} className="auth-form">
        <label>
          가입 시 등록한 이메일
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        {error && <p className="form-error">{error}</p>}
        {result && (
          <p className="save-message">
            회원님의 아이디는 <strong>{result}</strong> 입니다.
          </p>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "조회 중..." : "아이디 찾기"}
        </button>
      </form>
      <p>
        <Link to="/login">로그인으로 돌아가기</Link>
      </p>
    </div>
  );
}
