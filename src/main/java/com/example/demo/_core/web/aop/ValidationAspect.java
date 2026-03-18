package com.example.demo._core.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.demo._core.web.validation.SelfValidatable;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
@Component
public class ValidationAspect {

    @Around("@annotation(com.example.demo._core.web.annotation.CheckValidation)")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
        // 컨트롤러마다 중복 검증 코드를 쓰지 않도록, 검증 가능한 DTO를 AOP에서 공통 처리한다.
        for (var arg : joinPoint.getArgs()) {
            if (arg instanceof SelfValidatable validatable) {
                validatable.validate();
            }
        }

        return joinPoint.proceed();
    }
}
