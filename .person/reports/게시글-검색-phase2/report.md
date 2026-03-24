# 작업 보고서 [게시글 검색 Phase 2]

- **작업 일시**: 2026-03-24
- **진행 상태**: 완료

## 1. 전체 작업 흐름 (Workflow)

1. Phase 1 SSR 검색 구조를 유지한 채, Phase 2 목표를 `input 이벤트 기반 AJAX 실시간 검색`으로 확정했다.
2. 현재 프로젝트 규칙에 맞춰 `ApiController는 ajax가 필요할 때만 사용` 원칙대로 게시글 검색 전용 API를 추가하기로 했다.
3. `BoardApiController`를 만들고 `GET /api/board/search`가 `Resp.ok(BoardResponse.ListResponse)`를 반환하도록 구성했다.
4. Phase 1에서 만든 `BoardRequest.Search`, `BoardService.게시글목록보기()`, `BoardResponse.ListResponse`를 그대로 재사용해서 검색 규칙이 중복되지 않게 했다.
5. `list.mustache`에 AJAX용 로딩 메시지 영역, 오류 메시지 영역, 동적 렌더링 컨테이너를 추가했다.
6. 정적 JS 파일 `board-search.js`를 추가하고, 검색 input의 `input` 이벤트에 `fetch` 요청을 연결했다.
7. AJAX 성공 시 `목록 + 빈 결과 문구 + 페이지네이션 + 요약 카드`를 다시 그리도록 렌더링 함수를 만들었다.
8. AJAX 성공 시 `history.replaceState`로 URL을 `?page=1&keyword=...` 형태로 동기화했다.
9. AJAX 실패 시 기존 목록은 유지하고, 검색창 근처에 오류 메시지만 보이도록 처리했다.
10. API 동작을 검증하기 위해 `BoardApiControllerTest`를 추가했다.
11. 기존 `board` 관련 테스트와 새 API 테스트를 포함해 `./gradlew.bat test --tests com.example.demo.board.*`를 실행했고 통과했다.
12. 체크리스트에서 Phase 2 구현 항목을 완료 처리하고, 브라우저 수동 확인이 필요한 항목은 남겨두었다.

## 2. 변경한 코드 정리

### 2-1. 게시글 검색 API 추가

```java
@RequiredArgsConstructor
@RestController
public class BoardApiController {

    private final BoardService boardService;

    @GetMapping("/api/board/search")
    public Object search(BoardRequest.Search requestDTO) {
        return Resp.ok(boardService.게시글목록보기(requestDTO));
    }
}
```

- 파일: `src/main/java/com/example/demo/board/BoardApiController.java`
- 역할: 검색 input이 바뀔 때 브라우저가 HTML 전체가 아니라 JSON 데이터만 받아올 수 있도록 전용 검색 API를 만들었다.
- 핵심: 검색 규칙은 새로 만들지 않고 `BoardService.게시글목록보기()`를 그대로 재사용했다.

### 2-2. 목록 화면에 AJAX 상태 영역 추가

```mustache
<div class="mt-3" aria-live="polite">
    <p id="board-search-loading" class="text-secondary mb-1" hidden>검색 중...</p>
    <p id="board-search-error" class="text-danger mb-0" hidden></p>
</div>
```

```mustache
<div id="board-search-results" data-api-url="/api/board/search">
    ...
</div>
```

```mustache
<script src="/js/board-search.js"></script>
```

- 파일: `src/main/resources/templates/list.mustache`
- 역할: 브라우저에서 AJAX 상태를 표시하고, 결과 영역만 다시 그릴 수 있도록 구조를 준비했다.

### 2-3. 실시간 검색 JS 추가

```javascript
searchInput.addEventListener("input", async (event) => {
    const keyword = event.target.value;
    showLoading();
    hideError();

    try {
        const response = await fetch(buildApiUrl(apiUrl, keyword), {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            }
        });

        const payload = await response.json();
        if (!response.ok) {
            throw new Error(payload.msg || "검색 요청에 실패했습니다.");
        }

        const model = payload.body;
        searchInput.value = model.keyword;
        searchResults.innerHTML = renderSearchResults(model);
        history.replaceState(null, "", buildListUrl(1, model.keyword));
    } catch (error) {
        showError(error.message || "검색 요청에 실패했습니다.");
    } finally {
        hideLoading();
    }
});
```

