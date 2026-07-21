export type JlptLevel = "N5" | "N4" | "N3" | "N2" | "N1";

export interface WordCard {
  id: number;
  expression: string;
  furigana: string;
  meaning: string;
  level: JlptLevel;
  partOfSpeech: string;
  isSaved: boolean;
}

export interface LinkedWord {
  wordId: number;
  expression: string;
  startIndex: number;
  endIndex: number;
}

export interface ExampleSentenceData {
  id: number;
  sentenceJp: string;
  sentenceReading: string;
  sentenceMeaning: string;
  linkedWords: LinkedWord[];
}

export interface WordDetail extends WordCard {
  examples: ExampleSentenceData[];
}

export interface WordSummary {
  id: number;
  expression: string;
  furigana: string;
  meaning: string;
  level: JlptLevel;
}

export interface WordPage {
  content: WordSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SavedWordItem {
  id: number;
  wordId: number;
  expression: string;
  furigana: string;
  meaning: string;
  level: JlptLevel;
  savedAt: string;
}

export interface SavedWordCreated {
  id: number;
  wordId: number;
  expression: string;
  savedAt: string;
}

export type ReferralSource = "SEARCH" | "SNS" | "FRIEND" | "AD" | "OTHER";

export interface SignupRequest {
  username: string;
  password: string;
  email: string;
  birthDate?: string; // "YYYY-MM-DD"
  phoneNumber?: string;
  termsAgreed: boolean;
  referrerUsername?: string;
  targetLevel?: JlptLevel;
  currentLevel?: JlptLevel;
  dailyGoalCount?: number;
  referralSource?: ReferralSource;
}

export interface SignupResponse {
  id: number;
  username: string;
  email: string;
  targetLevel: JlptLevel | null;
  currentLevel: JlptLevel | null;
  dailyGoalCount: number | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface ApiErrorBody {
  status: number;
  code: string;
  message: string;
}

export interface ReviewWord {
  wordId: number;
  expression: string;
  furigana: string;
  meaning: string;
  level: JlptLevel;
  partOfSpeech: string;
  boxLevel: number;
  nextReviewAt: string;
}

export interface ReviewDueCount {
  count: number;
}

export interface MessageResponse {
  message: string;
}

export interface FindUsernameResponse {
  username: string;
}

export interface PasswordResetRequestResponse {
  message: string;
  // mock 단계라 토큰이 응답에 바로 온다. 실제 메일 발송으로 바뀌면 이 필드는 사라진다.
  token: string;
}
