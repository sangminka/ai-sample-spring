package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.example.demo._core.web.exception.Exception400;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private UserRequest.JoinDTO joinDTO;

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
}
