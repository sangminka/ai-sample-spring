# 작업 보고서 [게시글 검색 Phase 1]

- **작업 일시**: 2026-03-24
- **진행 상태**: 완료

## 1. 전체 작업 흐름 (Workflow)

1. 게시글 검색 Phase 1 요구사항을 `SSR 검색` 범위로 확정했다.
2. `.person/dicision/2026-03-24-게시글-검색-기능.md`의 Phase 1 체크리스트를 기준으로 구현 범위를 정리했다.
3. `BoardRequest.Search` DTO를 추가해서 `page`, `keyword`를 하나의 검색 요청 객체로 묶었다.
4. `BoardController`가 검색 DTO를 받고, 잘못된 `page` 요청은 첫 페이지로 리다이렉트하면서 `keyword`를 유지하도록 바꿨다.
5. `BoardService`에서 `keyword trim`, 빈 검색어면 전체 목록 처리, 검색이면 `count + paging + list` 계산을 통합했다.
6. `BoardRepository`에 `제목 + 내용` 통합 검색용 목록 조회 쿼리와 개수 조회 쿼리를 추가했다.
7. `BoardResponse`에 `keyword`, `encodedKeyword`, `hasKeyword`를 추가해서 화면에서 검색 상태를 안전하게 유지하도록 했다.
8. `list.mustache`에 검색 form을 추가하고, 페이지 이동/상세 이동 시 `page + keyword`가 유지되도록 바꿨다.
9. `detail.mustache`의 목록 복귀 링크에도 `keyword` 유지 로직을 추가했다.
10. 서비스 테스트와 컨트롤러 테스트에 검색/빈 결과/검색 상태 유지 케이스를 추가했다.
11. `./gradlew.bat test --tests com.example.demo.board.*`로 검증했고 Phase 1 범위 테스트를 통과했다.
12. 체크리스트의 Phase 1 항목을 모두 완료 처리했다.

## 2. 변경한 코드 정리

### 2-1. 검색 요청 DTO 추가

```java
@Data
public static class Search {
    private String page;
    private String keyword;
}
```

- 파일: `src/main/java/com/example/demo/board/BoardRequest.java`
- 목적: 검색 요청을 `page`, `keyword`로 일관되게 전달하기 위해 추가했다.

### 2-2. 컨트롤러에서 검색 DTO 사용 및 첫 페이지 리다이렉트 보강

```java
@GetMapping("/board/list")
public String list(BoardRequest.Search requestDTO, Model model) {
    var 페이지번호 = 페이지번호파싱하기(requestDTO.getPage());
    if (페이지번호 == null || 페이지번호 < 1) {
        return 목록첫페이지주소(requestDTO.getKeyword());
    }

    var listResponse = boardService.게시글목록보기(requestDTO);
    model.addAttribute("model", listResponse);
    return "list";
}

private String 목록첫페이지주소(String keyword) {
    var 정리키워드 = 키워드정리하기(keyword);
    if (정리키워드 == null) {
        return "redirect:/board/list?page=1";
    }

    return "redirect:/board/list?page=1&keyword=" + UriUtils.encode(정리키워드, StandardCharsets.UTF_8);
}
```

- 파일: `src/main/java/com/example/demo/board/BoardController.java`
- 핵심: `page`가 없거나 잘못되어도 검색 상태를 잃지 않고 `page=1`로 정리해서 이동하게 했다.

### 2-3. 서비스에 검색 규칙 통합

```java
public BoardResponse.ListResponse 게시글목록보기(BoardRequest.Search requestDTO) {
    var 검색어 = 키워드정리하기(requestDTO.getKeyword());
    int 보정페이지 = 페이지보정하기(페이지번호파싱하기(requestDTO.getPage()));
    int offset = (보정페이지 - 1) * PAGE_SIZE;
    int totalCount = 전체개수구하기(검색어);
    int totalPageCount = 총페이지수구하기(totalCount);
    var boards = 게시글목록조회하기(검색어, offset);
    int boardCount = boards.size();
    var pageNumbers = 페이지번호목록구하기(totalPageCount, 보정페이지);

    var paging = new BoardResponse.PagingResponse(
            보정페이지,
            PAGE_SIZE,
            offset,
            boardCount,
            totalCount,
            totalPageCount,
            이전페이지있음(보정페이지, totalPageCount),
            이전페이지구하기(보정페이지, totalPageCount),
            다음페이지있음(보정페이지, totalPageCount),
            다음페이지구하기(보정페이지),
            pageNumbers);

    return new BoardResponse.ListResponse(boards, paging, 검색어);
}
```

