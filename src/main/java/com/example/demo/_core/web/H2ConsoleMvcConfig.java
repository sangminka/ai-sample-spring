package com.example.demo._core.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class H2ConsoleMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 사용자가 /h2-console로 접근해도 실제 콘솔 서블릿 경로(/h2-console/)로 자연스럽게 연결한다.
        registry.addRedirectViewController("/h2-console", "/h2-console/");
    }
}
