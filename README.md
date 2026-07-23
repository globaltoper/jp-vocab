# 일본어 단어장 (JP Vocab)

JLPT 학습자를 위한 개인 단어장 웹 서비스. Spring Boot + MySQL 백엔드와 React + TypeScript 프론트엔드로 구성된 풀스택 프로젝트입니다.

## 구조

```
jp-vocab-backend/
├── src/main/java/com/toper/jpvocab/   # Spring Boot 백엔드
├── src/main/resources/
│   ├── application.yml
│   └── data.sql                       # 시드 데이터 (단어 110개 + 예문, N5~N1)
└── frontend/                          # React + TypeScript (Vite)
```

## 백엔드 실행

1. 로컬 MySQL이 실행 중이어야 합니다. 기본 접속 정보(`application.yml`)는 `localhost:3306`, DB `jpvocab`(자동 생성), 계정 `root/root` 입니다. 본인 환경에 맞게 `src/main/resources/application.yml`의 `spring.datasource`를 수정하세요.
2. 실행:
   ```bash
   ./gradlew bootRun
   ```
3. 로컬 기본(`ddl-auto: create-drop`)은 매 기동 시 테이블을 재생성합니다. 데이터를 보존하고 싶으면 `SPRING_PROFILES_ACTIVE=prod`로 실행하세요(`application-prod.yml`에서 `ddl-auto: update`로 덮어씀). `data.sql`은 `INSERT IGNORE`로 작성되어 있어 두 경우 모두 안전합니다.
4. JWT 시크릿은 `JWT_SECRET` 환경변수로 덮어쓸 수 있습니다(기본값은 개발용 더미 값).
5. 서버가 뜨면 `http://localhost:8080/swagger-ui/index.html`에서 전체 API 문서(Swagger UI)를 확인하고 바로 테스트할 수 있습니다. 인증이 필요한 API는 오른쪽 위 **Authorize** 버튼에 `/api/auth/login`으로 받은 `accessToken` 값을 넣으면 됩니다(`Bearer ` 접두사는 자동으로 붙습니다). OpenAPI 원본 JSON은 `/v3/api-docs`.

## 프론트엔드 실행

```bash
cd frontend
rm -rf node_modules package-lock.json   # 최초 1회: 개발 중 생성된 node_modules를 깨끗이 지우고 시작
npm install
npm run dev
```

기본적으로 `http://localhost:8080/api`를 백엔드 주소로 사용합니다. 다른 주소를 쓰려면 `frontend/.env.example`을 참고해 `.env`에 `VITE_API_BASE_URL`을 설정하세요.

## 발음(TTS) - VOICEVOX 실행 (선택)

