package com.example.demo.user;

import java.util.regex.Pattern;

import com.example.demo._core.web.exception.Exception400;
import com.example.demo._core.web.validation.SelfValidatable;

import lombok.Data;

public class UserRequest {

    @Data
    public static class JoinDTO implements SelfValidatable {
        private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,20}$");
        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

        private String username;
        private String password;
        private String email;
        private String postcode;
        private String address;
        private String detailAddress;
        private String extraAddress;

        @Override
        public void validate() {
            // 회원가입 Step 2에서는 DTO가 스스로 규칙을 알고 있고, AOP가 이 검증 메서드를 공통 호출한다.
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
    public static class Login {
        private String username;
        private String password;
    }

}
