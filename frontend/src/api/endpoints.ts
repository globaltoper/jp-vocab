import { apiClient } from "./client";
import type {
  FindUsernameResponse,
  JlptLevel,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  PasswordResetRequestResponse,
  ReviewDueCount,
  ReviewWord,
  SavedWordCreated,
  SavedWordItem,
  SignupRequest,
  SignupResponse,
  WordCard,
  WordDetail,
  WordPage,
} from "./types";

export const authApi = {
  signup: (payload: SignupRequest) => apiClient.post<SignupResponse>("/auth/signup", payload),
  login: (payload: LoginRequest) => apiClient.post<LoginResponse>("/auth/login", payload),
  // /auth/logout은 서버에서 permitAll이라 액세스 토큰이 만료된 상태여도 정상 동작한다.
  // (실제로 필요한 건 refreshToken뿐 - 그걸 DB에서 폐기 처리한다)
  logout: (refreshToken: string) => apiClient.post<void>("/auth/logout", { refreshToken }),
  verifyEmail: (token: string) => apiClient.post<MessageResponse>("/auth/verify-email", { token }),
  findUsername: (email: string) =>
    apiClient.post<FindUsernameResponse>("/auth/find-username", { email }),
  requestPasswordReset: (email: string) =>
    apiClient.post<PasswordResetRequestResponse>("/auth/password-reset/request", { email }),
  confirmPasswordReset: (token: string, newPassword: string) =>
    apiClient.post<MessageResponse>("/auth/password-reset/confirm", { token, newPassword }),
};

export const wordApi = {
  getRandom: (level?: JlptLevel) =>
    apiClient.get<WordCard>(`/words/random${level ? `?level=${level}` : ""}`),
  getDetail: (wordId: number) => apiClient.get<WordDetail>(`/words/${wordId}`),
  getList: (level: JlptLevel | undefined, page: number, size: number) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (level) params.set("level", level);
    return apiClient.get<WordPage>(`/words?${params.toString()}`);
  },
};

export const savedWordApi = {
  save: (wordId: number) => apiClient.post<SavedWordCreated>("/saved-words", { wordId }),
  list: () => apiClient.get<SavedWordItem[]>("/saved-words"),
  remove: (savedWordId: number) => apiClient.del<void>(`/saved-words/${savedWordId}`),
};

export const reviewApi = {
  getDue: () => apiClient.get<ReviewWord[]>("/reviews/due"),
  getDueCount: () => apiClient.get<ReviewDueCount>("/reviews/due/count"),
  submitResult: (wordId: number, remembered: boolean) =>
    apiClient.post<ReviewWord>(`/reviews/${wordId}/result`, { remembered }),
};
