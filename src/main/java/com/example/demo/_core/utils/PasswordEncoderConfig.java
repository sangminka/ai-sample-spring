package com.example.demo._core.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        // 회원가입 Step 3에서는 BCrypt 인코더를 빈으로 등록해 서비스에서 재사용한다.
        return new BCryptPasswordEncoder();
    }
}
