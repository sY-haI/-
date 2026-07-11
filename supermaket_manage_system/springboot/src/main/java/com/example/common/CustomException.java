package com.example.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {

    /**
     * 构造方法
     * @param message 异常信息
     */
    public CustomException(String message) {
        super(message);
    }

    /**
     * 构造方法
     * @param message 异常信息
     * @param cause 异常原因
     */
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
