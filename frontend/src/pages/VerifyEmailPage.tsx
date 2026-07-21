import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { authApi } from "../api/endpoints";
import { ApiError } from "../api/client";

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!token) {
      setStatus("error");
      setMessage("인증 토큰이 없습니다. 이메일에 있는 링크를 다시 확인해주세요.");
      return;
    }

    authApi
      .verifyEmail(token)
      .then((response) => {
        setStatus("success");
        setMessage(response.message);
      })
      .catch((err) => {
        setStatus("error");
        setMessage(err instanceof ApiError ? err.message : "이메일 인증에 실패했습니다.");
      });
  }, [token]);

  return (
    <div className="auth-page review-empty">
      <h1>이메일 인증</h1>
      {status === "loading" && <p>인증 처리 중...</p>}
      {status === "success" && <p className="save-message">{message}</p>}
      {status === "error" && <p className="form-error">{message}</p>}
      <p>
        <Link to="/login">로그인으로 이동</Link>
      </p>
    </div>
  );
}
