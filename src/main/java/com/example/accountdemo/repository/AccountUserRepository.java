package com.example.accountdemo.repository;

import com.example.accountdemo.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountUserRepository
        extends JpaRepository<AccountUser, Long> {

}
