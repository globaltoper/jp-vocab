# 테스트케이스 (JP Vocab)

도메인별 정상/예외/경계 케이스. TC ID 규칙: `{도메인 약어}-{번호}`.

## 1. 회원/인증 (AUTH)

| TC ID | 제목 | 사전조건 | 입력/절차 | 기대 결과 |
|---|---|---|---|---|
| AUTH-01 | 정상 회원가입 | - | 필수값(username, password 8자+, email, termsAgreed=true) 정상 입력 후 POST /signup | 201, User 저장, `emailVerified=false` |
| AUTH-02 | 중복 아이디 회원가입 | 동일 username 이미 존재 | POST /signup | 409(또는 정의된 코드), `USERNAME_ALREADY_EXISTS` |
| AUTH-03 | 약관 미동의 회원가입 | - | `termsAgreed=false`로 POST /signup | 400, 검증 실패 메시지 |
| AUTH-04 | 비밀번호 8자 미만 | - | `password="1234567"` | 400, `INVALID_INPUT` |
| AUTH-05 | 이메일 형식 오류 | - | `email="not-an-email"` | 400, `INVALID_INPUT` |
| AUTH-06 | 추천인 아이디 지정 회원가입 | referrer username 존재 | `referrerUsername` 포함 POST /signup | 201, `referrer_id` FK 정상 연결 |
| AUTH-07 | 존재하지 않는 추천인 지정 | - | `referrerUsername="ghost"` | 201, 가입은 그대로 성공하고 `referrer=null`로 저장(가입 자체를 막지 않는 정책) |
| AUTH-08 | 회원가입 시 이메일 인증 메일 발송(mock) | - | POST /signup | 서버 로그에 `[MOCK EMAIL]` 출력, `EmailVerificationToken` 생성 |
| AUTH-09 | 정상 로그인 | 가입된 계정 | POST /login (올바른 username/password) | 200, accessToken + refreshToken 반환 |
| AUTH-10 | 비밀번호 틀림 | 가입된 계정 | POST /login (잘못된 password) | 401, `INVALID_CREDENTIALS` |
| AUTH-11 | 존재하지 않는 아이디 로그인 | - | POST /login (없는 username) | 401, `INVALID_CREDENTIALS`(계정 존재 여부 노출 안 함) |
| AUTH-12 | 정상 토큰 리프레시 | 유효한 refreshToken 보유 | POST /refresh | 200, 새 accessToken/refreshToken, 기존 refreshToken은 `revoked=true`로 전환 |
| AUTH-13 | 폐기된 리프레시 토큰 재사용 | 이미 사용(회전)된 refreshToken | POST /refresh (옛 토큰) | 401, `INVALID_REFRESH_TOKEN` |
| AUTH-14 | 만료된 리프레시 토큰 | expiresAt 지남 | POST /refresh | 401, `INVALID_REFRESH_TOKEN` |
| AUTH-15 | 정상 로그아웃 | 유효한 refreshToken | POST /logout | 204, 해당 refreshToken `revoked=true` |
| AUTH-16 | 액세스 토큰 만료 후 API 호출 | accessToken 만료됨 | GET /api/saved-words (만료 토큰) | 401, 프론트는 자동으로 /refresh 후 재시도 |
| AUTH-17 | Authorization 헤더 없이 보호 API 호출 | - | GET /api/saved-words (헤더 없음) | 401 |
| AUTH-18 | 정상 이메일 인증 | 유효한 verification token | POST /verify-email | 200, `User.emailVerified=true` |
| AUTH-19 | 만료/잘못된 인증 토큰 | - | POST /verify-email (invalid token) | 400, `INVALID_VERIFICATION_TOKEN` |
| AUTH-20 | 아이디 찾기(존재하는 이메일) | - | POST /find-username | 200, username 반환(mock 단계) |
| AUTH-21 | 아이디 찾기(존재하지 않는 이메일) | - | POST /find-username | 404 또는 정의된 에러(`USER_NOT_FOUND_BY_EMAIL`) |
| AUTH-22 | 비밀번호 재설정 요청 | 가입된 이메일 | POST /password-reset/request | 200, `PasswordResetToken` 생성, mock 응답에 token 포함 |
| AUTH-23 | 비밀번호 재설정 확인 | 유효한 reset token | POST /password-reset/confirm | 200, 비밀번호 변경, 해당 유저의 모든 RefreshToken 전체 폐기(다른 기기 강제 로그아웃) |
| AUTH-24 | 이미 사용된 재설정 토큰 재사용 | `used=true` 토큰 | POST /password-reset/confirm | 400, `INVALID_PASSWORD_RESET_TOKEN` |
| AUTH-25 | 만료된 재설정 토큰 | expiresAt 지남(30분) | POST /password-reset/confirm | 400, `INVALID_PASSWORD_RESET_TOKEN` |

