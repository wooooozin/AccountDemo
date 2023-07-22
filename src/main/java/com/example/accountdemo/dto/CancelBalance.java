package com.example.accountdemo.dto;

import com.example.accountdemo.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class CancelBalance {
    /**
     {
     "transactionId":"c2033bb6d82a4250aecf8e27c49b63f6",
     "accountNumber":"1000000000",
     "amount":1000
     }
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull
        private String transactionId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    /**
     {
     "accountNumber":"1000000000",
     "transactionResult":"S",
     "transactionId":"5d011bb6d82cc50aecf8e27cdabb6772",
     "amount":1000,
     "transactedAt":"2022-06-01T23:26:14.671859"
     }
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResult(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }
}
