# T-2.1 Step 3 ~ Finalize 보고서

## 작업 목적

회원가입 기능의 마지막 단계로 BCrypt 암호화를 적용하고 실제 회원 저장을 구현했다.
이후 `frontend-design` 기준으로 `join-form.mustache`를 최종 정리하고, 전체 가입 흐름이 끝까지 동작하는지 검증했다.

## 전체 흐름

1. `TODO.md`의 Step 3과 Finalize 범위를 확인했다.
2. 현재 Step 2 상태를 읽고, 검증만 하던 `/api/users/join`을 실제 저장 흐름으로 확장하기로 정했다.
3. `spring-security-crypto`를 추가하고 BCrypt 인코더 빈을 등록했다.
4. `UserService.join(...)`에 중복 확인, 암호화, 엔티티 저장 로직을 구현했다.
5. `UserApiController`를 실제 회원가입 응답 흐름으로 변경했다.
6. `join-form.mustache`와 `design-tokens.css`를 최종 화면 수준으로 다듬었다.
7. 서비스/컨트롤러 테스트를 보강하고 전체 `./gradlew test`를 통과시켰다.

## 핵심 변경 사항

- `build.gradle`
  - `spring-security-crypto`를 추가해 BCrypt 암호화를 사용할 수 있게 했다.
- `src/main/java/com/example/demo/_core/utils/PasswordEncoderConfig.java`
  - `BCryptPasswordEncoder` 빈을 등록했다.
- `src/main/java/com/example/demo/user/UserService.java`
  - `join(...)` 메서드를 추가했다.
  - username 중복을 다시 확인하고, 비밀번호를 BCrypt로 암호화한 뒤 저장한다.
- `src/main/java/com/example/demo/user/UserApiController.java`
  - `/api/users/join`이 검증 통과용 API에서 실제 가입 처리 API가 되도록 바꿨다.
- `src/main/resources/templates/join-form.mustache`
  - 최종 가입 흐름 중심의 SSR 화면으로 재구성했다.
  - 가입 성공 시 저장된 사용자 정보와 함께 완료 메시지를 보여주도록 연결했다.
- `src/main/resources/static/css/design-tokens.css`
  - `frontend-design` 기준으로 가입 흐름 카드와 안내 레이아웃을 보강했다.
- `src/test/java/com/example/demo/user/UserServiceTest.java`
  - 비밀번호가 평문이 아닌 해시로 저장되는지, 중복 username이면 예외가 발생하는지 검증했다.
- `src/test/java/com/example/demo/user/UserApiControllerTest.java`
  - 가입 성공, 유효성 실패, 중복 username 실패를 검증했다.

## 구현 포인트

### BCrypt 저장

```java
var encodedPassword = bCryptPasswordEncoder.encode(requestDTO.getPassword());
```

사용자가 입력한 비밀번호를 그대로 저장하지 않고, BCrypt 해시로 바꾼 뒤 DB에 저장한다.

### 서버 측 중복 방어

```java
if (!isUsernameAvailable(requestDTO.getUsername())) {
    throw new Exception400("이미 사용 중인 아이디입니다.");
}
```

프론트의 중복 체크를 우회해도 서버 저장 직전에 한 번 더 막아 데이터 정합성을 지킨다.

### 최종 가입 응답

```java
return Resp.ok(Map.of(
        "message", "회원가입이 완료되었습니다.",
        "user", responseDTO));
```

가입 완료 후 화면이 바로 성공 상태를 표현할 수 있도록 간단한 사용자 정보와 메시지를 함께 보낸다.

## 쉽게 설명하면

회원가입은 종이에 이름만 적고 끝내는 게 아니라, 접수창에서
"이 이름 이미 쓰는 사람 있나요?",
"빠진 칸 없나요?",
"비밀번호는 숨겨서 보관하나요?"
를 모두 확인한 뒤 보관함에 넣는 과정과 비슷하다.
이번 단계는 그 마지막 보관 단계까지 완성한 셈이다.

## 기술 용어 풀이

- BCrypt
  - 비밀번호를 바로 읽을 수 없게 해시로 바꾸는 방식이다.
- Salting
  - 같은 비밀번호라도 결과가 똑같이 나오지 않게 섞어주는 개념이다.
- Password Encoder
  - 비밀번호를 안전한 문자열로 변환해 저장할 때 사용하는 도구다.

## 검증 결과

- 실행 명령: `./gradlew test --tests com.example.demo.user.UserApiControllerTest --tests com.example.demo.user.UserServiceTest`
- 실행 명령: `./gradlew test`
- 결과: 모두 통과

## 결과

- `T-2.1`의 Step 1, Step 2, Step 3, Finalize까지 모두 완료했다.
- 회원가입 폼은 최종 입력/검증/암호화/저장 흐름을 갖춘 상태가 되었다.

## 남은 일

- 다음 단계에서는 로그인 기능과 세션/인증 흐름으로 자연스럽게 이어갈 수 있다.
