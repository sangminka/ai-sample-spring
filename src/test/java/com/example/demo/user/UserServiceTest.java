package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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
}
