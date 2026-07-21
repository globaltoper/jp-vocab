import type { ApiErrorBody, LoginResponse } from "./types";

const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8080/api";

const ACCESS_TOKEN_KEY = "jpvocab_access_token";
const REFRESH_TOKEN_KEY = "jpvocab_refresh_token";

// 세션이 완전히 끊겼을 때(리프레시도 실패) 앱 전체에 알리는 이벤트 이름.
// AuthContext가 이 이벤트를 구독해서 로그인 화면으로 보낸다.
export const SESSION_EXPIRED_EVENT = "jpvocab:session-expired";

export class ApiError extends Error {
  status: number;
  code: string;

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.status = body.status;
    this.code = body.code;
  }
}

export function getStoredAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getStoredRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setStoredTokens(accessToken: string | null, refreshToken: string | null): void {
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }

  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

function clearSession(): void {
  setStoredTokens(null, null);
  window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
}

// 동시에 여러 요청이 401을 맞아도 /auth/refresh는 딱 한 번만 나가도록 진행 중인 refresh를 공유한다.
let refreshInFlight: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) {
    return null;
  }

  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });

        if (!response.ok) {
          clearSession();
          return null;
        }

        const data = (await response.json()) as LoginResponse;
        setStoredTokens(data.accessToken, data.refreshToken);
        return data.accessToken;
      } catch {
        clearSession();
        return null;
      } finally {
        refreshInFlight = null;
      }
    })();
  }

  return refreshInFlight;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  auth?: boolean; // true: 토큰 있으면 첨부 (없어도 그냥 진행, 서버가 401/그대로 처리)
}

async function doFetch(path: string, options: Required<RequestOptions>, accessToken: string | null) {
  const headers: Record<string, string> = {};
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (options.auth && accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  return fetch(`${API_BASE_URL}${path}`, {
    method: options.method,
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });
}

async function parseBody<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T;
  }
  const isJson = response.headers.get("content-type")?.includes("application/json");
  return (isJson ? await response.json() : undefined) as T;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const resolved: Required<RequestOptions> = {
    method: options.method ?? "GET",
    body: options.body,
    auth: options.auth ?? true,
  };

  let response = await doFetch(path, resolved, getStoredAccessToken());

  // 인증이 필요한 요청인데 액세스 토큰이 만료돼서 401이 온 경우: 리프레시 후 딱 한 번만 재시도.
  if (response.status === 401 && resolved.auth && getStoredRefreshToken()) {
    const newAccessToken = await refreshAccessToken();
    if (newAccessToken) {
      response = await doFetch(path, resolved, newAccessToken);
    }
  }

  const data = await parseBody<T | ApiErrorBody>(response);

  if (!response.ok) {
    if (data && typeof data === "object" && "code" in data) {
      throw new ApiError(data as ApiErrorBody);
    }
    throw new ApiError({ status: response.status, code: "UNKNOWN_ERROR", message: "요청에 실패했습니다." });
  }

  return data as T;
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: "POST", body }),
  del: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