```java
private String 키워드정리하기(String keyword) {
    if (keyword == null) {
        return null;
    }

    var trimmedKeyword = keyword.trim();
    if (trimmedKeyword.isBlank()) {
        return null;
    }

    return trimmedKeyword;
}
```

- 파일: `src/main/java/com/example/demo/board/BoardService.java`
- 핵심: `trim -> 빈 값이면 전체 목록 -> 검색이면 count/list 분기` 흐름을 서비스 한 곳에 모았다.

### 2-4. 레포지토리 검색 쿼리 추가

```java
@Query(value = """
        select * from board_tb
        where title like concat('%', :keyword, '%')
           or content like concat('%', :keyword, '%')
        order by id desc
        limit 3 offset :offset
        """, nativeQuery = true)
List<Board> findByPagingAndKeyword(@Param("keyword") String keyword, @Param("offset") Integer offset);

@Query(value = """
        select count(*) from board_tb
        where title like concat('%', :keyword, '%')
           or content like concat('%', :keyword, '%')
        """, nativeQuery = true)
Long countByKeyword(@Param("keyword") String keyword);
```

- 파일: `src/main/java/com/example/demo/board/BoardRepository.java`
- 핵심: `제목 + 내용` 포함 검색과 전체 개수 계산을 같은 조건으로 맞췄다.

### 2-5. 응답 DTO에 검색 상태 추가

```java
@Data
public static class ListResponse {
    private List<Min> boards;
    private PagingResponse paging;
    private String keyword;
    private String encodedKeyword;
    private boolean hasKeyword;

    public ListResponse(List<Board> boards, PagingResponse paging, String keyword) {
        this.boards = boards.stream().map(Min::new).toList();
        this.paging = paging;
        var normalizedKeyword = 정리된키워드(keyword);
        this.keyword = 출력용키워드(normalizedKeyword);
        this.encodedKeyword = 출력용키워드(인코딩된키워드(normalizedKeyword));
        this.hasKeyword = normalizedKeyword != null;
    }
}
```

```java
private static String 출력용키워드(String keyword) {
    if (keyword == null) {
        return "";
    }
    return keyword;
}
```

- 파일: `src/main/java/com/example/demo/board/BoardResponse.java`
- 핵심: Mustache에서 `null keyword`를 직접 렌더링하다가 예외가 나는 문제를 피하려고 출력용 키워드를 빈 문자열로 바꿨다.

### 2-6. 목록 화면에 검색 form과 상태 유지 링크 추가

```mustache
<form method="get" action="/board/list" class="row g-2 align-items-end">
    <input type="hidden" name="page" value="1">
    <div class="col-12 col-md-9">
        <label class="form-label" for="keyword">검색어</label>
        <input id="keyword"
            type="text"
            name="keyword"
            class="form-control"
            value="{{model.keyword}}"
            placeholder="제목 또는 내용을 입력하세요">
    </div>
    <div class="col-12 col-md-3">
        <button class="button w-100" type="submit">검색</button>
    </div>
</form>
```

```mustache
<a class="board-line"
    role="listitem"
    href="/board/{{id}}?page={{model.paging.currentPage}}{{#model.hasKeyword}}&keyword={{model.encodedKeyword}}{{/model.hasKeyword}}">
```

```mustache
{{^model.boards}}
<div class="board-line board-line--empty">
    {{#model.hasKeyword}}
    <span>'{{model.keyword}}' 검색 결과가 없습니다.</span>
    {{/model.hasKeyword}}
    {{^model.hasKeyword}}
    <span>이 페이지에는 게시글이 없습니다.</span>
    {{/model.hasKeyword}}
</div>
{{/model.boards}}
```

- 파일: `src/main/resources/templates/list.mustache`
- 핵심: 검색 form, 검색 결과 없음 문구, 상세/페이징 링크의 `keyword` 유지까지 한 번에 처리했다.

### 2-7. 상세 화면에서 검색 상태 유지

```mustache
<a class="button button--secondary"
    href="/board/list?page={{model.page}}{{#model.hasKeyword}}&keyword={{model.encodedKeyword}}{{/model.hasKeyword}}">
    목록으로 돌아가기
</a>
```

