# 🚩 작업 보고서: 게시글 페이징 Step 3 - 이전/다음 버튼

- **작업 일시**: 2026-03-23
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `todo.md`의 Step 3 요구사항과 `.person/dicision/2026-03-23-페이징-정책.md`를 함께 읽고, 이번 단계의 기준을 `0-based`가 아니라 `1-based` 페이지 정책으로 확정했다.
2. `BoardController`에서 `page`를 문자열로 직접 받아, `page` 누락, 숫자 형식 오류, `1` 미만 값을 모두 `/board/list?page=1`로 리다이렉트하도록 바꿨다.
3. `BoardService.게시글목록보기`에서 `offset = (page - 1) * 3`으로 계산식을 바꿔 `1-based` 페이지 번호와 SQL `OFFSET`의 역할을 분리했다.
4. `BoardResponse.Page`에 `hasPrev`, `prevPage`, `hasNext`, `nextPage`를 추가해 Mustache가 이전/다음 버튼을 단순하게 렌더링할 수 있게 만들었다.
5. `list.mustache`를 Step 3 설명 화면으로 바꾸고, GET form 기반의 `이전`, `다음` 버튼과 현재 페이지 상태 영역을 추가했다.
6. `detail.mustache`, `home.mustache`, `layout/header.mustache`의 링크를 모두 `?page=1` 기준으로 맞춰 첫 페이지의 정식 주소를 일관되게 유지했다.
7. `BoardControllerTest`, `BoardServiceTest`를 Step 3 정책에 맞게 수정하고 `.\gradlew.bat test --tests com.example.demo.board.*`로 검증했다.
8. 마지막으로 `todo.md`의 Step 3 완료 체크를 반영했다.

## 2. 🧩 변경된 모든 코드 포함

- `src/main/java/com/example/demo/board/BoardController.java`

```java
@GetMapping("/board/list")
public String list(@RequestParam(required = false) String page, Model model) {
    if (페이지가비어있음(page)) {
        return "redirect:/board/list?page=1";
    }

    var 페이지번호 = 페이지번호파싱하기(page);
    if (페이지번호 == null || 페이지번호 < 1) {
        return "redirect:/board/list?page=1";
    }

    var boardPage = boardService.게시글목록보기(페이지번호);
    model.addAttribute("model", boardPage);
    return "list";
}

@GetMapping("/board/{id}")
public String detail(@PathVariable("id") Integer id,
        @RequestParam(required = false) String page,
        Model model) {
    var board = boardService.게시글상세보기(id);
    board.setPage(상세페이지번호보정하기(page));
    model.addAttribute("model", board);
    return "detail";
}

private boolean 페이지가비어있음(String page) {
    return page == null || page.isBlank();
}

private Integer 페이지번호파싱하기(String page) {
    try {
        return Integer.parseInt(page);
    } catch (NumberFormatException e) {
        return null;
    }
}

private Integer 상세페이지번호보정하기(String page) {
    if (페이지가비어있음(page)) {
        return 1;
    }

    var 페이지번호 = 페이지번호파싱하기(page);
    if (페이지번호 == null || 페이지번호 < 1) {
        return 1;
    }

    return 페이지번호;
}
```

- `src/main/java/com/example/demo/board/BoardService.java`

```java
public BoardResponse.Page 게시글목록보기(Integer page) {
    int 보정페이지 = 페이지보정하기(page);
    int offset = (보정페이지 - 1) * PAGE_SIZE;

    return new BoardResponse.Page(보정페이지, PAGE_SIZE, offset, boardRepository.findByPaging(offset));
}

private int 페이지보정하기(Integer page) {
    if (page == null || page < 1) {
        return 1;
    }
    return page;
}
```

- `src/main/java/com/example/demo/board/BoardResponse.java`

```java
@Data
public static class Page {
    private Integer currentPage;
    private Integer pageSize;
    private Integer offset;
    private Integer boardCount;
    private boolean hasPrev;
    private Integer prevPage;
    private boolean hasNext;
    private Integer nextPage;
    private List<Min> boardList;

    public Page(Integer currentPage, Integer pageSize, Integer offset, List<Board> boardList) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.offset = offset;
        this.boardCount = boardList.size();
        this.hasPrev = currentPage > 1;
        this.prevPage = currentPage - 1;
        this.hasNext = this.boardCount == pageSize;
        this.nextPage = currentPage + 1;
        this.boardList = boardList.stream()
                .map(Min::new)
                .toList();
    }
}

@Data
public static class Detail {
    private Integer id;
    private String title;
    private String content;
    private String username;
    private String createdAtText;
    private Integer page;
}
```

- `src/main/resources/templates/list.mustache`

