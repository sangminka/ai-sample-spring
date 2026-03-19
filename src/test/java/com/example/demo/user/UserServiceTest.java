package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.board.Board;
import com.example.demo.board.BoardRepository;
import com.example.demo._core.web.exception.Exception400;
import com.example.demo.reply.ReplyRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ReplyRepository replyRepository;

    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private UserRequest.JoinDTO joinDTO;
    private UserRequest.LoginDTO loginDTO;
    private UserRequest.UpdateDTO updateDTO;
    private UserRequest.DeleteDTO deleteDTO;

    @BeforeEach
    void setUp() {
        joinDTO = new UserRequest.JoinDTO();
        joinDTO.setUsername("newuser");
        joinDTO.setPassword("1234");
        joinDTO.setEmail("newuser@example.com");
        joinDTO.setPostcode("06236");
        joinDTO.setAddress("서울시 강남구 테헤란로");
        joinDTO.setDetailAddress("101호");
        joinDTO.setExtraAddress("테헤란로");

        loginDTO = new UserRequest.LoginDTO();
        loginDTO.setUsername("newuser");
        loginDTO.setPassword("1234");

        updateDTO = new UserRequest.UpdateDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPostcode("04524");
        updateDTO.setAddress("서울시 중구 세종대로");
        updateDTO.setDetailAddress("202호");
        updateDTO.setExtraAddress("서울시청");

        deleteDTO = new UserRequest.DeleteDTO();
        deleteDTO.setPassword("1234");
    }

    @DisplayName("중복되지 않은 username이면 사용 가능하다")
    @Test
    void isUsernameAvailable_returnsTrue() {
        var username = "ssar";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        var result = userService.isUsernameAvailable(username);

        assertThat(result).isTrue();
    }

    @DisplayName("이미 존재하는 username이면 사용 불가능하다")
    @Test
    void isUsernameAvailable_returnsFalse() {
        var username = "cos";
        var user = User.builder()
                .id(1)
                .username(username)
                .password("1234")
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var result = userService.isUsernameAvailable(username);

        assertThat(result).isFalse();
    }

    @DisplayName("회원가입 시 비밀번호는 BCrypt로 암호화되어 저장된다")
    @Test
    void join_encryptsPasswordAndSavesUser() {
        when(userRepository.findByUsername(joinDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            var savedUser = invocation.getArgument(0, User.class);
            savedUser.setId(3);
            return savedUser;
        });

        var response = userService.join(joinDTO);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        var savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isNotEqualTo("1234");
        assertThat(bCryptPasswordEncoder.matches("1234", savedUser.getPassword())).isTrue();
        assertThat(response.getUsername()).isEqualTo("newuser");
    }

    @DisplayName("회원가입 시 이미 존재하는 username이면 예외가 발생한다")
    @Test
    void join_throwsExceptionWhenUsernameAlreadyExists() {
        var existingUser = User.builder()
                .id(1)
                .username("newuser")
                .password("encoded")
                .build();
        when(userRepository.findByUsername(joinDTO.getUsername())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.join(joinDTO))
                .isInstanceOf(Exception400.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @DisplayName("로그인 시 아이디와 비밀번호가 맞으면 세션용 사용자 정보를 반환한다")
    @Test
    void login_returnsSessionUser() {
        var user = User.builder()
                .id(7)
                .username("newuser")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("newuser@example.com")
                .build();
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));

        var sessionUser = userService.login(loginDTO);

        assertThat(sessionUser.getId()).isEqualTo(7);
        assertThat(sessionUser.getUsername()).isEqualTo("newuser");
        assertThat(sessionUser.getEmail()).isEqualTo("newuser@example.com");
    }

    @DisplayName("로그인 시 아이디가 없으면 예외가 발생한다")
    @Test
    void login_throwsExceptionWhenUsernameNotFound() {
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(Exception400.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("로그인 시 비밀번호가 다르면 예외가 발생한다")
    @Test
    void login_throwsExceptionWhenPasswordDoesNotMatch() {
        var user = User.builder()
                .id(7)
                .username("newuser")
                .password(bCryptPasswordEncoder.encode("9999"))
                .email("newuser@example.com")
                .build();
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(Exception400.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("마이페이지 조회 시 사용자 상세 정보를 반환한다")
    @Test
    void getDetail_returnsUserDetail() {
        var user = User.builder()
                .id(9)
                .username("newuser")
                .email("newuser@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build();
        when(userRepository.findById(9)).thenReturn(Optional.of(user));

        var detail = userService.getDetail(9);

        assertThat(detail.getId()).isEqualTo(9);
        assertThat(detail.getUsername()).isEqualTo("newuser");
        assertThat(detail.getPostcode()).isEqualTo("06236");
    }

    @DisplayName("회원정보 수정 시 주소와 이메일이 갱신되고 세션용 DTO를 반환한다")
    @Test
    void update_returnsUpdatedSessionUser() {
        var user = User.builder()
                .id(9)
                .username("newuser")
                .password("encoded")
                .email("newuser@example.com")
                .postcode("06236")
                .address("서울시 강남구 테헤란로")
                .detailAddress("101호")
                .extraAddress("테헤란로")
                .build();
        when(userRepository.findById(9)).thenReturn(Optional.of(user));

        var updatedUser = userService.update(9, updateDTO);

        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getPostcode()).isEqualTo("04524");
        assertThat(user.getAddress()).isEqualTo("서울시 중구 세종대로");
        assertThat(updatedUser.getUsername()).isEqualTo("newuser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @DisplayName("회원 탈퇴 시 사용자 댓글, 게시글의 댓글, 게시글, 사용자 순으로 삭제한다")
    @Test
    void delete_removesUserAndRelatedContents() {
        var user = User.builder()
                .id(9)
                .username("newuser")
                .password(bCryptPasswordEncoder.encode("1234"))
                .email("newuser@example.com")
                .build();
        var board = Board.builder()
                .id(11)
                .title("제목")
                .content("내용")
                .user(user)
                .build();
        when(userRepository.findById(9)).thenReturn(Optional.of(user));
        when(boardRepository.findAllByUserId(9)).thenReturn(List.of(board));

        userService.delete(9, deleteDTO);

        verify(replyRepository).deleteByUserId(9);
        verify(replyRepository).deleteByBoardIn(List.of(board));
        verify(boardRepository).deleteByUserId(9);
        verify(userRepository).delete(user);
    }

    @DisplayName("회원 탈퇴 시 비밀번호가 다르면 삭제하지 않는다")
    @Test
    void delete_throwsExceptionWhenPasswordDoesNotMatch() {
        var user = User.builder()
                .id(9)
                .username("newuser")
                .password(bCryptPasswordEncoder.encode("9999"))
                .email("newuser@example.com")
                .build();
        when(userRepository.findById(9)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.delete(9, deleteDTO))
                .isInstanceOf(Exception400.class)
                .hasMessage("현재 비밀번호가 올바르지 않습니다.");

        verify(replyRepository, never()).deleteByUserId(9);
        verify(boardRepository, never()).findAllByUserId(9);
        verify(userRepository, never()).delete(user);
    }
}