- 파일: `src/main/resources/templates/detail.mustache`
- 핵심: 검색 결과 목록에서 상세로 들어갔다가 돌아올 때 검색 상태가 유지되게 했다.

### 2-8. 테스트 보강

```java
@DisplayName("게시글 목록은 제목과 내용을 함께 검색하고 검색어를 유지한다")
@Test
void list_search_success() throws Exception {
    mvc.perform(get("/board/list").param("page", "1").param("keyword", " phase1-search "))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("value=\"phase1-search\"")))
            .andExpect(content().string(containsString("/board/" + titleMatchedBoard.getId() + "?page=1&keyword=phase1-search")));
}
```

```java
@DisplayName("게시글목록보기는 trim한 검색어로 제목과 내용을 함께 검색한다")
@Test
void 게시글목록보기_searchesByTrimmedKeyword() {
    var boardPage = boardService.게시글목록보기(검색요청("1", "  스프링  "));

    verify(boardRepository).countByKeyword("스프링");
    verify(boardRepository).findByPagingAndKeyword("스프링", 0);
    assertThat(boardPage.getKeyword()).isEqualTo("스프링");
}
```

- 파일:
  - `src/test/java/com/example/demo/board/BoardControllerTest.java`
  - `src/test/java/com/example/demo/board/BoardServiceTest.java`
- 핵심: 검색 trim, 제목/내용 통합 검색, 빈 결과 문구, 검색 상태 유지 링크를 테스트로 묶었다.

## 3. 쉬운 비유 (Easy Analogy)

- 이번 작업은 도서관 책장 앞에 `검색대`를 하나 놓은 것과 비슷하다.
- 예전에는 1페이지, 2페이지로 책장을 넘기면서 원하는 책을 직접 찾아야 했다.
- 이제는 검색대에 키워드를 넣으면 `제목`이든 `본문`이든 그 키워드가 들어간 책들만 다시 추려서 보여준다.
- 그리고 어떤 책을 펼쳐 봤다가 다시 돌아와도, 방금 검색했던 조건을 잊지 않고 같은 책장 위치로 되돌아오게 만든 셈이다.

## 4. 기술 딥다이브 (Technical Deep-dive)

### 검색어 정규화 위치를 서비스로 둔 이유

- 컨트롤러에서 `trim`과 검색 여부 판단을 다 해버리면, 나중에 AJAX API를 추가할 때 같은 규칙을 다시 복붙하게 된다.
- 그래서 서비스에 `키워드정리하기()`를 두고, `SSR form`과 이후 `AJAX fetch`가 같은 규칙을 공유하도록 만들었다.

### 검색과 전체 목록의 `count`를 분리한 이유

- 검색 목록만 바꾸고 `count(*)`를 전체 게시글 기준으로 두면 페이지 수가 틀어진다.
- 예를 들어 검색 결과가 2건인데 전체 개수는 21건이면, 화면에 불필요한 페이지 번호가 계속 보인다.
- 그래서 `countByKeyword()`와 `findByPagingAndKeyword()`를 같은 조건으로 맞췄다.

### Mustache에서 `null keyword`를 빈 문자열로 바꾼 이유

- Mustache 렌더링 과정에서 `{{model.keyword}}`가 `null`이면 예외가 날 수 있었다.
- 실제로 첫 테스트에서 `No key, method or field with name 'model.keyword'` 오류가 발생했다.
- 이 문제를 `BoardResponse`에서 출력용 키워드를 `""`로 바꾸는 방식으로 해결했다.
- 덕분에 검색어가 없을 때도 검색 input은 안전하게 렌더링되고, `hasKeyword`만으로 분기할 수 있게 됐다.

## 5. 검증 결과

- 실행 명령:

```bash
./gradlew.bat test --tests com.example.demo.board.*
```

- 결과: 성공
- 확인한 범위:
  - 잘못된 `page` 요청의 첫 페이지 리다이렉트
  - 제목/내용 통합 검색
  - 검색어 `trim`
  - 빈 검색어면 전체 목록
  - 검색 결과 없음 문구
  - 상세 진입 후 목록 복귀 시 `page + keyword` 유지
  - 기존 페이징 테스트 유지

## 6. 다음 단계

1. `BoardApiController`를 추가해서 `GET /api/board/search` JSON 응답을 만든다.
2. 목록 페이지에 `input` 이벤트 기반 AJAX 검색을 붙인다.
3. `300ms` 디바운스를 적용하고 마지막 요청만 반영되도록 안정화한다.
