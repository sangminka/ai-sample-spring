# 🚩 작업 보고서: page 파싱 책임 정리

- **작업 일시**: 2026-03-23
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `.person/dicision/2026-03-23-page-파싱-책임-정리.md`의 결론을 기준으로, 컨트롤러에 남길 `page` 처리 책임과 서비스로 넘길 보정 책임을 다시 확인했다.
2. `BoardController`에서 상세 페이지 전용 보정 메서드 `상세페이지번호보정하기(String page)`를 제거했다.
3. `페이지번호파싱하기(String page)`가 `null`, 빈 문자열, 숫자 형식 오류를 모두 `null`로 정리하도록 바꿔 `list/detail` 공용 파서 역할만 담당하게 했다.
4. `list`는 파싱 결과를 기준으로 `redirect:/board/list?page=1`을 결정하고, `detail`은 파싱 결과를 그대로 `BoardService`로 넘기도록 정리했다.
5. `BoardServiceTest`에 상세 페이지 번호 보정 책임이 서비스에 있다는 테스트를 추가하고, `BoardControllerTest`에는 잘못된 `page`로 상세 진입했을 때도 목록 복귀 링크가 `page=1`로 떨어지는지 검증을 추가했다.
6. `.\gradlew.bat test --tests com.example.demo.board.*`로 게시판 관련 테스트를 실행해 정책이 의도대로 유지되는지 확인했다.

## 2. 🧩 변경된 모든 코드 포함

- `src/main/java/com/example/demo/board/BoardController.java`

```java
@GetMapping("/board/list")
public String list(@RequestParam(required = false) String page, Model model) {
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
    var detailResponse = boardService.게시글상세보기(id, 페이지번호파싱하기(page));
    model.addAttribute("model", detailResponse);
    return "detail";
}

private Integer 페이지번호파싱하기(String page) {
    if (page == null || page.isBlank()) {
        return null;
    }

    try {
        return Integer.parseInt(page);
    } catch (NumberFormatException e) {
        return null;
    }
}
```

- `src/test/java/com/example/demo/board/BoardControllerTest.java`

```java
@DisplayName("게시글 상세 페이지는 잘못된 page가 와도 첫 페이지 복귀 링크를 사용한다")
@Test
void detail_fallsBackToFirstPageWhenPageInvalid() throws Exception {
    var savedBoard = boardRepository.save(Board.builder()
            .title("상세 잘못된 페이지 테스트")
            .content("상세 잘못된 페이지 본문")
            .user(userRepository.findById(2).orElseThrow())
            .build());

    mvc.perform(get("/board/" + savedBoard.getId()).param("page", "abc"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("상세 잘못된 페이지 테스트")))
            .andExpect(content().string(containsString("/board/list?page=1")));
}
```

- `src/test/java/com/example/demo/board/BoardServiceTest.java`

```java
@DisplayName("게시글상세보기는 잘못된 page를 1로 보정한다")
@Test
void 게시글상세보기_correctsInvalidPage() {
    var board = Board.builder()
            .id(8)
            .title("보정 제목")
            .content("보정 내용")
            .user(User.builder().id(1).username("ssar").build())
            .createdAt(LocalDateTime.of(2026, 3, 20, 9, 20))
            .build();
    when(boardRepository.findById(8)).thenReturn(Optional.of(board));

    var nullPageDetail = boardService.게시글상세보기(8, null);
    var zeroPageDetail = boardService.게시글상세보기(8, 0);

    assertThat(nullPageDetail.getPage()).isEqualTo(1);
    assertThat(zeroPageDetail.getPage()).isEqualTo(1);
}
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 학교 정문과 교실의 역할을 나누는 것과 같습니다. 정문인 컨트롤러는 학생증을 대충 확인하고 통과 여부를 판단하고, 교실인 서비스는 들어온 학생을 최종 자리로 안내합니다.
- 목록 화면의 `page`는 정문에서 엄격하게 확인해야 하는 출입증이고, 상세 화면의 `page`는 "어느 반에서 왔는지" 정도의 참고 정보라서 교실에서 `1반`으로 정리해 줘도 되는 문맥 값입니다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **공용 파서 하나로 역할 축소**: `페이지번호파싱하기(String page)`는 HTTP 요청 문자열을 `Integer` 또는 `null`로 바꾸는 책임만 가진다. `null`과 빈 문자열을 직접 처리하도록 해 보조 메서드 없이도 공용 파서로 쓸 수 있게 만들었다.

```java
private Integer 페이지번호파싱하기(String page) {
    if (page == null || page.isBlank()) {
        return null;
    }

    try {
        return Integer.parseInt(page);
    } catch (NumberFormatException e) {
        return null;
    }
}
```

- **list와 detail의 정책 분리**: 같은 `page` 파라미터라도 `list`는 조회 결과를 바꾸기 때문에 엄격히 검사하고, `detail`은 복귀 문맥이므로 서비스로 그대로 넘겨 최종 보정만 맡긴다.

```java
if (페이지번호 == null || 페이지번호 < 1) {
    return "redirect:/board/list?page=1";
}
```

```java
var detailResponse = boardService.게시글상세보기(id, 페이지번호파싱하기(page));
```

- **최종 보정 규칙의 단일화**: 상세 화면의 `page`가 잘못됐을 때의 최종 보정은 서비스 `페이지보정하기(Integer page)`가 맡는다. 이렇게 하면 컨트롤러와 서비스가 같은 보정 규칙을 두 번 갖지 않아도 된다.

## 5. ✅ 검증 결과

- 실행 명령어: `.\gradlew.bat test --tests com.example.demo.board.*`
- 결과: 성공
- 확인한 내용:
  - 목록 페이지는 여전히 잘못된 `page` 요청을 첫 페이지로 리다이렉트한다.
  - 상세 페이지는 잘못된 `page`가 와도 화면은 정상 렌더링되고 목록 복귀 링크는 `page=1`이 된다.
  - 서비스가 `null`, `0` 같은 잘못된 상세 `page`를 `1`로 보정한다.