```mustache
{{! Model Key: model(BoardResponse.Page) }}
{{> layout/header}}

<main class="site-container">
    <section class="board-page">
        <div class="board-page__intro">
            <span class="eyebrow">
                <i class="fa-solid fa-table-list"></i>
                Step 3
            </span>
            <h1 class="board-page__title">이전/다음 버튼으로 3개씩 넘겨 보는 페이징 학습</h1>
            <p class="board-page__description">
                이번 단계에서는 첫 페이지를 <code>?page=1</code>로 고정하고,
                <code>이전</code>, <code>다음</code> 버튼으로 세 개씩 이동합니다.
                주소의 페이지 번호를 바꾸면 <strong>offset = (page - 1) * limit</strong> 계산 결과에 따라
                다른 3건이 보입니다.
            </p>
        </div>

        <section class="board-page__summary" aria-label="pagination summary">
            <div class="board-page__chip"><span>page</span><strong>{{model.currentPage}}</strong></div>
            <div class="board-page__chip"><span>limit</span><strong>{{model.pageSize}}</strong></div>
            <div class="board-page__chip"><span>offset</span><strong>{{model.offset}}</strong></div>
            <div class="board-page__chip"><span>rows</span><strong>{{model.boardCount}}</strong></div>
        </section>

        <section class="board-list-card" aria-labelledby="board-list-title">
            <div class="board-list-card__header">
                <h2 id="board-list-title">현재 페이지 게시글 3건과 이동 버튼</h2>
                <p>
                    이제 목록 아래에서 이전/다음 버튼으로 다음 묶음을 바로 확인할 수 있습니다.
                    첫 페이지에서는 이전 버튼을 숨기고, 다음 버튼은 현재 페이지 결과가 3건일 때만 보여줍니다.
                </p>
                <div class="board-page__formula">
                    <span>offset = (page - 1) * limit</span>
                    <strong>{{model.offset}} = ({{model.currentPage}} - 1) * {{model.pageSize}}</strong>
                </div>
            </div>

            <div class="board-list-table" role="list">
                {{#model.boardList}}
                <a class="board-line" role="listitem" href="/board/{{id}}?page={{model.currentPage}}">
                    <span class="board-line__id">#{{id}}</span>
                    <span class="board-line__title">{{title}}</span>
                    <span class="board-line__author">{{username}}</span>
                    <span class="board-line__date">{{createdAtText}}</span>
                </a>
                {{/model.boardList}}
            </div>

            <div class="board-pagination">
                <div class="board-pagination__status">
                    <span>현재 페이지</span>
                    <strong>{{model.currentPage}}</strong>
                </div>
                <div class="board-pagination__actions">
                    {{#model.hasPrev}}
                    <form method="get" action="/board/list" class="board-pagination__form">
                        <input type="hidden" name="page" value="{{model.prevPage}}">
                        <button class="button button--secondary" type="submit">이전</button>
                    </form>
                    {{/model.hasPrev}}

                    {{#model.hasNext}}
                    <form method="get" action="/board/list" class="board-pagination__form">
                        <input type="hidden" name="page" value="{{model.nextPage}}">
                        <button class="button" type="submit">다음</button>
                    </form>
                    {{/model.hasNext}}
                </div>
            </div>
        </section>
    </section>
</main>
```

- `src/main/resources/templates/detail.mustache`

```mustache
{{! Model Key: model(BoardResponse.Detail) }}
{{> layout/header}}

<main class="site-container">
    {{#model}}
    <section class="board-detail-page">
        <div class="board-detail-card">
            <h1>{{title}}</h1>
            <article class="board-detail-card__content">{{content}}</article>
            <div class="board-detail-card__actions">
                <a class="button button--secondary" href="/board/list?page={{page}}">
                    목록으로 돌아가기
                </a>
            </div>
        </div>
    </section>
    {{/model}}
</main>
```

- `src/main/resources/templates/layout/header.mustache`

```mustache
<a class="site-nav__link" href="/board/list?page=1">게시글 목록</a>
```

- `src/main/resources/templates/home.mustache`

```mustache
<a class="button button--secondary" href="/board/list?page=1">
    <i class="fa-solid fa-table-list"></i>
    게시글 목록
</a>
```

- `src/main/resources/static/css/design-tokens.css`

```css
.board-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-xl);
  padding-top: var(--space-lg);
  border-top: 1px solid rgba(217, 226, 242, 0.9);
}

.board-pagination__status {
  display: grid;
  gap: var(--space-2xs);
  color: var(--color-text-secondary);
}

.board-pagination__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  justify-content: flex-end;
}
```

- `src/test/java/com/example/demo/board/BoardControllerTest.java`