- 파일: `src/main/resources/static/js/board-search.js`
- 역할: 검색창 값이 바뀔 때마다 API를 호출해서 목록 영역을 다시 그린다.
- 핵심:
  - `page=1`부터 검색 시작
  - 성공 시 결과 영역만 갱신
  - URL 동기화
  - 실패 시 기존 화면 유지

### 2-4. 결과 렌더링 함수 추가

```javascript
function renderSearchResults(model) {
    return `
        ${renderSummary(model)}
        ${renderBoardList(model)}
    `;
}
```

```javascript
function renderBoardLines(model) {
    if (model.boards.length === 0) {
        if (model.hasKeyword) {
            return `
                <div class="board-line board-line--empty">
                    <span>'${escapeHtml(model.keyword)}' 검색 결과가 없습니다.</span>
                </div>
            `;
        }

        return `
            <div class="board-line board-line--empty">
                <span>이 페이지에는 게시글이 없습니다.</span>
            </div>
        `;
    }

    return model.boards.map((board) => `
        <a class="board-line" role="listitem" href="${buildDetailUrl(board.id, model.paging.currentPage, model.keyword)}">
            <span class="board-line__id">#${escapeHtml(String(board.id))}</span>
            <span class="board-line__title">${escapeHtml(board.title)}</span>
            <span class="board-line__author">${escapeHtml(board.username)}</span>
            <span class="board-line__date">${escapeHtml(board.createdAtText)}</span>
        </a>
    `).join("");
}
```

- 파일: `src/main/resources/static/js/board-search.js`
- 역할: 서버가 준 `BoardResponse.ListResponse`를 받아 요약 카드, 목록, 빈 결과 문구, 페이지네이션을 브라우저에서 다시 만들어 낸다.

### 2-5. URL 동기화와 상세 링크 유지

```javascript
function buildListUrl(page, keyword) {
    const trimmedKeyword = keyword ? keyword.trim() : "";
    const params = new URLSearchParams();
    params.set("page", String(page));
    if (trimmedKeyword.length > 0) {
        params.set("keyword", trimmedKeyword);
    }
    return `/board/list?${params.toString()}`;
}

function buildDetailUrl(boardId, page, keyword) {
    const trimmedKeyword = keyword ? keyword.trim() : "";
    const params = new URLSearchParams();
    params.set("page", String(page));
    if (trimmedKeyword.length > 0) {
        params.set("keyword", trimmedKeyword);
    }
    return `/board/${boardId}?${params.toString()}`;
}
```

- 파일: `src/main/resources/static/js/board-search.js`
- 역할: AJAX로 검색해도 주소창과 상세 페이지 이동 링크가 SSR 구조와 같은 규칙을 따르도록 맞췄다.

### 2-6. API 테스트 추가

```java
@DisplayName("게시글 검색 API는 trim한 검색어로 제목과 내용을 함께 조회한다")
@Test
void search_success() throws Exception {
    mvc.perform(get("/api/board/search").param("page", "1").param("keyword", " phase2-api "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.body.keyword").value("phase2-api"))
            .andExpect(jsonPath("$.body.hasKeyword").value(true))
            .andExpect(jsonPath("$.body.paging.totalCount").value(2));
}
```

```java
@DisplayName("게시글 검색 API는 빈 검색어를 전체 목록 조회로 처리한다")
@Test
void search_blankKeyword_returnsAllBoards() throws Exception {
    mvc.perform(get("/api/board/search").param("page", "1").param("keyword", "   "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body.hasKeyword").value(false))
            .andExpect(jsonPath("$.body.keyword").value(""));
}
```

- 파일: `src/test/java/com/example/demo/board/BoardApiControllerTest.java`
- 역할: API 레벨에서 `trim`, 제목/내용 통합 검색, 빈 검색어 전체 목록 처리를 검증했다.

