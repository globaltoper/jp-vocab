import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { authApi } from "../api/endpoints";
import type { SignupRequest, SignupResponse } from "../api/types";
import {
  getStoredAccessToken,
  getStoredRefreshToken,
  setStoredTokens,
  SESSION_EXPIRED_EVENT,
} from "../api/client";

const USERNAME_STORAGE_KEY = "jpvocab_username";

interface AuthContextValue {
  username: string | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  signup: (payload: SignupRequest) => Promise<SignupResponse>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => {
    const hasToken = Boolean(getStoredAccessToken());
    return hasToken ? localStorage.getItem(USERNAME_STORAGE_KEY) : null;
  });

  // client.ts가 백그라운드에서 리프레시까지 실패하면(리프레시 토큰도 만료/폐기) 이 이벤트를 쏜다.
  // 그러면 화면에 남아있는 로그인 상태를 여기서 강제로 정리해준다.
  useEffect(() => {
    function handleSessionExpired() {
      localStorage.removeItem(USERNAME_STORAGE_KEY);
      setUsername(null);
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
  }, []);

  const login = useCallback(async (loginUsername: string, password: string) => {
    const response = await authApi.login({ username: loginUsername, password });
    setStoredTokens(response.accessToken, response.refreshToken);
    localStorage.setItem(USERNAME_STORAGE_KEY, loginUsername);
    setUsername(loginUsername);
  }, []);

  const signup = useCallback(async (payload: SignupRequest) => authApi.signup(payload), []);

  const logout = useCallback(async () => {
    const refreshToken = getStoredRefreshToken();
    // 서버에도 리프레시 토큰을 폐기 요청한다 - 안 하면 로그아웃해도 그 토큰으로 계속 액세스 토큰을 재발급받을 수 있다.
    // 네트워크 에러 등으로 실패해도 어차피 프론트에서는 로그아웃 처리한다(best-effort).
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        // 무시: 로컬 로그아웃은 계속 진행
      }
    }
    setStoredTokens(null, null);
    localStorage.removeItem(USERNAME_STORAGE_KEY);
    setUsername(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ username, isAuthenticated: username !== null, login, signup, logout }),
    [username, login, signup, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.");
  }
  return context;
}
