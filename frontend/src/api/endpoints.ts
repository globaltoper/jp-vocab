import { apiClient } from "./client";
import type {
  JlptLevel,
  LoginRequest,
  LoginResponse,
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
