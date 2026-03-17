package com.example.demo.user;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo._core.utils.Resp;

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

}
