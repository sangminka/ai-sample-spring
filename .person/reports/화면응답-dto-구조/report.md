# 🚩 작업 보고서: 화면응답 DTO 구조 리팩터링

- **작업 일시**: 2026-03-23
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `.person/dicision/2026-03-23-화면응답-dto-구조.md`의 합의를 기준으로, 게시판 응답 DTO를 화면 단위 Response DTO와 페이징 메타 DTO로 분리하는 방향을 확정했다.
2. `BoardResponse`에서 기존 `Page`, `Detail(page 포함)` 구조를 `ListResponse`, `PagingResponse`, `DetailResponse` 구조로 재편했다.
3. `BoardService`가 화면에 필요한 데이터를 하나의 Response DTO로 완성해서 반환하도록 바꿨다.
4. `BoardController`는 요청값 검증, `redirect`, `model.addAttribute("model", dto)`만 담당하도록 정리했다.
5. `list.mustache`, `detail.mustache`의 바인딩 경로를 새 DTO 구조에 맞게 수정했다.
6. `BoardServiceTest`와 기존 `BoardControllerTest`가 함께 통과하는지 확인해 구조 변경이 화면 렌더링까지 안전하게 이어지는지 검증했다.

## 2. 🧩 변경된 모든 코드 포함

- `src/main/java/com/example/demo/board/BoardResponse.java`

```java
@Data
public static class PagingResponse {
    private Integer currentPage;
    private Integer pageSize;
    private Integer offset;
    private Integer boardCount;
    private boolean hasPrev;
    private Integer prevPage;
    private boolean hasNext;
    private Integer nextPage;

    public PagingResponse(Integer currentPage, Integer pageSize, Integer offset, Integer boardCount) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.offset = offset;
        this.boardCount = boardCount;
        this.hasPrev = currentPage > 1;
        this.prevPage = currentPage - 1;
        this.hasNext = boardCount == pageSize;
        this.nextPage = currentPage + 1;
    }
}

@Data
public static class ListResponse {
    private List<Min> boards;
    private PagingResponse paging;

    public ListResponse(Integer currentPage, Integer pageSize, Integer offset, List<Board> boards) {
        this.boards = boards.stream()
                .map(Min::new)
                .toList();
        this.paging = new PagingResponse(currentPage, pageSize, offset, this.boards.size());
    }
}

@Data
public static class DetailResponse {
    private Detail board;
    private Integer page;

    public DetailResponse(Board board, Integer page) {
        this.board = new Detail(board);
        this.page = page;
    }
}
```

- `src/main/java/com/example/demo/board/BoardService.java`

```java
public BoardResponse.ListResponse 게시글목록보기(Integer page) {
    int 보정페이지 = 페이지보정하기(page);
    int offset = (보정페이지 - 1) * PAGE_SIZE;

    return new BoardResponse.ListResponse(보정페이지, PAGE_SIZE, offset, boardRepository.findByPaging(offset));
}

public BoardResponse.DetailResponse 게시글상세보기(Integer id, Integer page) {
    var board = boardRepository.findById(id)
            .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

    return new BoardResponse.DetailResponse(board, 페이지보정하기(page));
}
```

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

    var listResponse = boardService.게시글목록보기(페이지번호);
    model.addAttribute("model", listResponse);
    return "list";
}

@GetMapping("/board/{id}")
public String detail(@PathVariable("id") Integer id,
        @RequestParam(required = false) String page,
        Model model) {
    var detailResponse = boardService.게시글상세보기(id, 상세페이지번호보정하기(page));
    model.addAttribute("model", detailResponse);
    return "detail";
}
```

- `src/main/resources/templates/list.mustache`

```mustache
{{! Model Key: model(BoardResponse.ListResponse) }}

<strong>{{model.paging.currentPage}}</strong>
<strong>{{model.paging.offset}} = ({{model.paging.currentPage}} - 1) * {{model.paging.pageSize}}</strong>