## 2. 단어 조회 (WORD)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| WORD-01 | 랜덤 단어 조회(레벨 미지정) | GET /words/random | 200, 전체 레벨 중 랜덤 1개 |
| WORD-02 | 랜덤 단어 조회(레벨 지정) | GET /words/random?level=N3 | 200, `level=N3`인 단어만 반환 |
| WORD-03 | 랜덤 단어 조회(비로그인) | 토큰 없이 GET /words/random | 200, `isSaved=false` 고정 |
| WORD-04 | 랜덤 단어 조회(로그인, 저장한 단어) | 로그인 + 해당 단어 저장 이력 있음 | 200, `isSaved=true` |
| WORD-05 | 단어 상세 조회 | GET /words/{id} | 200, 예문 목록 + `linkedWords`(클릭 가능 인덱스) 포함 |
| WORD-06 | 존재하지 않는 단어 상세 조회 | GET /words/999999 | 404, `WORD_NOT_FOUND` |
| WORD-07 | 단어 목록 페이지네이션 | GET /words?page=0&size=20 | 200, `totalElements`/`totalPages` 정확히 계산 |
| WORD-08 | 단어 목록 레벨 필터 | GET /words?level=N5 | 200, N5만 반환 |
| WORD-09 | 페이지 범위 초과 조회 | GET /words?page=9999 | 200, 빈 `content` 배열(에러 아님) |

## 3. 내 단어장 (SAVEDWORD)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| SW-01 | 단어 저장 | POST /saved-words {wordId} | 201, SavedWord 생성 |
| SW-02 | 이미 저장한 단어 재저장 | 동일 wordId로 다시 POST | 409, `ALREADY_SAVED` (user_id+word_id 유니크 제약) |
| SW-03 | 존재하지 않는 단어 저장 시도 | POST /saved-words {wordId: 999999} | 404, `WORD_NOT_FOUND` |
| SW-04 | 비로그인 상태로 저장 시도 | 토큰 없이 POST /saved-words | 401 |
| SW-05 | 내 단어장 목록 조회 | GET /saved-words | 200, 저장한 단어 전체 반환 |
| SW-06 | 저장한 단어 없을 때 목록 조회 | GET /saved-words (저장 이력 없음) | 200, 빈 배열 |
| SW-07 | 단어 저장 취소 | DELETE /saved-words/{savedWordId} | 204, 레코드 삭제 |
| SW-08 | 타인이 저장한 단어 삭제 시도 | 다른 유저 소유 savedWordId로 DELETE | 403, `FORBIDDEN`(본인 소유 아님) |
| SW-09 | 존재하지 않는 savedWordId 삭제 | DELETE /saved-words/999999 | 404, `SAVED_WORD_NOT_FOUND` |
| SW-10 | 단어 저장 시 복습 스케줄 자동 생성 | POST /saved-words | ReviewSchedule이 box=1로 자동 생성됨(연쇄 동작 검증) |

## 4. 복습 - 라이트너 박스 (REVIEW)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| REV-01 | 오늘 복습할 단어 조회 | GET /reviews/due | 200, `nextReviewAt <= now`인 항목만 반환 |
| REV-02 | 복습할 단어 개수 조회 | GET /reviews/due/count | 200, count가 REV-01 결과 개수와 일치 |
| REV-03 | 복습 결과 제출(기억함) | POST /reviews/{wordId}/result {remembered:true} | 200, boxLevel+1(최대 5), nextReviewAt이 다음 간격만큼 증가 |
| REV-04 | 복습 결과 제출(못 외움) | POST /reviews/{wordId}/result {remembered:false} | 200, boxLevel=1로 초기화 |
| REV-05 | 최고 박스(5)에서 기억함 제출 | boxLevel=5인 상태 | POST .../result {remembered:true} | boxLevel 5 유지(상한), interval 30일 적용 |
| REV-06 | 복습 스케줄 없는 단어에 결과 제출 | 저장한 적 없는 wordId로 POST | 404, `REVIEW_NOT_FOUND` |
| REV-07 | 타인의 복습 스케줄에 접근 | 다른 유저 소유 wordId | 403 또는 404(본인 스케줄만 노출) |
| REV-08 | 비로그인 상태로 복습 API 호출 | 토큰 없이 GET /reviews/due | 401 |

