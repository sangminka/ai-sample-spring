package com.example.demo.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class UserApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("회원가입 DTO가 유효하면 AOP 검증을 통과한다")
    @Test
    void join_success() throws Exception {
        var requestBody = """
                {
                  "username": "cos1234",
                  "password": "1234",
                  "email": "cos@example.com",
                  "postcode": "06236",
                  "address": "서울시 강남구 테헤란로",
                  "detailAddress": "101호",
                  "extraAddress": "테헤란로"
                }
                """;

        mvc.perform(post("/api/users/join")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.body.user.username").value("cos1234"))
                .andExpect(jsonPath("$.body.user.email").value("cos@example.com"));
    }

    @DisplayName("회원가입 DTO가 유효하지 않으면 AOP 검증에서 400을 응답한다")
    @Test
    void join_fail() throws Exception {
        var requestBody = """
                {
                  "username": "co",
                  "password": "12",
                  "email": "",
                  "postcode": "",
                  "address": ""
                }
                """;

        mvc.perform(post("/api/users/join")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("아이디는 4자 이상 20자 이하의 영문자와 숫자로 입력해주세요."));
    }

    @DisplayName("이미 가입된 username이면 저장 전에 400을 응답한다")
    @Test
    void join_duplicateUsername() throws Exception {
        userRepository.save(User.builder()
                .username("duplicateuser")
                .password("encoded-password")
                .email("duplicate@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());

        var requestBody = """
                {
                  "username": "duplicateuser",
                  "password": "1234",
                  "email": "cos2@example.com",
                  "postcode": "06236",
                  "address": "서울시 강남구 테헤란로",
                  "detailAddress": "101호",
                  "extraAddress": "테헤란로"
                }
                """;

        mvc.perform(post("/api/users/join")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("이미 사용 중인 아이디입니다."));
    }
}
