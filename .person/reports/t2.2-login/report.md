# 🚩 작업 보고서: T2.2 로그인/로그아웃 및 헤더 분기

- **작업 일시**: 2026-03-19
- **진행 단계**: 완료

## 1. 🌊 전체 작업 흐름 (Workflow)

1. `todo.md`의 `T-2.2` 범위를 확인하고, 로그인/로그아웃뿐 아니라 헤더의 로그인/비로그인 분기 요구까지 반영하도록 계획을 확장했다.
2. `UserController`, `UserService`, `UserRequest`, `UserResponse`를 읽어 보니 로그인은 아직 비어 있고, 회원가입만 구현돼 있어서 세션 기반 로그인 흐름을 새로 추가했다.
3. 프로젝트 규칙에 맞게 `fetch`가 아니라 `<form method="post">`와 `x-www-form-urlencoded` 기반 로그인으로 구현했다.
4. 로그인 성공 시 세션에 최소 사용자 정보만 저장하도록 `SessionUser` DTO를 만들고, 모든 SSR 화면에서 공통으로 참조할 수 있게 `_core/web`에 `SessionUserAdvice`를 추가했다.
5. `header.mustache`는 `loginUser` 유무에 따라 메뉴를 다르게 보여주도록 바꾸고, `login-form.mustache`는 기존 토큰 디자인과 어울리는 SSR 화면으로 완성했다.
6. `UserServiceTest`, `UserControllerTest`를 추가/보강해 로그인 성공, 실패, 로그아웃, 헤더 분기를 검증했다.
7. `./gradlew test` 전체 테스트를 실행해 최종 통과를 확인했다.

## 2. 🧩 변경된 모든 코드 포함

- 브라우저가 로그인 폼을 제출하면 가장 먼저 `UserController`가 요청을 받는다.

```java
@PostMapping("/login")
public String login(UserRequest.LoginDTO requestDTO) {
    try {
        var sessionUser = userService.login(requestDTO);
        session.setAttribute("sessionUser", sessionUser);
        return "redirect:/";
    } catch (Exception400 exception) {
        var encodedMessage = UriUtils.encode(exception.getMessage(), StandardCharsets.UTF_8);
        return "redirect:/login-form?error=" + encodedMessage;
    }
}

@GetMapping("/logout")
public String logout() {
    session.invalidate();
    return "redirect:/";
}
```

- 로그인 입력값은 `LoginDTO`가 직접 검증한다.

```java
@Data
public static class LoginDTO implements SelfValidatable {
    private String username;
    private String password;

    @Override
    public void validate() {
        if (username == null || username.isBlank()) {
            throw new Exception400("아이디는 필수입니다.");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new Exception400("아이디는 4자 이상 20자 이하의 영문자와 숫자로 입력해주세요.");
        }
        if (password == null || password.isBlank()) {
            throw new Exception400("비밀번호는 필수입니다.");
        }
        if (password.length() < 4) {
            throw new Exception400("비밀번호는 4자 이상 입력해주세요.");
        }
    }
}
```

- 서비스는 사용자 조회와 BCrypt 검증을 담당하고, 세션에 넣을 최소 DTO만 반환한다.

```java
public UserResponse.SessionUser login(UserRequest.LoginDTO requestDTO) {
    requestDTO.validate();

    var user = userRepository.findByUsername(requestDTO.getUsername())
            .orElseThrow(() -> new Exception400("아이디 또는 비밀번호가 올바르지 않습니다."));

    if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
        throw new Exception400("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    return new UserResponse.SessionUser(user);
}
```

- 세션에 저장되는 값은 엔티티 전체가 아니라 화면 분기에 필요한 최소 정보다.

```java
@Data
public static class SessionUser {
    private Integer id;
    private String username;
    private String email;

    public SessionUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
```

- 모든 Mustache 화면이 로그인 상태를 공통으로 알 수 있도록 `_core/web`에 모델 주입 클래스를 추가했다.

```java
@RequiredArgsConstructor
@ControllerAdvice
public class SessionUserAdvice {

    private final HttpSession session;

    @ModelAttribute("loginUser")
    public UserResponse.SessionUser sessionUser() {
        return (UserResponse.SessionUser) session.getAttribute("sessionUser");
    }
}
```

