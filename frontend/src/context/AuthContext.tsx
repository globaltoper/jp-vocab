import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { authApi } from "../api/endpoints";
import { getStoredToken, setStoredToken } from "../api/client";

const USERNAME_STORAGE_KEY = "jpvocab_username";

interface AuthContextValue {
  username: string | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  signup: (username: string, password: string, email: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => {
    const hasToken = Boolean(getStoredToken());
    return hasToken ? localStorage.getItem(USERNAME_STORAGE_KEY) : null;
  });

  const login = useCallback(async (loginUsername: string, password: string) => {
    const response = await authApi.login({ username: loginUsername, password });
    setStoredToken(response.accessToken);
    localStorage.setItem(USERNAME_STORAGE_KEY, loginUsername);
    setUsername(loginUsername);
  }, []);

  const signup = useCallback(async (signupUsername: string, password: string, email: string) => {
    await authApi.signup({ username: signupUsername, password, email });
  }, []);

  const logout = useCallback(() => {
    setStoredToken(null);
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
