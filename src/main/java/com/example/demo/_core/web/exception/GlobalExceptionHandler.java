package com.example.demo._core.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo._core.utils.Resp;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception400.class)
    public Object handleBadRequest(Exception400 exception) {
        return Resp.fail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
