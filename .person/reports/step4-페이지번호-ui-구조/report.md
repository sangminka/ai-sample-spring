# 🚩 작업 보고서: Step 4 페이지번호 UI 구조

- **작업 일시**: 2026-03-23
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `.person/dicision/2026-03-23-step4-페이지번호-ui-구조.md`를 기준으로 Step 4 목표를 `COUNT(*)`, 총 페이지 수 계산, 전체 번호 UI 출력으로 확정했다.
2. `BoardRepository`에 `select count(*) from board_tb` 네이티브 쿼리를 추가해 전체 게시글 수를 직접 조회할 수 있게 만들었다.
3. `BoardService`에서 `totalCount`, `totalPageCount`, `hasPrev`, `hasNext`, `pageNumbers`를 계산하도록 확장했다.
4. `BoardResponse`에 `PageNumberResponse`와 확장된 `PagingResponse`를 추가해 Mustache가 비교 없이 현재 페이지를 강조할 수 있게 만들었다.
5. `list.mustache`를 Step 4 화면으로 바꾸고, 총 개수/총 페이지 수 요약과 번호 목록 UI를 렌더링하도록 수정했다.
6. CSS에 페이지 번호 스타일을 추가해 현재 페이지 강조와 번호 버튼 배치를 정리했다.
7. `BoardServiceTest`, `BoardControllerTest`에 총 페이지 계산, 마지막 페이지 판별, 범위 초과 빈 목록, 번호 UI 렌더링 검증을 추가했다.
8. `todo.md`의 Step 4 항목을 완료 처리했다.

## 2. 🧩 변경된 모든 코드 포함

- `src/main/java/com/example/demo/board/BoardRepository.java`

```java
@Query(value = "select count(*) from board_tb", nativeQuery = true)
Long countAll();
```

- `src/main/java/com/example/demo/board/BoardResponse.java`

```java
@Data
public static class PageNumberResponse {
    private Integer number;
    private boolean current;

    public PageNumberResponse(Integer number, Integer currentPage) {
        this.number = number;
        this.current = number.equals(currentPage);
    }
}

@Data
public static class PagingResponse {
    private Integer currentPage;
    private Integer pageSize;
    private Integer offset;
    private Integer boardCount;
    private Integer totalCount;
    private Integer totalPageCount;
    private boolean hasPrev;
    private Integer prevPage;
    private boolean hasNext;
    private Integer nextPage;
    private List<PageNumberResponse> pageNumbers;
}
```

- `src/main/java/com/example/demo/board/BoardService.java`

```java
public BoardResponse.ListResponse 게시글목록보기(Integer page) {
    int 보정페이지 = 페이지보정하기(page);
    int offset = (보정페이지 - 1) * PAGE_SIZE;
    int totalCount = boardRepository.countAll().intValue();
    int totalPageCount = 총페이지수구하기(totalCount);
    var boards = boardRepository.findByPaging(offset);
    int boardCount = boards.size();
    boolean hasPrev = 이전페이지있음(보정페이지, totalPageCount);
    int prevPage = 이전페이지구하기(보정페이지, totalPageCount);
    boolean hasNext = 다음페이지있음(보정페이지, totalPageCount);
    int nextPage = 다음페이지구하기(보정페이지);
    var pageNumbers = 페이지번호목록구하기(totalPageCount, 보정페이지);

    var paging = new BoardResponse.PagingResponse(
            보정페이지,
            PAGE_SIZE,
            offset,
            boardCount,
            totalCount,
            totalPageCount,
            hasPrev,
            prevPage,
            hasNext,
            nextPage,
            pageNumbers);

    return new BoardResponse.ListResponse(boards, paging);
}

private int 총페이지수구하기(int totalCount) {
    if (totalCount == 0) {
        return 0;
    }
    return (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
}

private List<BoardResponse.PageNumberResponse> 페이지번호목록구하기(int totalPageCount, int currentPage) {
    if (totalPageCount == 0) {
        return List.of();
    }

    return IntStream.rangeClosed(1, totalPageCount)
            .mapToObj(number -> new BoardResponse.PageNumberResponse(number, currentPage))
            .toList();
}
```

- `src/main/resources/templates/list.mustache`

```mustache
{{! Model Key: model(BoardResponse.ListResponse) }}

<h1 class="board-page__title">COUNT(*)와 전체 개수로 총 페이지 수를 계산하는 페이징 학습</h1>

<strong>{{model.paging.totalCount}}</strong>
<strong>{{model.paging.totalPageCount}}</strong>

{{#model.paging.pageNumbers}}
    {{#current}}
    <span class="page-number page-number--active">{{number}}</span>
    {{/current}}

    {{^current}}
    <a href="/board/list?page={{number}}" class="page-number">{{number}}</a>
    {{/current}}
{{/model.paging.pageNumbers}}
```

