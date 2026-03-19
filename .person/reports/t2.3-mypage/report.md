# 🚩 작업 보고서: T2.3 회원 탈퇴 및 마이페이지 화면 구현

- **작업 일시**: 2026-03-19
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `todo.md`의 `T-2.3` 요구사항을 확인하고, 마이페이지 화면, 회원정보 수정, 회원 탈퇴, 연관 게시글/댓글 정리까지 하나의 흐름으로 묶었다.
2. 현재 코드베이스를 확인한 결과 `update-form` 화면과 수정/탈퇴 라우트가 없고, 사용자 삭제 시 게시글/댓글 처리 전략도 비어 있었다.
3. 범위를 넓히지 않기 위해 `soft delete` 대신 **서비스 레이어 수동 물리 삭제 전략**을 선택했다.
4. `UserRequest`에 `UpdateDTO`, `DeleteDTO`를 추가하고, `UserResponse`에는 `Detail`, `SessionUser`를 사용해 화면 바인딩과 세션 갱신을 분리했다.
5. `UserController`에 `GET /user/update-form`, `POST /user/update`, `POST /user/delete`를 추가했다.
6. `UserService`에서 회원정보 수정과 탈퇴 로직을 구현하고, 탈퇴 시 `내 댓글 -> 내 게시글의 댓글 -> 내 게시글 -> 내 계정` 순서로 정리했다.
7. `update-form.mustache`를 새로 만들고, `header.mustache`에는 로그인 상태 전용 `마이페이지` 메뉴를 추가했다.
8. `UserServiceTest`, `UserControllerTest`를 확장해 수정/탈퇴/연관 삭제/세션 갱신을 검증했고, `./gradlew test` 전체 통과를 확인했다.

## 2. 🧩 변경된 모든 코드 포함

- 마이페이지 진입과 수정/탈퇴 요청은 `UserController`가 받는다.

```java
@GetMapping("/user/update-form")
public String updateForm(
        @RequestParam(value = "success", required = false) String successMessage,
        @RequestParam(value = "error", required = false) String errorMessage,
        Model model) {
    var sessionUser = getSessionUser();
    if (sessionUser == null) {
        return redirectLoginWithError("로그인이 필요합니다.");
    }

    model.addAttribute("userDetail", userService.getDetail(sessionUser.getId()));
    model.addAttribute("successMessage", successMessage);
    model.addAttribute("errorMessage", errorMessage);
    return "update-form";
}

@PostMapping("/user/update")
public String update(UserRequest.UpdateDTO requestDTO) {
    var sessionUser = getSessionUser();
    if (sessionUser == null) {
        return redirectLoginWithError("로그인이 필요합니다.");
    }

    try {
        var updatedSessionUser = userService.update(sessionUser.getId(), requestDTO);
        session.setAttribute("sessionUser", updatedSessionUser);
        return "redirect:/user/update-form?success=" + encodeMessage("회원정보가 수정되었습니다.");
    } catch (Exception400 exception) {
        return "redirect:/user/update-form?error=" + encodeMessage(exception.getMessage());
    }
}
```

- 탈퇴는 현재 비밀번호 확인 후 서비스에서 연관 데이터 정리를 수행한다.

```java
@PostMapping("/user/delete")
public String delete(UserRequest.DeleteDTO requestDTO) {
    var sessionUser = getSessionUser();
    if (sessionUser == null) {
        return redirectLoginWithError("로그인이 필요합니다.");
    }

    try {
        userService.delete(sessionUser.getId(), requestDTO);
        session.invalidate();
        return "redirect:/login-form?message=" + encodeMessage("회원 탈퇴가 완료되었습니다.");
    } catch (Exception400 exception) {
        return "redirect:/user/update-form?error=" + encodeMessage(exception.getMessage());
    }
}
```

- 수정과 탈퇴용 DTO는 스스로 입력 규칙을 검증한다.

```java
@Data
public static class UpdateDTO implements SelfValidatable {
    private String email;
    private String postcode;
    private String address;
    private String detailAddress;
    private String extraAddress;

    @Override
    public void validate() {
        if (email == null || email.isBlank()) {
            throw new Exception400("이메일은 필수입니다.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new Exception400("이메일 형식이 올바르지 않습니다.");
        }
        if (postcode == null || postcode.isBlank()) {
            throw new Exception400("우편번호는 필수입니다.");
        }
        if (address == null || address.isBlank()) {
            throw new Exception400("주소는 필수입니다.");
        }
    }
}

@Data
public static class DeleteDTO implements SelfValidatable {
    private String password;

    @Override
    public void validate() {
        if (password == null || password.isBlank()) {
            throw new Exception400("현재 비밀번호는 필수입니다.");
        }
        if (password.length() < 4) {
            throw new Exception400("현재 비밀번호는 4자 이상 입력해주세요.");
        }
    }
}
```

