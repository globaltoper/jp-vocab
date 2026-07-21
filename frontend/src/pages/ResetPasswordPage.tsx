import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { authApi } from "../api/endpoints";
import { ApiError } from "../api/client";

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState("");
  const [token, setToken] = useState(searchParams.get("token") ?? "");
  const [newPassword, setNewPassword] = useState("");

  const [requestMessage, setRequestMessage] = useState<string | null>(null);
  const [confirmMessage, setConfirmMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isRequesting, setIsRequesting] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);

  async function handleRequest(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setRequestMessage(null);
    setIsRequesting(true);
    try {
      const response = await authApi.requestPasswordReset(email);
      setRequestMessage(response.message);
      // mock 단계: 실제 메일함 대신 여기서 바로 토큰을 채워준다.
      setToken(response.token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "요청에 실패했습니다.");
    } finally {
      setIsRequesting(false);
    }
  }

  async function handleConfirm(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setConfirmMessage(null);
    setIsConfirming(true);
    try {
      const response = await authApi.confirmPasswordReset(token, newPassword);
      setConfirmMessage(response.message);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "비밀번호 변경에 실패했습니다.");
    } finally {
      setIsConfirming(false);
    }
  }

  return (
    <div className="auth-page">
      <h1>비밀번호 재설정</h1>

      <h2 className="auth-form-section">1. 이메일로 재설정 요청</h2>
      <form onSubmit={handleRequest} className="auth-form">
        <label>
          가입 시 등록한 이메일
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <button type="submit" disabled={isRequesting}>
          {isRequesting ? "요청 중..." : "재설정 메일 요청"}
        </button>
      </form>
      {requestMessage && <p className="save-message">{requestMessage}</p>}

      <h2 className="auth-form-section">2. 토큰으로 새 비밀번호 설정</h2>
      <form onSubmit={handleConfirm} className="auth-form">
        <label>
          토큰
          <input value={token} onChange={(e) => setToken(e.target.value)} required />
        </label>
        <label>
          새 비밀번호 (8자 이상)
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <button type="submit" disabled={isConfirming}>
          {isConfirming ? "변경 중..." : "비밀번호 변경"}
        </button>
      </form>
      {confirmMessage && <p className="save-message">{confirmMessage}</p>}
      {error && <p className="form-error">{error}</p>}

      <p>
        <Link to="/login">로그인으로 돌아가기</Link>
      </p>
    </div>
  );
}
