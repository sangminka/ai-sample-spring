package com.example.demo.user;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.board.Board;
import com.example.demo.board.BoardRepository;
import com.example.demo.reply.Reply;
import com.example.demo.reply.ReplyRepository;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @DisplayName("로그인 성공 시 세션에 사용자 정보가 저장되고 홈으로 이동한다")
    @Test
    void login_success() throws Exception {
        userRepository.save(User.builder()
                .username("loginuser1")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("login1@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());

        mvc.perform(post("/login")
                        .contentType("application/x-www-form-urlencoded")
                        .param("username", "loginuser1")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("sessionUser", hasProperty("username", is("loginuser1"))));
    }

    @DisplayName("로그인 실패 시 로그인 폼으로 되돌아간다")
    @Test
    void login_fail() throws Exception {
        userRepository.save(User.builder()
                .username("loginuser2")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("login2@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());

        mvc.perform(post("/login")
                        .contentType("application/x-www-form-urlencoded")
                        .param("username", "loginuser2")
                        .param("password", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login-form?error=*"));
    }

    @DisplayName("로그아웃 시 기존 세션을 무효화한다")
    @Test
    void logout_success() throws Exception {
        var session = new MockHttpSession();
        var loginUser = User.builder()
                .id(1)
                .username("ssar1234")
                .password("encoded-password")
                .email("ssar@example.com")
                .build();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(loginUser));

        mvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist("sessionUser"));
    }

    @DisplayName("비로그인 상태 홈 화면은 회원가입과 로그인 메뉴를 노출한다")
    @Test
    void home_guestHeader() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원가입")))
                .andExpect(content().string(not(containsString("로그아웃"))));
    }

    @DisplayName("로그인 상태 홈 화면은 환영 문구와 로그아웃 메뉴를 노출한다")
    @Test
    void home_loginHeader() throws Exception {
        var session = new MockHttpSession();
        var loginUser = User.builder()
                .id(1)
                .username("ssar1234")
                .password("encoded-password")
                .email("ssar@example.com")
                .build();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(loginUser));

        mvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ssar1234님 환영합니다")))
                .andExpect(content().string(containsString("마이페이지")))
                .andExpect(content().string(containsString("로그아웃")))
                .andExpect(content().string(not(containsString("회원가입"))));
    }

    @DisplayName("비로그인 상태에서 마이페이지 접근 시 로그인 폼으로 리다이렉트한다")
    @Test
    void updateForm_redirectsWhenNotLoggedIn() throws Exception {
        mvc.perform(get("/user/update-form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login-form?error=*"));
    }

    @DisplayName("로그인 상태에서 마이페이지 화면을 볼 수 있다")
    @Test
    void updateForm_success() throws Exception {
        var savedUser = userRepository.save(User.builder()
                .username("mypageuser1")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("mypage1@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());
        var session = new MockHttpSession();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(savedUser));

        mvc.perform(get("/user/update-form").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원정보 수정")))
                .andExpect(content().string(containsString("mypage1@example.com")));
    }

    @DisplayName("회원정보 수정 시 세션과 화면 값이 함께 갱신된다")
    @Test
    void update_success() throws Exception {
        var savedUser = userRepository.save(User.builder()
                .username("mypageuser2")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("before@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());
        var session = new MockHttpSession();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(savedUser));

        mvc.perform(post("/user/update")
                        .session(session)
                        .contentType("application/x-www-form-urlencoded")
                        .param("email", "after@example.com")
                        .param("postcode", "04524")
                        .param("address", "서울시 중구 세종대로")
                        .param("detailAddress", "202호")
                        .param("extraAddress", "서울시청"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/user/update-form?success=*"))
                .andExpect(request().sessionAttribute("sessionUser", hasProperty("email", is("after@example.com"))));
    }

    @DisplayName("회원 탈퇴 시 작성한 게시글과 댓글이 함께 정리되고 로그인 화면으로 이동한다")
    @Test
    void delete_success() throws Exception {
        var targetUser = userRepository.save(User.builder()
                .username("deleteuser1")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("delete1@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());
        var anotherUser = userRepository.save(User.builder()
                .username("deleteuser2")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("delete2@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());
        var board = boardRepository.save(Board.builder()
                .title("탈퇴 대상 게시글")
                .content("내용")
                .user(targetUser)
                .build());
        replyRepository.save(Reply.builder()
                .comment("탈퇴 사용자의 댓글")
                .user(targetUser)
                .board(board)
                .build());
        replyRepository.save(Reply.builder()
                .comment("다른 사용자의 댓글")
                .user(anotherUser)
                .board(board)
                .build());

        var session = new MockHttpSession();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(targetUser));

        mvc.perform(post("/user/delete")
                        .session(session)
                        .contentType("application/x-www-form-urlencoded")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login-form?message=*"))
                .andExpect(request().sessionAttributeDoesNotExist("sessionUser"));

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(targetUser.getId())).isEmpty();
        org.assertj.core.api.Assertions.assertThat(
                boardRepository.findAll().stream().noneMatch(savedBoard -> savedBoard.getId().equals(board.getId())))
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(
                replyRepository.findAll().stream().noneMatch(savedReply -> savedReply.getBoard().getId().equals(board.getId())))
                .isTrue();
    }

    @DisplayName("회원 탈퇴 시 비밀번호가 틀리면 마이페이지로 되돌아간다")
    @Test
    void delete_fail() throws Exception {
        var targetUser = userRepository.save(User.builder()
                .username("deleteuser3")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("delete3@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build());
        var session = new MockHttpSession();
        session.setAttribute("sessionUser", new UserResponse.SessionUser(targetUser));

        mvc.perform(post("/user/delete")
                        .session(session)
                        .contentType("application/x-www-form-urlencoded")
                        .param("password", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/user/update-form?error=*"));

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(targetUser.getId())).isPresent();
    }
}