- 서비스는 회원정보 수정과 탈퇴를 실제로 처리한다.

```java
@Transactional
public UserResponse.SessionUser update(Integer userId, UserRequest.UpdateDTO requestDTO) {
    requestDTO.validate();

    var user = findUser(userId);
    user.setEmail(requestDTO.getEmail());
    user.setPostcode(requestDTO.getPostcode());
    user.setAddress(requestDTO.getAddress());
    user.setDetailAddress(requestDTO.getDetailAddress());
    user.setExtraAddress(requestDTO.getExtraAddress());

    return new UserResponse.SessionUser(user);
}

@Transactional
public void delete(Integer userId, UserRequest.DeleteDTO requestDTO) {
    requestDTO.validate();

    var user = findUser(userId);
    if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
        throw new Exception400("현재 비밀번호가 올바르지 않습니다.");
    }

    replyRepository.deleteByUserId(userId);

    var boards = boardRepository.findAllByUserId(userId);
    if (!boards.isEmpty()) {
        replyRepository.deleteByBoardIn(boards);
        boardRepository.deleteByUserId(userId);
    }

    userRepository.delete(user);
}
```

- 마이페이지 화면은 수정 영역과 탈퇴 영역을 한 화면에 배치했다.

```mustache
<div class="profile-shell">
    <section class="profile-card">
        <form class="profile-form" action="/user/update" method="post">
            <input value="{{userDetail.username}}" readonly>
            <input name="email" value="{{userDetail.email}}">
            <input name="postcode" value="{{userDetail.postcode}}">
            <input name="address" value="{{userDetail.address}}">
            <button class="button button--primary" type="submit">회원정보 수정</button>
        </form>
    </section>

    <aside class="danger-card">
        <form class="danger-card__form" action="/user/delete" method="post">
            <input name="password" type="password">
            <button class="button button--danger" type="submit">회원 탈퇴</button>
        </form>
    </aside>
</div>
```

- 헤더는 로그인 상태일 때 마이페이지 링크를 노출한다.

```mustache
{{#loginUser}}
<span class="site-nav__welcome">{{username}}님 환영합니다</span>
<a class="site-nav__link" href="/user/update-form">마이페이지</a>
<a class="site-nav__link site-nav__link--cta" href="/logout">로그아웃</a>
{{/loginUser}}
```

- 테스트는 실제 사용 흐름과 데이터 정리를 함께 검증했다.

```java
mvc.perform(post("/user/update")
                .session(session)
                .contentType("application/x-www-form-urlencoded")
                .param("email", "after@example.com")
                .param("postcode", "04524")
                .param("address", "서울시 중구 세종대로")
                .param("detailAddress", "202호")
                .param("extraAddress", "서울시청"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/user/update-form?success=*"))
        .andExpect(request().sessionAttribute("sessionUser", hasProperty("email", is("after@example.com"))));
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 도서관 회원 카드 데스크를 만든 것과 비슷하다.
- 마이페이지는 회원이 주소나 연락처를 바꾸러 오는 창구다.
- 회원 탈퇴는 단순히 카드 한 장만 없애는 게 아니라, 그 회원 이름으로 빌려 둔 책 기록과 메모까지 같이 정리하는 과정이다.
- 그래서 탈퇴 전에 본인 확인용 비밀번호를 다시 묻고, 관련 기록을 순서대로 지운 뒤 마지막에 회원 카드 자체를 없앴다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **서비스 레이어 수동 물리 삭제**
  - 현재 엔티티에는 `deleted` 같은 soft delete 필드가 없어서, soft delete를 넣으려면 조회 조건과 로그인 정책 전반을 다 바꿔야 한다.
  - 그래서 이번 단계에서는 범위를 제어하기 위해 서비스 레이어에서 삭제 순서를 명시하는 전략을 택했다.

- **BCrypt 재검증**
  - 탈퇴는 위험한 작업이라, 로그인된 상태여도 현재 비밀번호를 한 번 더 확인했다.

```java
if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
    throw new Exception400("현재 비밀번호가 올바르지 않습니다.");
}
```

- **세션 최신화**
  - 회원정보 수정 후 DB만 바꾸면 헤더 환영 영역과 다른 화면이 예전 정보를 계속 볼 수 있다.
  - 그래서 수정 직후 `sessionUser`도 새 DTO로 다시 넣어 세션과 DB를 동시에 맞췄다.

- **SSR + form 전송**
  - 프로젝트 규칙에 맞춰 `fetch` 대신 `<form method="post">`와 `x-www-form-urlencoded`를 사용했다.
  - 덕분에 컨트롤러가 리다이렉트와 메시지 전달을 중심으로 흐름을 단순하게 유지할 수 있었다.

- **검증 결과**
  - `./gradlew test` 전체 테스트 통과
