package com.example.accountdemo.service;

import com.example.accountdemo.domain.Account;
import com.example.accountdemo.domain.AccountUser;
import com.example.accountdemo.dto.AccountDto;
import com.example.accountdemo.exception.AccountException;
import com.example.accountdemo.repository.AccountRepository;
import com.example.accountdemo.repository.AccountUserRepository;
import com.example.accountdemo.type.AccountStatus;
import com.example.accountdemo.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.example.accountdemo.type.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void createFirstRandomAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByAccountUserOrderByIdDesc(any()))
                .willReturn(Optional.empty());
        given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
        given(accountRepository.save(any())).willAnswer(invocation -> {
            Account account = invocation.getArgument(0, Account.class);
            account.setAccountUser(user);
            return account;
        });

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(12L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        Account savedAccount = captor.getValue();
        assertEquals(12L, accountDto.getUserId());
        assertTrue(Pattern.matches("\\d{10}", savedAccount.getAccountNumber()));
        assertEquals(1000L, accountDto.getBalance());
    }

    @Test
    void createSecondAccount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByAccountUserOrderByIdDesc(any()))
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012")
                        .accountUser(user)
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

//    @Test
//    void createAccountSuccess() {
//        //given
//        AccountUser user = AccountUser.builder()
//                .name("Pobi").build();
//        user.setId(12L);
//        given(accountUserRepository.findById(anyLong()))
//                .willReturn(Optional.of(user));
//        given(accountRepository.findFirstByOrderByIdDesc())
//                .willReturn(Optional.of(Account.builder()
//                        .accountNumber("1000000012").build()));
//        given(accountRepository.save(any()))
//                .willReturn(Account.builder()
//                        .accountUser(user)
//                        .accountNumber("1000000015").build());
//
//        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//
//        // when
//        AccountDto accountDto = accountService.createAccount(1L, 1000L);
//
//        // then
//        verify(accountRepository, times(1)).save(captor.capture());
//        assertEquals(12L, accountDto.getUserId());
//        assertEquals("1000000013", captor.getValue().getAccountNumber());
//    }
//
//    @Test
//    void createFirstAccount() { // 계좌가 없을 떄
//        //given
//        AccountUser user = AccountUser.builder()
//                .name("Pobi").build();
//        user.setId(12L);
//        given(accountUserRepository.findById(anyLong()))
//                .willReturn(Optional.of(user));
//        given(accountRepository.findFirstByOrderByIdDesc())
//                .willReturn(Optional.empty());
//        given(accountRepository.save(any()))
//                .willReturn(Account.builder()
//                        .accountUser(user)
//                        .accountNumber("1000000015").build());
//
//        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
//
//        // when
//        AccountDto accountDto = accountService.createAccount(1L, 1000L);
//
//        // then
//        verify(accountRepository, times(1)).save(captor.capture());
//        assertEquals(12L, accountDto.getUserId());
//        assertEquals("1000000000", captor.getValue().getAccountNumber());
//    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {
        //given

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(USER_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_ID, accountException.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Loopy").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000002").build()
                ));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        // when
        AccountDto accountDto = accountService.deleteAccount(
                1L,
                "1234567890"
        );
        // then
        verify(accountRepository, times(1))
                .save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000002", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    // 실패 케이스
    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(
                        1L,
                        "1234567890"
                )
        );
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Loopy").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(
                        1L,
                        "1234567890"
                )
        );

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_userUnMatch() {
        //given
        AccountUser loopy = AccountUser.builder()
                .name("Loopy").build();
        loopy.setId(12L);
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(13L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(loopy));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(0L)
                        .accountNumber("1000000002").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(
                        1L,
                        "1234567890"
                )
        );

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        AccountUser loopy = AccountUser.builder()
                .name("Loopy").build();
        loopy.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(loopy));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(loopy)
                        .balance(100L)
                        .accountNumber("1000000002").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(
                        1L,
                        "1234567890"
                )
        );

        // then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 계좌인 경우 해지할 수 없다")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        AccountUser loopy = AccountUser.builder()
                .name("Loopy").build();
        loopy.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(loopy));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(loopy)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(100L)
                        .accountNumber("1000000002").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(
                        1L,
                        "1234567890"
                )
        );

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    void successGetAccountsByUserId() {
        //given
        AccountUser loopy = AccountUser.builder()
                .name("Loopy").build();
        loopy.setId(12L);

        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(loopy)
                        .accountNumber("1111111111")
                        .balance(10000L)
                        .build(),
                Account.builder()
                        .accountUser(loopy)
                        .accountNumber("2222222222")
                        .balance(20000L)
                        .build(),
                Account.builder()
                        .accountUser(loopy)
                        .accountNumber("3333333333")
                        .balance(30000L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(loopy));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        // when
        List<AccountDto> accountDtoList = accountService.getAccountByUserId(1L);

        // then
        assertEquals(3, accountDtoList.size());
        assertEquals("1111111111", accountDtoList.get(0).getAccountNumber());
        assertEquals(10000, accountDtoList.get(0).getBalance());
        assertEquals("2222222222", accountDtoList.get(1).getAccountNumber());
        assertEquals(20000, accountDtoList.get(1).getBalance());
        assertEquals("3333333333", accountDtoList.get(2).getAccountNumber());
        assertEquals(30000, accountDtoList.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }
}