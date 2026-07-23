# 배포 가이드

Railway(백엔드 + MySQL) + Vercel(프론트엔드) 조합 기준. 둘 다 무료/최소 비용으로 시작할 수 있고 GitHub 연동만으로 배포됩니다.

## 0. GitHub에 올리기

이 프로젝트는 아직 커밋된 적이 없는 로컬 git 저장소입니다. **터미널(또는 IntelliJ 터미널)에서 직접** 아래를 실행하세요 (이 부분은 본인 계정 인증이 필요해서 제가 대신 못 해드립니다).

```bash
cd ~/dev/projects/jp-vocab-backend

# IntelliJ가 git 작업 중이었다면 잠긴 락 파일이 남아있을 수 있습니다. 있으면 지워주세요.
rm -f .git/index.lock

git add -A
git commit -m "Initial commit: JP vocab backend + frontend"
```

GitHub에서 새 저장소를 만든 뒤(Add README 등 옵션은 전부 체크 해제, 빈 저장소로 생성):

```bash
git remote add origin https://github.com/<your-username>/jp-vocab-backend.git
git branch -M main
git push -u origin main
```

## 1. 백엔드 배포 (Railway)

1. [railway.app](https://railway.app) 로그인 → **New Project** → **Deploy from GitHub repo** → 방금 올린 저장소 선택.
2. 저장소 루트에 `Dockerfile`이 있으므로 Railway가 자동으로 감지해서 Docker 빌드로 배포합니다. Root Directory는 비워두세요(백엔드가 저장소 루트 기준).
3. 같은 프로젝트에 **+ New → Database → Add MySQL** 로 MySQL 플러그인 추가.
4. 백엔드 서비스의 **Variables** 탭에서 아래 환경변수를 설정하세요.

   **주의**: Railway MySQL 플러그인이 주는 `MYSQL_URL`/`MYSQL_PUBLIC_URL`은 `mysql://user:pass@host:port/db` 형태라 JDBC가 요구하는 `jdbc:mysql://...` 접두사가 없습니다. 이 값을 `SPRING_DATASOURCE_URL`에 그대로 넣으면 접속에 실패하니, 아래처럼 `MYSQLHOST`/`MYSQLPORT`/`MYSQLDATABASE` 변수를 조합해서 `jdbc:mysql://`를 직접 붙여주세요 (Railway가 `${{MySQL.변수명}}` 참조 문법을 자동완성해줍니다):

   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `SPRING_DATASOURCE_URL` = `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul`
   - `SPRING_DATASOURCE_USERNAME` = `${{MySQL.MYSQLUSER}}`
   - `SPRING_DATASOURCE_PASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
   - `JWT_SECRET` = (직접 만든 랜덤 문자열, 최소 32자 이상 권장)
   - `CORS_ALLOWED_ORIGINS` = 일단 `http://localhost:5173`로 두고, 4단계에서 Vercel 도메인이 생기면 그걸로 업데이트
5. 배포가 끝나면 **Settings → Networking → Generate Domain**으로 공개 URL을 받습니다. 예: `https://jp-vocab-backend-production.up.railway.app`
6. `https://<railway-domain>/api/words/random` 로 접속해서 JSON이 내려오는지 확인하세요.

> Railway MySQL 플러그인이 실제로 어떤 변수명을 제공하는지는 MySQL 서비스의 **Variables** 탭에서 꼭 한 번 확인하세요. 버전에 따라 이름이 조금씩 다를 수 있습니다.

## 2. VOICEVOX 엔진 배포 (선택 - 발음 품질 개선)

발음 듣기는 3단계로 처리됩니다: ① `src/main/resources/tts-cache`에 미리 만들어 커밋해둔 파일(배포 이미지에 포함) → ② 이번 서버 프로세스가 새로 만든 런타임 캐시 → ③ VOICEVOX 실시간 합성(그마저 실패하면 프런트가 브라우저 기본 음성으로 자동 전환). 즉 VOICEVOX 없이도 앱은 항상 정상 동작합니다.

**`scripts/warm_tts_cache.py`로 배포 전에 캐시를 미리 다 채워뒀다면(README.md 참고), 이 앱이 말하는 문장은 거의 다 고정 콘텐츠라서 실제 운영 중 VOICEVOX가 호출될 일이 거의 없습니다.** 그래서 프로덕션에 VOICEVOX 서비스를 아예 안 띄워도 되고, 이 경우 아래 단계는 전부 건너뛰어도 됩니다(비용 절감). 새 단어/문장을 나중에 추가할 계획이라면, VOICEVOX는 로컬에서 그때그때 warm-up 스크립트를 돌릴 때만 잠깐 띄우면 충분합니다.

그래도 실시간 합성 폴백까지 운영 환경에서 살려두고 싶다면:

1. Railway 같은 프로젝트 안에서 **+ New → Empty Service**(또는 Docker Image 배포) → Docker 이미지로 `voicevox/voicevox_engine:cpu-latest` 지정.
2. 이 서비스는 **Public Networking을 켜지 마세요** - 외부에 노출할 필요가 없고, 인증이 없는 엔진이라 열어두면 아무나 리소스를 씁니다. 백엔드 서비스에서 Railway의 내부 네트워크 주소(`http://<voicevox-서비스명>.railway.internal:50021`)로 접근하게 합니다.
3. 백엔드 서비스의 **Variables**에 추가:
   - `VOICEVOX_BASE_URL` = `http://<voicevox-서비스명>.railway.internal:50021`
4. 백엔드를 재배포하면 끝. `/api/tts/speak`가 정상 응답하면 성공입니다.

`TTS_CACHE_DIR` 환경변수는 굳이 안 정해줘도 됩니다(기본값 `/tmp/jpvocab-tts-cache` - 컨테이너 내 임시 폴더, 재배포하면 사라지는 런타임 전용 캐시일 뿐 실제 캐시는 ①번 번들 캐시가 담당).

## 3. 프론트엔드 배포 (Vercel)

1. [vercel.com](https://vercel.com) 로그인 → **Add New → Project** → 같은 GitHub 저장소 선택.
2. **Root Directory**를 `frontend`로 지정 (모노레포이므로 필수).
3. Framework Preset은 Vite로 자동 인식됩니다. Build Command `npm run build`, Output Directory `dist` 그대로 두면 됩니다.
4. **Environment Variables**에 추가:
   - `VITE_API_BASE_URL` = `https://<railway-domain>/api` (1단계에서 받은 백엔드 URL)
5. Deploy. 완료되면 `https://<프로젝트명>.vercel.app` 같은 도메인이 생깁니다.

## 4. CORS 연결 마무리

Railway 백엔드 서비스의 `CORS_ALLOWED_ORIGINS` 환경변수를 Vercel 도메인으로 업데이트하고 재배포하세요.

```
CORS_ALLOWED_ORIGINS=https://<프로젝트명>.vercel.app,http://localhost:5173
```

(패턴 매칭을 지원하므로 `https://*.vercel.app`처럼 와일드카드로 넣으면 PR 프리뷰 배포 도메인까지 한 번에 허용됩니다.)

## 5. 최종 확인

1. Vercel 도메인 접속 → 랜덤 단어 카드가 뜨는지 확인
2. 회원가입 → 로그인 → 단어 저장 → 내 단어장/복습 페이지 동작 확인
3. 안 되면 브라우저 개발자도구 Network 탭에서 CORS 에러인지, 401/404인지 확인 후 알려주시면 같이 봐드릴게요.

## 참고

- `ddl-auto`는 로컬(`create-drop`, 매번 초기화)과 배포(`update`, 데이터 보존)가 다르게 설정되어 있습니다(`application-prod.yml`). `data.sql`도 `INSERT IGNORE`라 재배포해도 시드 데이터가 중복 삽입되지 않습니다.
- 다른 플랫폼(Render, Fly.io, AWS 등)을 쓰고 싶으면 `Dockerfile`이 이미 있으니 Docker를 지원하는 곳이면 거의 동일한 방식으로 배포할 수 있습니다.