- 헤더는 `loginUser`가 없을 때와 있을 때를 Mustache 섹션으로 분기한다.

```mustache
<nav class="site-nav" aria-label="주요 메뉴">
    <a class="site-nav__link" href="/">홈</a>
    {{^loginUser}}
    <a class="site-nav__link" href="/join-form">회원가입</a>
    <a class="site-nav__link site-nav__link--cta" href="/login-form">로그인</a>
    {{/loginUser}}
    {{#loginUser}}
    <span class="site-nav__welcome">{{username}}님 환영합니다</span>
    <a class="site-nav__link site-nav__link--cta" href="/logout">로그아웃</a>
    {{/loginUser}}
</nav>
```

- 로그인 화면은 JavaScript 없이도 동작하는 SSR 폼으로 완성했다.

```mustache
<form class="login-form" action="/login" method="post">
    <label class="form-field" for="username">
        <span class="form-field__label">아이디</span>
        <input id="username" name="username" class="form-field__input" type="text" autocomplete="username">
    </label>

    <label class="form-field" for="password">
        <span class="form-field__label">비밀번호</span>
        <input id="password" name="password" class="form-field__input" type="password" autocomplete="current-password">
    </label>

    <div class="login-form__submit">
        <button class="button button--primary" type="submit">로그인</button>
    </div>
</form>
```

- 테스트는 실제 사용자 관점의 흐름을 검증하도록 구성했다.

```java
mvc.perform(post("/login")
                .contentType("application/x-www-form-urlencoded")
                .param("username", "loginuser1")
                .param("password", "1234"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(request().sessionAttribute("sessionUser", hasProperty("username", is("loginuser1"))));

mvc.perform(get("/").session(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("ssar1234님 환영합니다")))
        .andExpect(content().string(containsString("로그아웃")));
```

## 3. 🍦 상세비유 쉬운 예시를 들어서 (Easy Analogy)

- 이번 작업은 영화관 입구에 표 확인 직원과 팔찌를 배치한 것과 같습니다.
- 로그인 폼은 매표소 창구이고, 사용자가 아이디/비밀번호를 제출하면 직원이 진짜 표인지 확인합니다.
- 확인이 끝나면 손목에 팔찌를 채워 주는데, 이 팔찌가 바로 `sessionUser`입니다.
- 헤더는 손목 팔찌를 보고 손님인지 아닌지를 바로 알아서, 비회원에게는 `회원가입/로그인`을 보여주고 회원에게는 `환영합니다/로그아웃`을 보여줍니다.

## 4. 📚 기술 딥다이브 (Technical Deep-dive)

- **HttpSession**
  - 서버가 브라우저별 로그인 상태를 기억하는 저장소다.
  - 이번에는 `sessionUser`라는 키로 최소 사용자 정보만 저장했다.
  - 엔티티 전체를 넣지 않고 DTO만 넣어서 뷰 분기와 유지보수를 단순하게 만들었다.

- **BCryptPasswordEncoder.matches()**
  - 회원가입 때 저장된 해시 비밀번호와 로그인 입력값을 안전하게 비교한다.
  - 평문 비밀번호를 그대로 DB와 비교하지 않기 때문에 보안상 안전하다.

```java
if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
    throw new Exception400("아이디 또는 비밀번호가 올바르지 않습니다.");
}
```

- **ControllerAdvice + ModelAttribute**
  - 여러 컨트롤러가 공통으로 써야 하는 데이터를 자동으로 뷰 모델에 넣는 기술이다.
  - 이번에는 헤더가 모든 화면에서 공통이기 때문에, 로그인 상태를 한 곳에서만 주입하도록 만들었다.

- **Mustache Section**
  - `{{#loginUser}} ... {{/loginUser}}`는 로그인 상태일 때만 렌더링된다.
  - `{{^loginUser}} ... {{/loginUser}}`는 로그인 상태가 아닐 때만 렌더링된다.
  - 덕분에 자바스크립트 없이도 헤더 메뉴가 서버 렌더링 단계에서 바로 달라진다.

- **검증 결과**
  - `./gradlew test` 전체 테스트 통과