- `src/main/resources/static/css/design-tokens.css`

```css
.board-pagination__numbers {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
  align-items: center;
  width: 100%;
}

.page-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  padding: 0.55rem 0.9rem;
  border: 1px solid rgba(217, 226, 242, 0.9);
  border-radius: 999px;
  background: rgba(246, 248, 252, 0.82);
  color: var(--color-text-primary);
  font-size: var(--text-sm);
  font-weight: 700;
  text-decoration: none;
}

.page-number--active {
  border-color: transparent;
  background: var(--color-brand-500);
  color: #fff;
}
```

- `src/test/java/com/example/demo/board/BoardServiceTest.java`

```java
when(boardRepository.countAll()).thenReturn(9L);

var boardPage = boardService.게시글목록보기(1);

assertThat(boardPage.getPaging().getTotalCount()).isEqualTo(9);
assertThat(boardPage.getPaging().getTotalPageCount()).isEqualTo(3);
assertThat(boardPage.getPaging().getPageNumbers()).hasSize(3);
assertThat(boardPage.getPaging().getPageNumbers().getFirst().isCurrent()).isTrue();
```

- `src/test/java/com/example/demo/board/BoardControllerTest.java`

```java
mvc.perform(get("/board/list").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("COUNT(*)와 전체 개수로 총 페이지 수를 계산하는 페이징 학습")))
        .andExpect(content().string(containsString("page-number page-number--active\">1</span>")));

mvc.perform(get("/board/list").param("page", String.valueOf(outOfRangePage)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("이 페이지에는 게시글이 없습니다.")));
```

- `todo.md`

```markdown
## [Step 4] 전체 페이지 계산 및 번호 UI

- [x] T-4.1 `SELECT COUNT(*)` 쿼리를 직접 작성하여 전체 게시글 수 조회
- [x] T-4.2 전체 개수와 페이지 크기(3)를 이용해 `총 페이지 수` 계산 로직 구현
- [x] T-4.3 Mustache에서 `1 2 3 4...` 형태의 페이지 번호 목록을 동적으로 출력
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 학급 전체 학생 수를 세고 몇 반까지 있는지 계산한 뒤, 교실 문 앞에 `1반 2반 3반` 표지판을 전부 붙이는 것과 같습니다.
- Step 3에서는 "지금 교실 안에 학생이 3명 있네, 다음 반도 있을 것 같아" 수준이었다면, Step 4에서는 실제로 전체 학생 수를 세서 반 개수를 정확히 계산한 뒤 번호판을 만든 것입니다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **COUNT(*)와 totalPageCount 계산**: 전체 게시글 수를 직접 세고, 그 숫자를 페이지 크기 3으로 나눠 총 페이지 수를 구했다. 나누어 떨어지지 않더라도 마지막 페이지가 하나 더 필요하므로 올림 계산이 들어간다.

```java
int totalCount = boardRepository.countAll().intValue();
int totalPageCount = 총페이지수구하기(totalCount);
```

- **Mustache 친화적인 번호 객체 리스트**: `currentPage`만 넘기면 Mustache가 각 번호와 비교하기 어렵다. 그래서 `number`, `current`를 가진 `PageNumberResponse` 리스트로 바꿔 템플릿이 상태값만 보고 렌더링하게 했다.

```java
new BoardResponse.PageNumberResponse(number, currentPage)
```

- **범위 초과 페이지 정책 유지**: `page > 마지막 페이지`는 리다이렉트하지 않고 빈 목록을 렌더링한다. 대신 번호 목록은 정상적으로 보여주고, 이전 버튼은 마지막 페이지로 돌아갈 수 있게 계산했다.

## 5. ✅ 검증 결과

- 실행 명령어: `.\gradlew.bat test --tests com.example.demo.board.*`
- 결과: 성공
- 확인한 내용:
  - `COUNT(*)` 기반 전체 개수 조회가 정상 동작한다.
  - `totalPageCount`, `hasNext`, `hasPrev`가 전체 개수 기준으로 정확히 계산된다.
  - 현재 페이지 번호만 `page-number--active`로 강조된다.
  - 마지막 페이지를 넘는 요청은 빈 목록으로 렌더링되고 번호 목록은 유지된다.
