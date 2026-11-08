# React + TypeScript + Vite

## 7주차 필수 미션

- 실행: `pnpm install --frozen-lockfile` → `pnpm dev`. 검사: `pnpm lint`, `pnpm build`.
- `/signup`: useState 입력 관리, 워크북 이메일·비밀번호 정규표현식, 비밀번호 최대 72자, 앞뒤 공백을 제거한 닉네임 2~12자, 비밀번호 확인 일치 검증.
- `GET /members/email/{email}`, `GET /members/nickname/{nickname}`의 `{ available: boolean }` 응답을 확인합니다. 입력 변경 시 해당 확인 결과를 초기화하고, 요청 중 모든 입력과 버튼을 비활성화합니다.
- `POST /members`에는 `email`, `nickname`, `password`만 보냅니다. 성공·실패·요청 중 상태를 표시합니다. 비밀번호 확인은 전송하지 않습니다.
- `.env.local`에 `VITE_API_BASE_URL=http://localhost:8080`을 설정합니다. 기존 TMDB 환경변수는 그대로 유지합니다. 실제 비밀 값이 있는 `.env.local`은 커밋하지 않습니다.
- `/login`: 이메일·비밀번호 입력 및 회원가입 링크. 버튼은 실제 로그인을 수행하지 않았다는 안내만 표시합니다. 로그인 API, JWT, 인증 상태를 추가하지 않았습니다.
- `/my`, `/my/edit`: 예시 회원 정보 조회와 닉네임 수정·취소. useState와 내 정보 경로 안의 React Context로 수정한 값을 공유합니다. 새로고침하거나 내 정보 경로를 벗어나면 예시 값으로 초기화됩니다. 회원 정보 API는 호출하지 않습니다.
- 기존 영화·검색·상세·북마크·TanStack Query 코드는 유지합니다. 헤더에는 새 화면 이동 링크를 연결했습니다.

### 검증 결과 (2026-11-08)

- TypeScript, lint, production build: 통과.
- 이 작업 환경의 pnpm 12.4.1 실행기는 lockfile 검사 중 stack overflow로 종료되어, 패키지 설정과 lockfile을 바꾸지 않고 npm으로 의존성을 준비한 뒤 `npm run lint`, `npm run build`로 같은 검사 스크립트를 실행해 통과했습니다.
- 실제 TMDB: 목록 1→2→1 캐시(페이지별 요청 1회), 검색·빈 검색·검색 결과 없음, 상세·잘못된 ID·404, 북마크 새로고침 유지 통과.
- 브라우저: 로그인 입력 및 인증 미수행 안내, 회원가입 검증, 예시 내 정보 조회·수정·취소, 모든 새 경로 직접 접근·새로고침, 375px 화면, 브라우저 오류 없음 통과.
- 모의 API: 중복/사용 가능/잘못된 응답, 입력 변경 시 확인 초기화, 요청 중 입력·버튼 비활성화, POST 필드 확인, 409/500 오류 및 재시도·성공 처리 통과. **모의 응답 결과이며 실서버 성공 인증이 아닙니다.**
- 실제 BE: localhost:8080 연결 실패 및 화면의 오류 표시 확인. **실서버 중복 확인과 회원가입 성공은 미검증**입니다.

### 미션 인증 시 직접 확인

1. 기존 BE를 실행하고 `.env.local`의 주소와 BE CORS 허용 주소를 확인합니다. BE 코드는 변경하지 않았습니다.
2. `/signup`에서 사용 중인 값과 새 값으로 중복 확인합니다. 값을 변경하면 다시 확인해야 합니다.
3. 새 계정으로 가입 후 개발자 도구 Network에서 POST `/members` 상태 코드와 응답을 캡처합니다. 요청에는 세 필드만 있어야 하며, 캡처 시 비밀번호는 가립니다.
4. `/login`, `/my`, `/my/edit` 화면과 닉네임 수정 전후를 캡처합니다. 로그인·내 정보는 API 요청이 없어야 합니다.
5. 모의 API 캡처를 실제 회원가입 성공 증거로 사용하지 않습니다.

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

You can also install [eslint-plugin-react-x](https://npmx.dev/package/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://npmx.dev/package/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

## 5주차 TMDB API
- FE/.env.local에 VITE_TMDB_API_READ_ACCESS_TOKEN을 설정합니다. TMDB 설정 → API의 Read Access Token을 사용하며 실제 토큰은 커밋하지 않습니다.
- pnpm install → pnpm dev, 검증: pnpm lint / pnpm build
- 최종 화면은 ky 2.x 공통 클라이언트로 인기 영화(페이지 이동), 검색, 상세 API를 호출합니다.
- 순수 fetch 실습: 개발 서버가 실행된 앱의 개발자 도구 Console에서 아래 코드를 실행하고 Network의 popular 요청 상태와 응답을 확인합니다. Authorization 헤더는 캡처하지 않습니다.

```js
const { getMoviesWithFetch } = await import('/src/api/movies/get-movies-with-fetch.ts');
const result = await getMoviesWithFetch();
console.log({ page: result.page, count: result.results.length });
```

- 요청 언어: ko-KR. 빈 검색어는 요청하지 않으며 검색어/영화 ID 변경 및 화면 이탈 시 이전 요청을 취소합니다.
- 로컬 영화 ID와 TMDB ID의 충돌을 막기 위해 북마크 저장 키를 umcine-tmdb-bookmark-store로 분리했습니다. 예전 저장 데이터는 삭제하지 않습니다.
- 출처: https://developer.themoviedb.org/docs/image-basics / https://developer.themoviedb.org/docs/faq
- ky 2.x 문법: https://github.com/sindresorhus/ky
