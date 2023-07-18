package com.example.accountdemo.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 클라이언트와 컨트롤러간의 연결
public class AccountInfo {
    private String accountNumber;
    private Long balance;
}