```java
@Test
void list_redirectsWhenPageMissing() throws Exception {
    mvc.perform(get("/board/list"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/board/list?page=1"));
}

@Test
void list_redirectsWhenPageIsNotNumber() throws Exception {
    mvc.perform(get("/board/list").param("page", "abc"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/board/list?page=1"));
}

@Test
void list_firstPage_success() throws Exception {
    mvc.perform(get("/board/list").param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/board/" + boardF.getId() + "?page=1")))
            .andExpect(content().string(containsString("value=\"2\"")));
}

@Test
void list_secondPage_success() throws Exception {
    mvc.perform(get("/board/list").param("page", "2"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/board/" + boardC.getId() + "?page=2")))
            .andExpect(content().string(containsString("value=\"1\"")))
            .andExpect(content().string(containsString("value=\"3\"")));
}

@Test
void list_redirectsWhenPageIsLessThanOne() throws Exception {
    mvc.perform(get("/board/list").param("page", "0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/board/list?page=1"));
}
```

- `src/test/java/com/example/demo/board/BoardServiceTest.java`

```java
var boardPage = boardService.게시글목록보기(1);

assertThat(boardPage.getCurrentPage()).isEqualTo(1);
assertThat(boardPage.getOffset()).isEqualTo(0);
assertThat(boardPage.isHasPrev()).isFalse();
assertThat(boardPage.isHasNext()).isTrue();
assertThat(boardPage.getNextPage()).isEqualTo(2);

var secondPage = boardService.게시글목록보기(2);

assertThat(secondPage.getCurrentPage()).isEqualTo(2);
assertThat(secondPage.getOffset()).isEqualTo(3);
assertThat(secondPage.isHasPrev()).isTrue();
assertThat(secondPage.getPrevPage()).isEqualTo(1);
assertThat(secondPage.isHasNext()).isFalse();
```

- `todo.md`

```markdown
## [Step 3] UI 제어: 이전/다음 버튼

- [x] T-3.1 Mustache 화면에 `이전`, `다음` 버튼 추가 및 페이지 이동 링크 연결
- [x] T-3.2 현재 페이지가 0일 때 `이전` 버튼 처리 등의 기초적인 예외 상황 고려
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 책장을 3권씩 보여주는 도서관 서가와 같습니다. 한 번에 모든 책을 꺼내놓는 대신, `1페이지`는 첫 번째 책 묶음, `2페이지`는 다음 책 묶음을 보여주고, `이전`과 `다음` 버튼은 사서에게 "다음 칸 보여주세요"라고 말하는 버튼 역할을 합니다.
- 특히 `page`와 `offset`을 나눈 것은 "손님이 보는 책장 번호"와 "사서가 창고에서 실제로 몇 칸 건너뛸지 계산하는 숫자"를 구분한 것과 같습니다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **문자열 파라미터 직접 파싱**: `@RequestParam Integer page`를 쓰면 `abc` 같은 값이 들어왔을 때 스프링이 바로 400을 만들 수 있다. 이번 단계에서는 잘못된 주소를 첫 페이지로 돌려보내야 해서 `String page`로 받은 뒤 `Integer.parseInt`를 직접 호출해 제어권을 가져왔다.

```java
var 페이지번호 = 페이지번호파싱하기(page);
if (페이지번호 == null || 페이지번호 < 1) {
    return "redirect:/board/list?page=1";
}
```

- **1-based 페이지와 SQL OFFSET 분리**: 사용자는 `1페이지`, `2페이지`처럼 보지만 DB는 `0`, `3`, `6`처럼 건너뛸 개수를 원한다. 그래서 `offset = (page - 1) * PAGE_SIZE` 공식을 적용했다.

```java
int offset = (보정페이지 - 1) * PAGE_SIZE;
```

- **Mustache 조건 렌더링**: 이전 버튼은 `hasPrev`가 참일 때만, 다음 버튼은 `hasNext`가 참일 때만 보여준다. 이렇게 하면 템플릿이 `if-else` 없이도 DTO 상태만 보고 화면을 그릴 수 있다.

```mustache
{{#model.hasPrev}}
<form method="get" action="/board/list">
    <input type="hidden" name="page" value="{{model.prevPage}}">
    <button type="submit">이전</button>
</form>
{{/model.hasPrev}}
```

- **현재 단계의 한계도 명확히 유지**: Step 3에서는 총 게시글 수를 아직 세지 않기 때문에, 다음 버튼은 `현재 결과가 3건이면 존재할 가능성이 높다`는 기준으로만 보여준다. 정확한 마지막 페이지 계산은 Step 4에서 `COUNT(*)`를 추가하면서 해결할 수 있다.

## 5. ✅ 검증 결과

- 실행 명령어: `.\gradlew.bat test --tests com.example.demo.board.*`
- 결과: 성공
- 확인한 내용:
  - `page` 누락 시 `/board/list?page=1` 리다이렉트
  - `page=abc` 같은 숫자 형식 오류 리다이렉트
  - `page=0` 같은 정책 위반 리다이렉트
  - `page=1`, `page=2`에서 3개씩 목록 이동
  - 상세 페이지에서 목록 복귀 시 현재 페이지 유지
