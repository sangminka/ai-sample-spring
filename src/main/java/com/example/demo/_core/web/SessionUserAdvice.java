package com.example.demo._core.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.user.UserResponse;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice
public class SessionUserAdvice {

    private final HttpSession session;

    @ModelAttribute("loginUser")
    public UserResponse.SessionUser sessionUser() {
        // 모든 SSR 템플릿이 같은 세션 값을 바라보게 만들어 헤더 분기를 공통화한다.
        return (UserResponse.SessionUser) session.getAttribute("sessionUser");
    }
}
