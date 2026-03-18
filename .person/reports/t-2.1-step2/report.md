# T-2.1 Step 2 보고서

## 작업 목적

회원가입 2단계 목표에 맞춰 `UserRequest.JoinDTO`를 추가하고, AOP를 이용해 공통 유효성 검사 로직을 적용했다.
동시에 프로젝트를 Java 21 유지, Spring Boot 4.0.3 기준으로 올려 이후 단계 구현 기반도 최신화했다.

## 전체 흐름

1. `TODO.md`의 `T-2.1 Step 2` 요구사항을 다시 확인했다.
2. 현재 프로젝트 구조를 읽고 `_core/web` 영역이 비어 있어 공통 AOP 검증 기반이 필요함을 확인했다.
3. `build.gradle`을 Spring Boot 4.0.3으로 올렸다.
4. `JoinDTO`, 검증 어노테이션, Aspect, 400 예외 처리기를 추가했다.
5. `UserApiController`에 회원가입 검증 API를 추가했다.
6. `join-form.mustache`를 Step 2 화면으로 확장해 가입 입력과 유효성 검사 요청 흐름을 연결했다.
7. Boot 4.0.3 테스트 구조에 맞게 MVC 테스트 의존성을 보강하고 전체 테스트를 통과시켰다.

## 핵심 변경 사항

- `build.gradle`
  - Spring Boot 버전을 `4.0.3`으로 업데이트했다.
  - Boot 4.0.3에서 MVC 테스트가 별도 starter로 분리되어 `spring-boot-starter-webmvc-test`를 테스트 의존성에 추가했다.
- `src/main/java/com/example/demo/user/UserRequest.java`
  - `JoinDTO`를 추가하고 DTO 내부에서 자체 검증 규칙을 수행하도록 구현했다.
- `src/main/java/com/example/demo/_core/web/annotation/CheckValidation.java`
  - 컨트롤러 메서드에 붙일 공통 검증 트리거 어노테이션을 추가했다.
- `src/main/java/com/example/demo/_core/web/aop/ValidationAspect.java`
  - `SelfValidatable` DTO의 `validate()`를 AOP에서 공통 호출하도록 구현했다.
- `src/main/java/com/example/demo/_core/web/exception/Exception400.java`
  - 유효성 검증 실패용 400 예외를 추가했다.
- `src/main/java/com/example/demo/_core/web/exception/GlobalExceptionHandler.java`
  - `Exception400`을 `Resp.fail(...)` 형태의 응답으로 변환했다.
- `src/main/java/com/example/demo/user/UserApiController.java`
  - `/api/users/join` POST API를 추가하고 `@CheckValidation`을 적용했다.
- `src/main/resources/templates/join-form.mustache`
  - Step 1 화면을 Step 2 수준으로 확장해 비밀번호, 이메일, 주소 입력과 가입 버튼을 추가했다.
- `src/main/resources/static/css/design-tokens.css`
  - Step 2 회원가입 폼 레이아웃과 상태 메시지 스타일을 보강했다.
- `src/test/java/com/example/demo/user/UserApiControllerTest.java`
  - 유효성 검사 성공/실패 케이스를 검증하는 테스트를 추가했다.

## 구현 포인트

### AOP 공통 검증

```java
@Around("@annotation(com.example.demo._core.web.annotation.CheckValidation)")
public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
    for (var arg : joinPoint.getArgs()) {
        if (arg instanceof SelfValidatable validatable) {
            validatable.validate();
        }
    }

    return joinPoint.proceed();
}
```

컨트롤러 메서드 안에 검증 코드를 반복해서 넣지 않고, 검증 가능한 DTO를 AOP에서 공통 처리한다.

### JoinDTO 자체 검증

```java
if (username == null || username.isBlank()) {
    throw new Exception400("아이디는 필수입니다.");
}
if (!USERNAME_PATTERN.matcher(username).matches()) {
    throw new Exception400("아이디는 4자 이상 20자 이하의 영문자와 숫자로 입력해주세요.");
}
```

DTO가 자신의 입력 규칙을 직접 알고 있고, AOP는 이 규칙을 공통 실행하는 구조다.

### 프론트엔드 연결

```javascript
const response = await fetch("/api/users/join", {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
});
```

사용자가 입력한 값을 JoinDTO 형태 JSON으로 보내고, 응답 성공/실패 메시지를 바로 화면에 표시한다.

## 쉽게 설명하면

학교에서 제출물 검사를 담임선생님이 매번 직접 손으로 확인하는 대신,
교문 앞 공통 검사대에서 "이름 썼는지, 번호 맞는지, 빠진 건 없는지"를 먼저 확인하고 교실로 들여보내는 구조와 비슷하다.
`JoinDTO`는 제출물이고, AOP는 교문 앞 검사대 역할을 한다.

## 기술 용어 풀이

- AOP
  - 핵심 로직 바깥에서 공통 기능을 끼워 넣는 방식이다.
- Aspect
  - AOP에서 실제 공통 기능을 실행하는 클래스다.
- DTO
  - 화면과 서버 사이에서 데이터를 주고받을 때 쓰는 전송용 객체다.

## 검증 결과

- 실행 명령: `./gradlew test --tests com.example.demo.user.UserApiControllerTest --tests com.example.demo.user.UserServiceTest`
- 실행 명령: `./gradlew test`
- 결과: 모두 통과

## 추가 확인 사항

- Spring Boot 4.0.3 기준 runtimeClasspath를 확인한 결과, `spring-boot-starter-data-jpa` 아래로 `spring-aop`와 `aspectjweaver`가 들어왔다.
- 따라서 이번 구현에서는 `spring-boot-starter-aop`를 별도로 추가하지 않아도 AOP 구성이 동작했다.
- 반면 MVC 테스트는 Boot 4.0.3에서 별도 테스트 starter가 필요해 `spring-boot-starter-webmvc-test`를 추가했다.

## 남은 일

- Step 3에서 BCrypt 암호화와 실제 회원 저장 로직을 붙여 회원가입 프로세스를 완성해야 한다.
- Finalize 단계에서 `frontend-design` 스킬 기준으로 화면 완성도를 더 높일 수 있다.