## 3. 쉬운 비유 (Easy Analogy)

- Phase 1이 `검색 버튼을 누르면 도서관 사서가 새 목록 종이를 통째로 다시 가져다주는 방식`이었다면,
- Phase 2는 `검색대 화면이 직접 사서에게 “이 키워드 결과만 다시 보여줘”라고 물어보고, 책 목록 부분만 갈아끼우는 방식`과 비슷하다.
- 건물 전체를 다시 짓는 게 아니라, 안내판만 바꾸는 느낌이라고 보면 된다.

## 4. 기술 딥다이브 (Technical Deep-dive)

### 왜 API가 `BoardResponse.ListResponse`를 그대로 재사용하는가

- Phase 1 SSR에서도 `boards + paging + keyword` 구조를 이미 쓰고 있었다.
- AJAX라고 해서 다른 DTO를 새로 만들면, 검색 규칙과 화면 표현이 둘로 갈라질 위험이 있다.
- 그래서 `Resp.ok(BoardResponse.ListResponse)`를 그대로 사용해 SSR과 AJAX가 같은 응답 구조를 공유하게 만들었다.

### 왜 `input` 이벤트를 바로 사용했는가

- 이번 단계의 학습 포인트는 `실시간 검색이 어떻게 붙는가`다.
- `keyup`보다 `input`이 값 변화 중심이라 붙여넣기까지 자연스럽게 잡을 수 있다.
- 아직 디바운스는 넣지 않았기 때문에, 이번 단계는 “입력하면 곧바로 API 호출” 구조만 명확히 만든 상태다.

### 왜 `history.replaceState`를 사용했는가

- AJAX 검색 결과가 바뀌면 주소도 같은 상태를 반영해야 새로고침 시 흐름이 맞는다.
- 하지만 입력할 때마다 브라우저 히스토리를 쌓으면 뒤로가기가 한 글자 단위로 깨진다.
- 그래서 `pushState`가 아니라 `replaceState`를 택해서 현재 주소만 덮어쓰는 방식으로 맞췄다.

### 왜 실패 시 기존 목록을 유지하는가

- 실시간 검색은 보조 기능에 가깝기 때문에 실패했다고 현재 목록까지 사라지면 UX가 불안해진다.
- 그래서 네트워크 실패나 응답 실패가 나도 `searchResults.innerHTML`은 건드리지 않고, 오류 문구만 노출하게 만들었다.

### 현재 Phase 2의 한계

- 아직 디바운스가 없어서 입력마다 즉시 요청이 나간다.
- 이전 요청이 늦게 도착했을 때 최신 결과를 덮어쓰는 문제를 아직 막지 않았다.
- 이 두 부분은 Phase 3에서 `300ms` 디바운스와 마지막 요청 우선 처리로 해결할 예정이다.

## 5. 검증 결과

- 실행 명령:

```bash
./gradlew.bat test --tests com.example.demo.board.*
```

- 결과: 성공

### 자동 검증으로 확인한 것

- `GET /api/board/search` 응답 구조
- `trim`된 검색어 반환
- 제목/내용 통합 검색
- 빈 검색어면 전체 목록 처리
- 기존 `board` SSR 테스트와 공존하는지 확인

### 아직 수동 확인이 필요한 것

- 검색창에 1글자 입력 시 브라우저에서 즉시 검색되는지
- 붙여넣기 시에도 결과가 갱신되는지
- 입력값을 지우면 전체 목록으로 복귀하는지
- 주소창이 `?page=1&keyword=...` 형태로 갱신되는지
- 페이지 클릭은 여전히 SSR submit/link로 동작하는지

## 6. 다음 단계

1. `board-search.js`에 `300ms` 디바운스를 적용한다.
2. 디바운스 대기 중에는 로딩을 띄우지 않고, 실제 요청 시작 시점에만 `검색 중...`을 보이게 한다.
3. `AbortController` 또는 요청 번호 비교로 마지막 요청만 반영되도록 만든다.
4. 브라우저 수동 점검을 한 뒤 Phase 2 남은 체크를 완료 처리한다.
