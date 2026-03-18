package com.example.demo.user;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo._core.utils.Resp;
import com.example.demo._core.web.annotation.CheckValidation;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;
    private final HttpSession session;

    @GetMapping("/api/usernames/check")
    public Object checkUsername(@RequestParam("username") String username) {
        // Step 1 학습 목적에 맞춰 중복 체크 결과를 단순한 JSON 구조로 응답한다.
        var isAvailable = userService.isUsernameAvailable(username);
        var message = isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";

        return Resp.ok(Map.of(
                "username", username,
                "available", isAvailable,
                "message", message));
    }

    @CheckValidation
    @PostMapping("/api/users/join")
    public Object join(@RequestBody UserRequest.JoinDTO requestDTO) {
        // Step 3에서는 AOP 유효성 검사를 통과한 뒤 BCrypt 암호화와 실제 저장까지 수행한다.
        var responseDTO = userService.join(requestDTO);
        return Resp.ok(Map.of(
                "message", "회원가입이 완료되었습니다.",
                "user", responseDTO));
    }

}