단어/예문/딕테이션 발음 듣기는 [VOICEVOX](https://voicevox.hiroshiba.jp/) 엔진을 프록시해서 재생합니다. VOICEVOX는 무료 오픈소스 일본어 음성 합성 엔진으로, 브라우저 기본 TTS보다 훨씬 자연스러운 발음을 냅니다.

1. Docker Desktop이 설치되어 있어야 합니다.
2. 백엔드를 띄우기 전에 VOICEVOX 엔진을 별도 컨테이너로 실행:
   ```bash
   docker run --rm -p 127.0.0.1:50021:50021 voicevox/voicevox_engine:cpu-latest
   ```
   (Apple Silicon Mac도 이 이미지로 동작합니다. 첫 실행은 이미지를 받느라 시간이 걸릴 수 있습니다.)
3. 정상 기동되면 `http://127.0.0.1:50021/docs`에서 API 문서를 볼 수 있고, `curl http://127.0.0.1:50021/speakers`로 사용 가능한 목소리 목록을 확인할 수 있습니다.
4. 백엔드는 기본값으로 `http://localhost:50021`을 사용합니다(`VOICEVOX_BASE_URL` 환경변수로 변경 가능).

**VOICEVOX를 안 띄워도 앱은 정상 동작합니다.** 아래 세 단계로 음성을 찾고, 전부 실패해야만 실시간 합성을 시도합니다.

1. `src/main/resources/tts-cache/`에 미리 만들어 커밋해둔 파일(배포 이미지에 포함됨)
2. 이번 서버 프로세스가 실행 중 새로 만든 런타임 캐시 파일
3. 그래도 없으면 VOICEVOX 엔진에 실시간 합성 요청 - 이마저 실패하면(꺼져있는 등) 프런트가 자동으로 브라우저 기본 음성으로 전환

### 음성 성별 선택

내비게이션 바에서 **여성 음성**(四国めたん) / **남성 음성**(玄野武宏) 중 고를 수 있습니다. 선택은 브라우저에 저장되어 계속 유지됩니다.

### 음성 캐시 미리 만들기 (`scripts/warm_tts_cache.py`)

단어/예문/딕테이션 문장은 전부 `data.sql`에 고정으로 들어있는 콘텐츠라서, 매번 실시간으로 합성할 필요 없이 미리 다 만들어 캐시로 저장해둘 수 있습니다.

```bash
# 1) 로컬 VOICEVOX 도커 실행 (위 2번 참고)
# 2) 캐시가 바로 src/main/resources/tts-cache 에 쌓이도록 지정해서 백엔드 실행
TTS_CACHE_DIR=src/main/resources/tts-cache ./gradlew bootRun

# 3) 다른 터미널에서 (전체 콘텐츠 × 여성/남성 음성 전부 생성, 몇 분~몇십 분 소요)
python3 scripts/warm_tts_cache.py

# 4) 새로 생긴 .wav 파일들을 커밋
git add src/main/resources/tts-cache
git commit -m "chore: warm up tts cache"
```

이미 있는 캐시 파일은 건너뛰므로, 새 단어/문장을 추가한 뒤 다시 실행해도 새로 추가된 것만 생성됩니다. 자세한 내용은 `src/main/resources/tts-cache/README.md` 참고.

### 크레딧 표시 (필수)

VOICEVOX 이용약관상 캐릭터 음성을 쓰는 경우 크레딧 표시가 필수입니다(`VOICEVOX:四国めたん`, `VOICEVOX:玄野武宏`). 이 프로젝트는 프런트엔드 하단 `Footer` 컴포넌트에 이미 표시해뒀습니다 — 다른 캐릭터로 바꾸거나 새 목소리를 추가하면 `Footer.tsx`의 문구도 같이 업데이트하세요.

## 배포

Railway(백엔드+MySQL) + Vercel(프론트엔드)로 배포하는 단계별 가이드는 [`DEPLOYMENT.md`](./DEPLOYMENT.md) 참고.

## 참고 (명세와 다르게 구현한 부분)

- `example_sentence_words`에 `position` 대신 `start_index`/`end_index` 두 컬럼을 두었습니다. API 응답의 `linkedWords`가 시작/끝 인덱스를 모두 요구하기 때문입니다.
- 별도의 `ExampleController`는 만들지 않았습니다. API 명세상 예문은 단어 상세 조회(`GET /api/words/{wordId}`) 응답에 포함되는 형태로만 노출되기 때문입니다.
- Java 25 대신 21(LTS) 툴체인을 사용했습니다. 라이브러리/툴 생태계 호환성이 더 넓고 안정적입니다. `build.gradle`의 `JavaLanguageVersion`만 바꾸면 25로 전환할 수 있습니다.
- 이 개발 환경은 Maven Central/Gradle 배포 서버에 대한 네트워크 접근이 막혀 있어 `./gradlew build`로 백엔드 컴파일을 직접 검증하지 못했습니다. 코드는 꼼꼼히 검토했지만, 로컬(IntelliJ 등)에서 첫 빌드 시 오류가 나면 알려주세요. 프론트엔드 코드는 별도 환경에서 `npm run build`로 정상 빌드를 확인했습니다.
- `frontend/node_modules`는 이 개발 환경의 파일 삭제 제약 때문에 일부 손상되어 있을 수 있습니다(`rolldown` 네이티브 바이너리 오류가 날 수 있음). 처음 받으면 위 안내대로 `node_modules`를 지우고 `npm install`을 새로 하면 정상 동작합니다.
- 복습 기능(라이트너 박스): 단어 저장 시 자동으로 복습 스케줄이 생성됩니다. `GET /api/reviews/due`, `GET /api/reviews/due/count`, `POST /api/reviews/{wordId}/result`(`{ "remembered": true|false }`) 3개 API와 프론트 `/review` 페이지가 추가되었습니다.
- 인증: 로그인 시 액세스 토큰(30분)과 리프레시 토큰(14일, DB 저장)을 함께 발급합니다. `POST /api/auth/refresh`로 재발급(회전), `POST /api/auth/logout`으로 폐기. 프론트는 401을 받으면 자동으로 리프레시 후 재시도합니다.
- 회원가입 확장 필드: 생일/전화번호/약관동의(필수)/추천인/희망레벨/현재레벨/하루목표단어수/유입경로.
- 이메일 인증·아이디 찾기·비밀번호 재설정은 **실제 메일 발송 없이 mock**으로 구현되어 있습니다 — 서버 로그(`[MOCK EMAIL] ...`)와 API 응답에 토큰/아이디가 그대로 노출됩니다(`MockEmailService` 참고). 실제 서비스로 전환하려면 `MockEmailService` 내부만 SMTP 연동으로 교체하고, `FindUsernameResponse`/`PasswordResetRequestResponse`에서 민감한 필드를 제거하면 됩니다.
- 딕테이션(`/dictation`)·타자연습(`/typing-practice`): `dictation_sentences` 테이블에 있는 전용 문장(단어별 예문과 별개, N5~N1 212개 시드)을 듣고 로마자로 받아쓰는 기능입니다. 로마자→히라가나 변환은 `wanakana` 라이브러리(프런트)가 실시간으로 처리합니다. 딕테이션은 정답(읽기)을 채점 전에 절대 응답에 포함하지 않고, 후리가나 기준으로 관대하게(Levenshtein 유사도) 채점합니다. 타자연습은 정답을 처음부터 보여주고 정확한 재현 속도(CPM)를 측정하는 별도 엔드포인트(`/api/dictation/practice-random`)를 씁니다.
- 발음 듣기는 VOICEVOX 프록시(`/api/tts/speak`)로 고품질 음성을 재생하고, 실패 시 브라우저 기본 TTS로 자동 폴백합니다. 여성(四国めたん)/남성(玄野武宏) 음성을 내비게이션 바에서 선택할 수 있고, 고정 콘텐츠는 `scripts/warm_tts_cache.py`로 미리 생성해 `tts-cache`에 캐싱합니다. 위 "발음(TTS)" 섹션 참고.
- API 문서는 Swagger UI(springdoc-openapi)로 자동 생성됩니다. `./gradlew bootRun` 후 `http://localhost:8080/swagger-ui/index.html` 참고. Spring Boot 3.3.x와의 호환 때문에 springdoc 버전은 `2.6.0`으로 고정했습니다(2.7.0+는 Spring Boot 3.4+ 전용).
