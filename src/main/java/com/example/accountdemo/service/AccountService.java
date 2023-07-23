package com.example.accountdemo.service;

import com.example.accountdemo.dto.AccountDto;
import com.example.accountdemo.domain.Account;
import com.example.accountdemo.domain.AccountUser;
import com.example.accountdemo.exception.AccountException;
import com.example.accountdemo.repository.AccountRepository;
import com.example.accountdemo.repository.AccountUserRepository;
import com.example.accountdemo.type.AccountStatus;
import com.example.accountdemo.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static com.example.accountdemo.type.ErrorCode.*;

@Service
@RequiredArgsConstructor // 꼭 필요한 아규먼트가 들어간 생성자를 만들어준다.->final 등
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회하고
     * 계좌의 번호를 생성하고
     * 계좌를 저장하고 , 그 정보를 넘긴다.
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = getAccountUser(userId);

        String newAccountNumber = generateNewAccountNumber(userId);

        validateCreatedAccount(accountUser);

        return AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }
    /**
     * 1. 첫번째 숫자는 0이 되지 않도록 한다. (1 ~ 9)
     * 2. 계정 별로 가장 최근에 생성된 계좌를 확인하고 없다면 랜덤으로 생성된 첫자리 숫자 + 나머지 자릿수 랜덤 숫자
     * 3. 가장 최근 계좌 또는 새로운 계좌에 + 1
     * 4. 계좌가 사용중이라면 새로운 랜덤 계좌번호 생성
     */
    private String generateNewAccountNumber(Long userId) {
        AccountUser accountUser = getAccountUser(userId);
        String newAccountNumber;
        Random random = new Random();
        int digit = random.nextInt(9) + 1; // 첫 자리 숫자 범위 1~9, 0이 되지 않게 함
        do {
            String accountNumber = accountRepository.findFirstByAccountUserOrderByIdDesc(accountUser)
                    .map(Account::getAccountNumber)
                    .orElse(digit + String.format("%09d", random.nextInt(1000000000)));

            long accountNumberLong = Long.parseLong(accountNumber) + 1;
            newAccountNumber = Long.toString(accountNumberLong);
            // 전체계좌 조회해서 중복확인
        } while (accountRepository.findByAccountNumber(newAccountNumber).isPresent());
        return newAccountNumber;
    }

    private void validateCreatedAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_ID);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) throws AccountException {
        if (!Objects.equals(account.getAccountUser().getId(), accountUser.getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }

    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        return accountUser;
    }
}

