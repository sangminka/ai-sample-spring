# 개발 단계 (Development Phases)

## Phase 1: 기반 구조 및 핵심 도메인 설계 (Infrastructure & Core Domain)

- [x] 프로젝트 초기 설정 (Spring Boot, Gradle, Dependencies)
- [x] 데이터베이스 스키마 설계 및 엔티티 구현 (`User`, `Board`, `Reply`)
- [x] 공통 응답 규격 및 유틸리티 설정 (`_core`)
- [x] H2 데이터베이스 및 JPA 설정

## Phase 2: 회원 인증 시스템 (Membership System)

- [ ] 회원가입 로직 구현 (아이디 중복 체크 포함)
- [ ] 세션 기반 로그인/로그아웃 구현
- [ ] 회원 탈퇴 및 관련 데이터 처리 로직
- [ ] 인증 필터 또는 인터셉터를 통한 권한 관리

## Phase 3: 게시글 관리 시스템 (Core Blog Features)

- [ ] 게시글 CRUD (목록, 상세, 작성, 수정, 삭제) 구현
- [ ] 페이징 처리 (Pageable) 적용
- [ ] 게시글 검색 기능 (제목, 내용) 구현
- [ ] 게시글 상세 보기 시 작성자 정보 연동

## Phase 4: 댓글 및 상호작용 (Interaction Features)

- [ ] 게시글별 댓글 작성 및 삭제 구현
- [ ] 댓글 목록 조회 및 게시글 상세 페이지 통합
- [ ] 작성자 본인 확인 및 삭제 권한 검증 로직

## Phase 5: 예외 처리 및 안정화 (Polishing & Validation)

- [ ] 글로벌 예외 처리기 (GlobalExceptionHandler) 구현
- [ ] 입력 데이터 유효성 검사 (Bean Validation) 적용
- [ ] UI/UX 개선 (Mustache 템플릿 레이아웃 정리)
- [ ] 통합 테스트 및 버그 수정
