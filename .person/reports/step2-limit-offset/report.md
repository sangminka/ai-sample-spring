# 작업 보고서: Step 2 SQL LIMIT/OFFSET 페이징

- **작업 일시**: 2026-03-20
- **진행 상태**: 완료

## 1. 전체 작업 흐름

1. [TODO.md](C:/workspace/sample_lab/ai-sample-spring/TODO.md)의 Step 2 목표를 확인하고, 이번 단계에서는 UI 버튼보다 `LIMIT 3`, `OFFSET`, `page` 파라미터 학습에 집중하도록 범위를 고정했습니다.
2. [BoardRepository.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardRepository.java)에 native SQL 기반 `LIMIT 3 OFFSET :offset` 조회를 추가해 SQL이 직접 보이도록 만들었습니다.
3. [BoardService.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardService.java)에서 `page -> offset` 계산을 수행하고, 목록 데이터와 메타 정보를 함께 담는 [BoardResponse.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardResponse.java)의 `Page` DTO를 반환하도록 정리했습니다.
4. [BoardController.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardController.java)에서 `GET /board/list?page={n}` 흐름을 연결하고, 상세보기에서 다시 목록으로 돌아갈 때 현재 페이지 번호가 유지되도록 처리했습니다.
5. [list.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/list.mustache)와 [detail.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/detail.mustache)를 Step 2 학습용 화면으로 다듬고, [design-tokens.css](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/static/css/design-tokens.css)에 page/limit/offset 요약 UI를 위한 스타일을 추가했습니다.
6. [BoardServiceTest.java](C:/workspace/sample_lab/ai-sample-spring/src/test/java/com/example/demo/board/BoardServiceTest.java), [BoardControllerTest.java](C:/workspace/sample_lab/ai-sample-spring/src/test/java/com/example/demo/board/BoardControllerTest.java)로 첫 페이지 3건 조회, `page=1`일 때 `offset=3` 적용, 상세에서 목록 페이지 유지까지 검증했습니다.
7. `.\gradlew.bat test --tests com.example.demo.board.BoardServiceTest --tests com.example.demo.board.BoardControllerTest`와 `.\gradlew.bat test`를 모두 통과시킨 뒤, [TODO.md](C:/workspace/sample_lab/ai-sample-spring/TODO.md)의 `T-2.1`, `T-2.2`를 완료 처리했습니다.

## 2. 핵심 변경 코드

- Repository에서 SQL 페이징을 눈으로 확인할 수 있게 만들었습니다.

```java
@Query(value = "select * from board_tb order by id desc limit 3 offset :offset", nativeQuery = true)
List<Board> findByPaging(@Param("offset") Integer offset);
```

- Service는 `page`를 보정하고 `offset = page * 3`을 계산한 뒤, 화면에 필요한 메타 정보까지 묶어서 반환합니다.

```java
public BoardResponse.Page 게시글목록보기(Integer page) {
    int 보정페이지 = 페이지보정하기(page);
    int offset = 보정페이지 * PAGE_SIZE;

    return new BoardResponse.Page(보정페이지, PAGE_SIZE, offset, boardRepository.findByPaging(offset));
}
```

- 응답 DTO에 목록 데이터와 학습용 메타 정보를 함께 담았습니다.

```java
@Data
public static class Page {
    private Integer currentPage;
    private Integer pageSize;
    private Integer offset;
    private Integer boardCount;
    private List<Min> boardList;
}
```

- 컨트롤러는 목록 화면과 상세 화면에 필요한 값을 Mustache로 전달합니다.

```java
@GetMapping("/board/list")
public String list(@RequestParam(defaultValue = "0") Integer page, Model model) {
    var boardPage = boardService.게시글목록보기(page);
    model.addAttribute("boardList", boardPage.getBoardList());
    model.addAttribute("currentPage", boardPage.getCurrentPage());
    model.addAttribute("pageSize", boardPage.getPageSize());
    model.addAttribute("offset", boardPage.getOffset());
    model.addAttribute("boardCount", boardPage.getBoardCount());
    return "list";
}
```

- 목록 화면은 버튼 없이도 지금 어떤 페이징 계산이 적용됐는지 바로 보이게 했습니다.

```mustache
<section class="board-page__summary" aria-label="pagination summary">
    <div class="board-page__chip">
        <span>page</span>
        <strong>{{currentPage}}</strong>
    </div>
    <div class="board-page__chip">
        <span>limit</span>
        <strong>{{pageSize}}</strong>
    </div>
    <div class="board-page__chip">
        <span>offset</span>
        <strong>{{offset}}</strong>
    </div>
</section>
```

- 상세보기에서는 다시 현재 페이지로 되돌아갈 수 있게 링크를 유지했습니다.

```mustache
<a class="button button--secondary" href="/board/list?page={{page}}">
    <i class="fa-solid fa-arrow-left"></i>
    목록으로 돌아가기
</a>
```

## 3. 쉬운 비유

- 이번 작업은 책장을 한 번에 다 보는 대신, 맨 위에서 3권만 꺼내 보는 연습과 같습니다.
- `LIMIT 3`은 “이번에 딱 3권만 꺼내자”이고, `OFFSET 3`은 “앞의 3권은 건너뛰고 그다음 3권부터 보자”에 해당합니다.
- 그래서 `page`가 0이면 첫 3권, `page`가 1이면 다음 3권처럼 보이게 됩니다.

## 4. 기술 정리

- **LIMIT**
  - 한 번에 가져올 행 수를 고정합니다.
  - 이번 단계에서는 항상 3개만 보여주도록 고정했습니다.

- **OFFSET**
  - 앞에서 몇 개를 건너뛸지 결정합니다.
  - `page=1`이면 `offset=3`, `page=2`이면 `offset=6`이 됩니다.

- **0부터 시작하는 page**
  - Step 2는 계산 원리를 배우는 단계라서 `page`를 0부터 시작하게 두었습니다.
  - 이렇게 해야 `offset = page * limit` 공식이 가장 직관적으로 보입니다.

- **이번 단계에서 일부러 하지 않은 것**
  - 이전/다음 버튼
  - 페이지 번호 목록
  - 전체 페이지 수 계산
  - 검색 결합

- **검증 결과**
  - `.\gradlew.bat test --tests com.example.demo.board.BoardServiceTest --tests com.example.demo.board.BoardControllerTest` 통과
  - `.\gradlew.bat test` 전체 통과
