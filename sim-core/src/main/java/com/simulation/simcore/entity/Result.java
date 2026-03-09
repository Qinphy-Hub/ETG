package com.simulation.simcore.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <E> Result<E> success(E data) {
        return new Result<>(0, "success", data);
    }

    public static <E> Result<E> failure(String message, E data) {
        return new Result<>(1, message, data);
    }

    public static Result<String> success() {
        return new Result<>(0, "success", null);
    }

    public static Result<String> failure(String message) {
        return new Result<>(1, message, null);
    }
}