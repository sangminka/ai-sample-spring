# T-1.5 공통 화면 레이아웃 구성 보고서

## 작업 목적

- Mustache 기반 SSR 화면에서 재사용 가능한 공통 레이아웃 뼈대를 먼저 마련한다.
- 이후 `join-form.mustache`, `login-form.mustache`, 게시글 화면들이 동일한 헤더, 푸터, 콘텐츠 셸을 공유하도록 기반을 만든다.
- 임시 수준의 `home.mustache` 단일 페이지 구조를 partial 조합 구조로 전환해 화면 확장 비용을 낮춘다.

## 변경 전 상태

- [home.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/home.mustache)는 단순한 정적 HTML 한 페이지였다.
- 공통 헤더, 푸터, 스타일 시스템, 레이아웃 래퍼가 존재하지 않았다.
- Bootstrap과 같은 공통 UI 프레임워크 연결이 없어서 이후 화면마다 중복 마크업이 생길 가능성이 높았다.
- 네비게이션 구조가 없어 화면 간 이동 기준점이 부재했다.

## 적용한 설계 원칙

- Mustache partial 중심 구조로 분리해 공통 레이아웃 책임을 `header.mustache`, `footer.mustache`에 집중시킨다.
- 화면별 템플릿은 `main` 영역 콘텐츠만 담당하도록 역할을 분리한다.
- Bootstrap CDN을 기본 레이아웃 레벨에서 연결해 이후 폼, 리스트, 상세 화면에 바로 재사용한다.
- 프로젝트의 `frontend-design` 스킬 지침에 맞춰 브랜드 컬러, 둥근 모서리, 부드러운 그림자, 명확한 정보 계층을 적용한다.

## 파일별 변경 내용

### 1. [header.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/layout/header.mustache)

- HTML 문서 시작부, 메타 태그, 타이틀 처리, 폰트, Bootstrap CSS 연결을 담당하도록 생성했다.
- 공통 CSS 변수를 정의해 기본 색상, 배경, 반경, 그림자 값을 중앙에서 관리하도록 구성했다.
- 상단 네비게이션 바를 추가했다.
- `/home`, `/join-form`, `/login-form`, `/boards`로 이동하는 기본 링크를 배치했다.
- `main` 태그를 열어 각 페이지 템플릿이 콘텐츠만 채울 수 있게 만들었다.

### 2. [footer.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/layout/footer.mustache)

- `main` 닫기, 공통 푸터 렌더링, Bootstrap JS 연결, 문서 종료 태그를 담당하도록 생성했다.
- 푸터에 현재 작업 성격인 공통 레이아웃 기반 구축 정보를 표기했다.

### 3. [home.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/home.mustache)

- 기존 단일 HTML 문서를 제거하고 `{{> layout/header}}`, `{{> layout/footer}}` partial 조합 방식으로 변경했다.
- 공통 레이아웃 적용 결과를 확인할 수 있도록 hero 섹션과 안내 카드 섹션을 구성했다.
- 다음 단계 화면인 회원가입, 로그인으로 확장된다는 흐름이 보이도록 CTA를 배치했다.

### 4. [TODO.md](C:/workspace/sample_lab/ai-sample-spring/TODO.md)

- `T-1.5` 및 하위 항목을 완료 처리했다.

### 5. [task.md](C:/workspace/sample_lab/ai-sample-spring/task.md)

- 상세 태스크 문서의 `T-1.5` 및 하위 항목을 완료 처리했다.

## Mustache partial 구성 방식

- 공통 문서 시작부와 네비게이션은 `layout/header.mustache`가 담당한다.
- 페이지별 템플릿은 콘텐츠 섹션만 렌더링한다.
- 공통 푸터와 스크립트 로딩, 문서 종료는 `layout/footer.mustache`가 담당한다.
- 이 구조를 사용하면 이후 화면은 다음 패턴만 따르면 된다.

```mustache
{{> layout/header}}
페이지별 콘텐츠
{{> layout/footer}}
```

## Bootstrap 적용 범위

- 공통 네비게이션 바
- 컨테이너 및 반응형 grid
- 버튼 스타일
- 카드형 정보 섹션
- 반응형 spacing과 typography 조합

## 재사용 방식 가이드

- 회원가입 화면은 `join-form.mustache`에서 form 카드만 구현하면 된다.
- 로그인 화면은 `login-form.mustache`에서 인증 입력 영역만 구성하면 된다.
- 게시글 목록, 상세, 작성, 수정 화면도 동일하게 공통 셸 위에 콘텐츠만 교체하면 된다.
- 공통 스타일 수정이 필요하면 개별 화면이 아니라 `header.mustache`의 CSS 변수와 공통 클래스부터 조정하면 된다.

## 기대 효과

- 레이아웃 중복을 줄여 템플릿 유지보수 비용을 낮춘다.
- 신규 화면 구현 시 네비게이션, 푸터, 외부 라이브러리 연결을 반복하지 않아도 된다.
- 이후 `frontend-design` 스킬로 폼 화면을 확장할 때 일관된 시각 구조를 유지할 수 있다.

## 확인 필요 사항

- 현재 네비게이션에 포함된 `/join-form`, `/login-form`, `/boards` 라우트는 아직 구현 전일 수 있다.
- 따라서 화면 이동 링크는 선행 레이아웃 작업 관점의 자리 표시자 역할을 가진다.

## 다음 작업 제안

- `T-2.1` 회원가입 화면에서 `join-form.mustache`를 같은 레이아웃 위에 구현한다.
- `T-2.2` 로그인 화면에서 `login-form.mustache`를 같은 구조로 구현한다.
- 공통 알림 영역이나 서버 검증 메시지 출력 슬롯이 필요하면 header, footer 사이에 재사용 가능한 fragment 규칙을 추가한다.
