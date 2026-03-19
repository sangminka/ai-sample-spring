package com.example.demo.user;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.example.demo._core.web.exception.Exception400;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final HttpSession session;

    // 회원가입 페이지 반환환
    @GetMapping("/join-form")
    public String joinForm() {
        return "join-form";
    }

    @GetMapping("/login-form")
    public String loginForm(
            @RequestParam(value = "error", required = false) String errorMessage,
            @RequestParam(value = "message", required = false) String message,
            Model model) {
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("message", message);
        return "login-form";
    }

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

    private UserResponse.SessionUser getSessionUser() {
        return (UserResponse.SessionUser) session.getAttribute("sessionUser");
    }

    private String redirectLoginWithError(String message) {
        return "redirect:/login-form?error=" + encodeMessage(message);
    }

    private String encodeMessage(String message) {
        return UriUtils.encode(message, StandardCharsets.UTF_8);
    }

}
