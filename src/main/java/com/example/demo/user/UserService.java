package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public boolean isUsernameAvailable(String username) {
        // 빈 값은 정상적인 아이디 검사가 아니므로 서비스 단계에서 먼저 막는다.
        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepository.findByUsername(username).isEmpty();
    }

}
