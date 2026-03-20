# 🚩 작업 보고서: Step 1 게시글 전체 목록과 상세보기

- **작업 일시**: 2026-03-20
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `TODO.md`의 Step 1 목표를 확인하고, 학습 흐름에 맞춰 `전체 목록 -> 상세보기 이동` 구조로 정리했다.
2. [data.sql](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/db/data.sql)에 게시글을 23건까지 늘려 이후 페이징 실습에 충분한 데이터를 만들었다.
3. [BoardRepository.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardRepository.java), [BoardService.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardService.java), [BoardResponse.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardResponse.java)를 채워 목록/상세 DTO와 조회 로직을 만들었다.
4. [BoardController.java](C:/workspace/sample_lab/ai-sample-spring/src/main/java/com/example/demo/board/BoardController.java)에 `/board/list`, `/board/{id}` 라우트를 추가했다.
5. [list.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/list.mustache)는 한 줄형 목록으로 만들고, [detail.mustache](C:/workspace/sample_lab/ai-sample-spring/src/main/resources/templates/detail.mustache)는 상세 페이지로 구성했다.
6. [BoardServiceTest.java](C:/workspace/sample_lab/ai-sample-spring/src/test/java/com/example/demo/board/BoardServiceTest.java), [BoardControllerTest.java](C:/workspace/sample_lab/ai-sample-spring/src/test/java/com/example/demo/board/BoardControllerTest.java)로 기능을 검증했고, `./gradlew test` 전체 통과를 확인했다.
7. `TODO.md`의 `T-1.1`, `T-1.2`, `T-1.3`을 완료 상태로 체크했다.

## 2. 🧩 변경된 모든 코드 포함

- 게시글 목록과 상세보기는 서비스에서 DTO로 변환한다.

```java
public List<BoardResponse.Min> 게시글목록보기() {
    return boardRepository.findAllByOrderByIdDesc().stream()
            .map(BoardResponse.Min::new)
            .toList();
}

public BoardResponse.Detail 게시글상세보기(Integer id) {
    var board = boardRepository.findById(id)
            .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

    return new BoardResponse.Detail(board);
}
```

- 목록과 상세 DTO는 화면에 필요한 값만 담는다.

```java
@Data
public static class Min {
    private Integer id;
    private String title;
    private String username;
    private String createdAtText;

    public Min(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.username = board.getUser().getUsername();
        this.createdAtText = board.getCreatedAt().format(DATE_TIME_FORMATTER);
    }
}
```

- 컨트롤러는 목록 페이지와 상세 페이지를 분리해 렌더링한다.

```java
@GetMapping("/board/list")
public String list(Model model) {
    model.addAttribute("boardList", boardService.게시글목록보기());
    return "list";
}

@GetMapping("/board/{id}")
public String detail(@PathVariable("id") Integer id, Model model) {
    model.addAttribute("board", boardService.게시글상세보기(id));
    return "detail";
}
```

- 목록 화면은 한 줄형이고, 각 줄 전체를 클릭하면 상세 페이지로 이동한다.

```mustache
{{#boardList}}
<a class="board-line" role="listitem" href="/board/{{id}}">
    <span class="board-line__id">#{{id}}</span>
    <span class="board-line__title">{{title}}</span>
    <span class="board-line__author">{{username}}</span>
    <span class="board-line__date">{{createdAtText}}</span>
</a>
{{/boardList}}
```

- 상세 화면은 제목, 작성자, 작성일, 본문, 목록 복귀 버튼만 두는 최소 학습형 구조다.

```mustache
{{#board}}
<h1 class="board-detail-card__title">{{title}}</h1>
<div class="board-detail-card__meta">
    <span>작성자 {{username}}</span>
    <span>작성일 {{createdAtText}}</span>
    <span>번호 #{{id}}</span>
</div>
<article class="board-detail-card__content">
    {{content}}
</article>
{{/board}}
```

- 테스트는 목록/상세의 핵심 학습 흐름을 검증한다.

```java
mvc.perform(get("/board/list"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("게시글 전체 목록")))
        .andExpect(content().string(containsString("목록 테스트용 게시글")))
        .andExpect(content().string(containsString("/board/" + savedBoard.getId())));
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 도서관 책장을 먼저 통째로 펼쳐 보는 연습과 비슷하다.
- 아직 페이지를 나눠 꽂는 법은 배우지 않았고, 먼저 책이 어떻게 한 줄 목록으로 보이는지 익혔다.
- 책 제목 한 줄을 누르면 그 책의 상세 설명 페이지로 넘어가도록 만들어, `목록`과 `상세`의 역할 차이도 함께 공부할 수 있게 했다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **정렬 있는 전체 목록 조회**
  - Step 1에서는 아직 `LIMIT/OFFSET`을 쓰지 않지만, 목록 순서는 안정적이어야 한다.
  - 그래서 `findAll()` 대신 `findAllByOrderByIdDesc()`를 사용해 최신글 순으로 고정했다.

- **DTO 변환 위치**
  - Entity를 뷰에 직접 넘기지 않고 서비스에서 DTO로 바꿨다.
  - 이렇게 해두면 Step 2 이후 페이징이 붙어도 화면이 필요한 데이터 구조는 그대로 유지된다.

- **목록/상세 분리**
  - 목록은 `요약`, 상세는 `전체 내용`을 보여주는 역할 분리다.
  - 이후 페이징은 목록 쿼리만 잘라내면 되므로 학습 흐름이 더 단순해진다.

- **검증 결과**
  - `./gradlew test --tests com.example.demo.board.BoardServiceTest --tests com.example.demo.board.BoardControllerTest` 통과
  - `./gradlew test` 전체 통과
