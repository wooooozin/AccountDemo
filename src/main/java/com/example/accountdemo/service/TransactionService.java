package com.example.accountdemo.service;

import com.example.accountdemo.domain.Account;
import com.example.accountdemo.domain.AccountUser;
import com.example.accountdemo.domain.Transaction;
import com.example.accountdemo.dto.TransactionDto;
import com.example.accountdemo.exception.AccountException;
import com.example.accountdemo.repository.AccountRepository;
import com.example.accountdemo.repository.AccountUserRepository;
import com.example.accountdemo.repository.TransactionRepository;
import com.example.accountdemo.type.AccountStatus;
import com.example.accountdemo.type.TransactionResultType;
import com.example.accountdemo.type.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.accountdemo.type.ErrorCode.*;
import static com.example.accountdemo.type.TransactionResultType.F;
import static com.example.accountdemo.type.TransactionResultType.S;
import static com.example.accountdemo.type.TransactionType.CANCEL;
import static com.example.accountdemo.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /**
     * 사용자가 없는 경우, 계좌가 없는 경우
     * 사용자 아이디와 계좌 소유주가 다른 경우,
     * 계좌가 이미 해지 상태인 경우, 거래금액이 잔액보다 큰 경우,
     * 거래 금액이 너무 작거나 큰 경우 실패 응답
     */
    @Transactional
    public TransactionDto useBalance(
            Long userId,
            String accountNumber,
            Long amount
    ) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);
        account.useBalance(amount);


        return TransactionDto.fromEntity(
                saveAtndGetTransaction(USE, S, amount, account)
        );
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAtndGetTransaction(USE, F, amount, account);
    }

    private Transaction saveAtndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType,
            Long amount,
            Account account) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount
    ) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAtndGetTransaction(CANCEL, S, amount, account)
        );
    }

    /**
     * 거래 아이디에 해당하는 거래가 없는 경우,
     * 거래금액과 거래 취소 금액이 다른 경우(부분취소 불가) 실패 응답
     * 1년이 넘은 거래는 사용 취소 불가능
     * 해당 계좌에서 거래 (사용, 사용 취소) 가 진행 중일 때 - 미구현
     * 다른 거래 요청이 오는 경우 해당 거래가 동시에 잘못 처리되는 것을 방지해야 한다. - 미구현
     */
    private void validateCancelBalance(
            Transaction transaction,
            Account account,
            Long amount
    ) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if(!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(CANCEL_MUST_FULLY);
        }
        if(transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new AccountException(TOO_OLD_ORDER_TO_CANCEL);
        }
    }

    @Transactional
    public void saveFailedCancelTransaction(
            String accountNumber,
            Long amount
    ) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAtndGetTransaction(CANCEL, F, amount, account);
    }

    public TransactionDto queryTransaction(String transactionId) {
        return TransactionDto.fromEntity(
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND))
        );
    }
}
