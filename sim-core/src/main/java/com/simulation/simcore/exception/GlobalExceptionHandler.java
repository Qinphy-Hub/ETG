package com.simulation.simcore.exception;

import com.simulation.simcore.entity.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        return Result.failure(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "operation failure!");
    }
}
