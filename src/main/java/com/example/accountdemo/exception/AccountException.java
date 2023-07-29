package com.example.accountdemo.exception;

import com.example.accountdemo.type.ErrorCode;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder

public class AccountException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }

}