## 5. 딕테이션 (DICTATION)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| DIC-01 | 랜덤 딕테이션 문장 조회 | GET /dictation/random | 200, 응답에 `sentenceReading`(정답) 미포함 확인 |
| DIC-02 | 레벨 지정 문장 조회 | GET /dictation/random?level=N2 | 200, N2 문장만 반환 |
| DIC-03 | 정답 제출(완전 일치) | typedReading == 정답 | POST /{id}/attempt | 200, `accuracyPercent=100` |
| DIC-04 | 정답 제출(일부 오타) | typedReading이 정답과 90% 유사 | POST /{id}/attempt | 200, 0 < accuracy < 100 (Levenshtein 기반 관대한 채점) |
| DIC-05 | 빈 문자열 제출 | typedReading="" | POST /{id}/attempt | 200, `accuracyPercent` 낮음, `cpm=0` |
| DIC-06 | 존재하지 않는 문장 ID로 제출 | POST /dictation/999999/attempt | 404, `DICTATION_SENTENCE_NOT_FOUND` |
| DIC-07 | 비로그인 상태로 제출 | 토큰 없이 POST /attempt | 200, 채점은 되지만 `saved=false`(기록 미저장) |
| DIC-08 | 로그인 상태로 제출 | 토큰 있음 | POST /attempt | 200, `saved=true`, DictationAttempt 레코드 생성 |
| DIC-09 | elapsedMs 음수 입력 | `elapsedMs=-1` | POST /attempt | 400, `INVALID_INPUT`(`@PositiveOrZero` 검증) |
| DIC-10 | CPM 계산 검증 | typedReading 10자, elapsedMs=60000(1분) | POST /attempt | `cpm=10` |
| DIC-11 | 딕테이션 기록 조회 | 로그인 + 제출 이력 있음 | GET /dictation/history | 200, 최근 20개까지 내림차순 |
| DIC-12 | 비로그인 상태로 기록 조회 | 토큰 없이 GET /history | 401 |
| DIC-13 | 마침표/쉼표 포함 정답 채점 | 정답에 「。」「、」포함, 입력값도 포함 | POST /attempt | 정규화(공백/구두점 제거) 후 비교되어 정상 채점 |

## 6. 타자연습 (TYPING)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| TYP-01 | 타자연습용 문장 조회 | GET /dictation/practice-random | 200, `sentenceReading`(정답) **포함**되어 응답(딕테이션과 차이점) |
| TYP-02 | 레벨 지정 조회 | GET /dictation/practice-random?level=N5 | 200, N5만 반환 |
| TYP-03 | 로마자 입력 → 히라가나 변환(프론트) | "kyouhatenkigaiidesune." 입력 | wanakana가 "きょうはてんきがいいですね。"로 실시간 변환 |
| TYP-04 | 정확히 일치하는 입력 완료 | 변환된 값 == sentenceReading | 프론트에서 자동으로 완료 처리, CPM 계산 |

## 7. TTS (VOICEVOX 프록시)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| TTS-01 | 정상 TTS 요청 | POST /tts/speak {text, speedScale:1.0} | 200, `Content-Type: audio/wav`, 바이너리 응답 |
| TTS-02 | speedScale 미지정 | POST /tts/speak {text} (speedScale 없음) | 200, 기본값 1.0 적용(`resolvedSpeedScale()`) |
| TTS-03 | speedScale 범위 초과 | `speedScale=3.0` | 400, `INVALID_INPUT`(`@DecimalMax(2.0)`) |
| TTS-04 | 빈 텍스트 요청 | `text=""` | 400, `INVALID_INPUT`(`@NotBlank`) |
| TTS-05 | VOICEVOX 엔진 다운 상태 | VOICEVOX 서비스 중지 | POST /tts/speak | 503, `TTS_UNAVAILABLE` |
| TTS-06 | 프론트 폴백 동작 | 백엔드가 503 반환 | 프론트가 자동으로 브라우저 Web Speech API로 재생 전환(에러 노출 없음) |
| TTS-07 | 캐시 재사용 | 동일 (text, speedScale) 재요청 | 프론트가 이전 응답 blob을 재사용(네트워크 재요청 없음) |
| TTS-08 | 비로그인 상태로 TTS 요청 | 토큰 없이 POST /tts/speak | 200(공개 API) |

## 8. 크로스커팅 (보안/공통)

| TC ID | 제목 | 입력/절차 | 기대 결과 |
|---|---|---|---|
| SEC-01 | SQL Injection 시도 | username에 `' OR '1'='1` 입력 후 로그인 | JPA 파라미터 바인딩으로 무력화, 401 |
| SEC-02 | 만료된 JWT로 API 호출 | accessToken 만료 | 401, `UNAUTHORIZED` |
| SEC-03 | 위조된 JWT로 API 호출 | 서명이 다른 토큰 | 401(서명 검증 실패) |
| SEC-04 | CORS 미허용 오리진에서 요청 | `CORS_ALLOWED_ORIGINS`에 없는 도메인 | 브라우저에서 CORS 에러(preflight 차단) |
| SEC-05 | XSS 방지(프론트) | 단어 뜻에 `<script>` 포함된 데이터 렌더링 | React 기본 이스케이프로 스크립트 미실행 |
| SEC-06 | 비밀번호 평문 저장 여부 | 회원가입 후 DB 직접 조회 | `password` 컬럼이 BCrypt 해시 형태(`$2a$...`)인지 확인 |
