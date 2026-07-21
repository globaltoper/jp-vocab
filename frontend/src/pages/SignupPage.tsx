import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { ApiError } from "../api/client";
import type { JlptLevel, ReferralSource } from "../api/types";

const LEVELS: JlptLevel[] = ["N5", "N4", "N3", "N2", "N1"];

const REFERRAL_SOURCES: { value: ReferralSource; label: string }[] = [
  { value: "SEARCH", label: "검색" },
  { value: "SNS", label: "SNS" },
  { value: "FRIEND", label: "지인 추천" },
  { value: "AD", label: "광고" },
  { value: "OTHER", label: "기타" },
];

export function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();

  // 필수
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [termsAgreed, setTermsAgreed] = useState(false);

  // 선택
  const [birthDate, setBirthDate] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [referrerUsername, setReferrerUsername] = useState("");
  const [targetLevel, setTargetLevel] = useState<JlptLevel | "">("");
  const [currentLevel, setCurrentLevel] = useState<JlptLevel | "">("");
  const [dailyGoalCount, setDailyGoalCount] = useState("");
  const [referralSource, setReferralSource] = useState<ReferralSource | "">("");

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await signup({
        username,
        password,
        email,
        termsAgreed,
        birthDate: birthDate || undefined,
        phoneNumber: phoneNumber || undefined,
        referrerUsername: referrerUsername || undefined,
        targetLevel: targetLevel || undefined,
        currentLevel: currentLevel || undefined,
        dailyGoalCount: dailyGoalCount ? Number(dailyGoalCount) : undefined,
        referralSource: referralSource || undefined,
      });
      navigate("/login");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "회원가입에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <h1>회원가입</h1>
      <form onSubmit={handleSubmit} className="auth-form">
        <label>
          아이디
          <input value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
        </label>
        <label>
          비밀번호 (8자 이상)
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <label>
          이메일
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>

        <h2 className="auth-form-section">선택 입력</h2>

        <label>
          생일
          <input type="date" value={birthDate} onChange={(e) => setBirthDate(e.target.value)} />
        </label>
        <label>
          전화번호
          <input
            type="tel"
            placeholder="010-1234-5678"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </label>
        <label>
          추천인 아이디
          <input value={referrerUsername} onChange={(e) => setReferrerUsername(e.target.value)} />
        </label>
        <label>
          현재 수준
          <select value={currentLevel} onChange={(e) => setCurrentLevel(e.target.value as JlptLevel | "")}>
            <option value="">선택 안 함</option>
            {LEVELS.map((lv) => (
              <option key={lv} value={lv}>
                {lv}
              </option>
            ))}
          </select>
        </label>
        <label>
          희망 레벨(목표)
          <select value={targetLevel} onChange={(e) => setTargetLevel(e.target.value as JlptLevel | "")}>
            <option value="">선택 안 함</option>
            {LEVELS.map((lv) => (
              <option key={lv} value={lv}>
                {lv}
              </option>
            ))}
          </select>
        </label>
        <label>
          하루 목표 단어 수
          <input
            type="number"
            min={1}
            max={500}
            placeholder="예: 10"
            value={dailyGoalCount}
            onChange={(e) => setDailyGoalCount(e.target.value)}
          />
        </label>
        <label>
          어떻게 알게 되셨나요?
          <select
            value={referralSource}
            onChange={(e) => setReferralSource(e.target.value as ReferralSource | "")}
          >
            <option value="">선택 안 함</option>
            {REFERRAL_SOURCES.map((item) => (
              <option key={item.value} value={item.value}>
                {item.label}
              </option>
            ))}
          </select>
        </label>

        <label className="auth-form-checkbox">
          <input
            type="checkbox"
            checked={termsAgreed}
            onChange={(e) => setTermsAgreed(e.target.checked)}
            required
          />
          (필수) 이용약관 및 개인정보 처리방침에 동의합니다.
        </label>

        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "가입 중..." : "회원가입"}
        </button>
      </form>
      <p>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </div>
  );
}