{{#model.boards}}
<a class="board-line" role="listitem" href="/board/{{id}}?page={{model.paging.currentPage}}">
    <span class="board-line__title">{{title}}</span>
</a>
{{/model.boards}}

{{#model.paging.hasPrev}}
<input type="hidden" name="page" value="{{model.paging.prevPage}}">
{{/model.paging.hasPrev}}

{{#model.paging.hasNext}}
<input type="hidden" name="page" value="{{model.paging.nextPage}}">
{{/model.paging.hasNext}}
```

- `src/main/resources/templates/detail.mustache`

```mustache
{{! Model Key: model(BoardResponse.DetailResponse) }}

<h1 class="board-detail-card__title">{{model.board.title}}</h1>
<span>작성자 {{model.board.username}}</span>
<article class="board-detail-card__content">
    {{model.board.content}}
</article>
<a class="button button--secondary" href="/board/list?page={{model.page}}">
    목록으로 돌아가기
</a>
```

- `src/test/java/com/example/demo/board/BoardServiceTest.java`

```java
var boardPage = boardService.게시글목록보기(1);

assertThat(boardPage.getPaging().getCurrentPage()).isEqualTo(1);
assertThat(boardPage.getPaging().getPageSize()).isEqualTo(3);
assertThat(boardPage.getPaging().getOffset()).isEqualTo(0);
assertThat(boardPage.getBoards()).hasSize(3);

var boardDetail = boardService.게시글상세보기(7, 2);

assertThat(boardDetail.getBoard().getTitle()).isEqualTo("상세 제목");
assertThat(boardDetail.getPage()).isEqualTo(2);
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 도시락을 다시 싸는 것과 같습니다. 예전에는 반찬, 밥, 수저를 따로따로 건네줬다면, 이제는 목록 화면용 도시락 하나(`ListResponse`)와 상세 화면용 도시락 하나(`DetailResponse`)로 나눠서 건네주는 구조로 바꿨습니다.
- 페이징 정보는 목록 화면에서만 쓰는 사이드 반찬이기 때문에 `PagingResponse`라는 별도 칸에 담았고, 상세 화면에는 돌아갈 페이지 번호만 작은 메모처럼 붙였습니다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **화면 단위 Response DTO 분리**: `ListResponse`와 `DetailResponse`를 분리해 각 화면이 정말 필요한 데이터만 가지도록 만들었다. 이렇게 하면 DTO 이름만 봐도 어느 화면을 위한 데이터인지 바로 이해할 수 있다.

```java
public BoardResponse.ListResponse 게시글목록보기(Integer page) {
    int offset = (page - 1) * PAGE_SIZE;
    return new BoardResponse.ListResponse(page, PAGE_SIZE, offset, boardRepository.findByPaging(offset));
}
```

- **페이징 메타 정보의 독립**: 페이지 목록 이동에 필요한 값은 `PagingResponse` 안에 몰아 넣었다. 덕분에 `list.mustache`는 `model.paging.hasNext`처럼 구조를 읽기 좋게 참조할 수 있다.

```mustache
{{#model.paging.hasNext}}
<input type="hidden" name="page" value="{{model.paging.nextPage}}">
{{/model.paging.hasNext}}
```

- **컨트롤러 책임 축소**: 컨트롤러는 잘못된 요청을 걸러내고, 서비스가 만든 화면용 DTO를 그대로 `model`에 넣는 역할만 담당한다. 이렇게 하면 웹 계층의 판단과 화면 데이터 조립이 서로 섞이지 않는다.

```java
var listResponse = boardService.게시글목록보기(페이지번호);
model.addAttribute("model", listResponse);
```

## 5. ✅ 검증 결과

- 실행 명령어: `.\gradlew.bat test --tests com.example.demo.board.*`
- 결과: 성공
- 확인한 내용:
  - 목록 DTO가 `ListResponse + PagingResponse` 구조로 정상 반환됨
  - 상세 DTO가 `DetailResponse + page` 구조로 정상 반환됨
  - 기존 목록/상세 화면이 새 Mustache 바인딩으로 정상 렌더링됨
  - 컨트롤러의 `redirect` 정책이 유지됨
