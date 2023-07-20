package com.example.accountdemo.controller;

import com.example.accountdemo.service.TransactionService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(TransactionControllerTest.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;



}