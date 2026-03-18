package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo._core.web.exception.Exception400;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean isUsernameAvailable(String username) {
        // 빈 값은 정상적인 아이디 검사가 아니므로 서비스 단계에서 먼저 막는다.
        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepository.findByUsername(username).isEmpty();
    }

    @Transactional
    public UserResponse.Min join(UserRequest.JoinDTO requestDTO) {
        // 프론트 중복 체크를 우회해도 서버 저장 직전에 한 번 더 막아 데이터 정합성을 지킨다.
        if (!isUsernameAvailable(requestDTO.getUsername())) {
            throw new Exception400("이미 사용 중인 아이디입니다.");
        }

        var encodedPassword = bCryptPasswordEncoder.encode(requestDTO.getPassword());
        var user = User.builder()
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .email(requestDTO.getEmail())
                .postcode(requestDTO.getPostcode())
                .address(requestDTO.getAddress())
                .detailAddress(requestDTO.getDetailAddress())
                .extraAddress(requestDTO.getExtraAddress())
                .build();

        var savedUser = userRepository.save(user);
        return new UserResponse.Min(savedUser);
    }

}
