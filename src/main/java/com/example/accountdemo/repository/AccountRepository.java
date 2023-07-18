package com.example.accountdemo.repository;

import com.example.accountdemo.domain.Account;
import com.example.accountdemo.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 제일 마지막에 생성된 계좌, 값이 없을 수도 있기 때문에 옵셔널로
    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String AccountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
