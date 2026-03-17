# T-2.1 Step 1 보고서

## 작업 목적

회원가입 기능의 첫 단계로 아이디 중복 체크 API와 Fetch API 기반 비동기 통신 화면을 구현했다.
이번 단계의 목표는 회원 저장 전체가 아니라 `입력 -> 요청 -> 응답 -> 화면 반영` 흐름을 학습 가능한 형태로 만드는 것이다.

## 전체 흐름

1. `TODO.md`의 `T-2.1 Step 1` 요구사항을 다시 확인했다.
2. `user` 패키지와 템플릿 현재 상태를 읽고 회원가입 화면과 중복 체크 API가 비어 있음을 확인했다.
3. `UserService`에 username 중복 판별 로직을 추가했다.
4. `UserApiController`에 아이디 중복 체크 API를 추가했다.
5. `UserController`에 `/join-form` 라우트를 추가했다.
6. `join-form.mustache`와 CSS를 추가해 Fetch API 기반 비동기 화면을 구현했다.
7. `UserServiceTest`를 추가하고 테스트를 실행해 중복 체크 로직을 검증했다.

## 핵심 변경 사항

- `src/main/java/com/example/demo/user/UserService.java`
  - 공백 입력을 방어하고 username 중복 여부를 반환하는 `isUsernameAvailable` 메서드를 추가했다.
- `src/main/java/com/example/demo/user/UserApiController.java`
  - `/api/usernames/check` GET API를 추가했다.
  - 응답은 `Resp.ok(...)` 형태로 `username`, `available`, `message`를 내려주도록 구성했다.
- `src/main/java/com/example/demo/user/UserController.java`
  - `/join-form` 화면 라우트를 추가했다.
- `src/main/resources/templates/join-form.mustache`
  - 아이디 입력, 중복 체크 버튼, 상태 메시지, Fetch API 흐름 설명을 담은 화면을 만들었다.
- `src/main/resources/static/css/design-tokens.css`
  - 회원가입 Step 1 화면에 맞는 폼, 상태 패널, 안내 레이아웃 스타일을 추가했다.
- `src/test/java/com/example/demo/user/UserServiceTest.java`
  - 사용 가능/중복 두 케이스를 검증하는 테스트를 추가했다.

## 구현 포인트

### 백엔드

```java
@GetMapping("/api/usernames/check")
public Object checkUsername(@RequestParam("username") String username) {
    var isAvailable = userService.isUsernameAvailable(username);
    var message = isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";

    return Resp.ok(Map.of(
            "username", username,
            "available", isAvailable,
            "message", message));
}
```

아이디 하나를 받아 중복 여부와 안내 메시지를 JSON으로 반환한다.

### 프론트엔드

```javascript
async function checkUsername() {
    const username = usernameInput.value.trim();

    if (!username) {
        renderStatus("아이디를 먼저 입력해주세요.", "warning");
        usernameInput.focus();
        return;
    }

    const response = await fetch(`/api/usernames/check?username=${encodeURIComponent(username)}`);
    const responseBody = await response.json();
    const result = responseBody.body;

    renderStatus(result.message, result.available ? "success" : "error");
}
```

입력값을 확인한 뒤 Fetch API로 서버를 호출하고, 받은 응답을 즉시 화면 문구에 반영한다.

## 쉽게 설명하면

도서관에서 책을 빌리기 전에 사서에게 "이 책 아직 있나요?"라고 먼저 물어보는 과정과 비슷하다.
사용자는 아이디를 입력하고 버튼을 누르고, 화면은 서버에게 "이 이름 이미 누가 쓰고 있나요?"를 물어본 뒤, 답을 받아 바로 알려준다.

## 기술 용어 풀이

- Fetch API
  - 브라우저에서 서버로 HTTP 요청을 보내는 자바스크립트 기능이다.
- 비동기 통신
  - 페이지 전체를 새로고침하지 않고 필요한 데이터만 서버와 주고받는 방식이다.
- SSR
  - 서버가 HTML을 만들어 브라우저에 보내주는 렌더링 방식이다.

## 검증 결과

- 실행 명령: `./gradlew test --tests com.example.demo.user.UserServiceTest`
- 결과: 통과

## 남은 일

- Step 2에서 `JoinDTO`와 공통 유효성 검사 로직을 붙여 실제 회원가입 입력 전체를 검증해야 한다.
- Step 3에서 비밀번호 암호화와 저장 로직을 추가해야 한다.
- Finalize 단계에서 `frontend-design` 스킬 기준의 최종 UI 다듬기가 가능하다.
