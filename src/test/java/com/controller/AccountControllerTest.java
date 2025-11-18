package com.controller;

import com.dto.DepositRequest;
import com.dto.TransferRequest;
import com.dto.WithdrawRequest;
import com.model.Account;
import com.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountController using Mockito.
 * These tests call the controller methods directly and mock AccountService and DTOs.
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService service;

    @InjectMocks
    private AccountController controller;

    @BeforeEach
    void setUp() {
        // @InjectMocks will construct controller with mocked service,
        // but keep this to show intent and allow replacement if needed.
        controller = new AccountController(service);
    }

    @Test
    void create_shouldCallServiceAndReturnAccount() {
        // arrange
        Account req = new Account();
        req.setHolderName("Alice");

        Account expected = mock(Account.class);
        when(service.createAccount("Alice")).thenReturn(expected);

        // act
        Account actual = controller.create(req);

        // assert
        assertSame(expected, actual);
        verify(service, times(1)).createAccount("Alice");
    }

    @Test
    void get_shouldReturnAccountFromService() {
        // arrange
        String accNo = "12345";
        Account expected = mock(Account.class);
        when(service.getAccount(accNo)).thenReturn(expected);

        // act
        Account actual = controller.get(accNo);

        // assert
        assertSame(expected, actual);
        verify(service).getAccount(accNo);
    }

    @Test
    void deposit_shouldCallServiceWithAmountAndReturnUpdatedAccount() {
        // arrange
        String accountNumber = "acc-1";
        DepositRequest depositReq = mock(DepositRequest.class);
        when(depositReq.getAmount()).thenReturn(150.0);
        Account updated = mock(Account.class);
        when(service.deposit(accountNumber, 150.0)).thenReturn(updated);

        // act
        Account result = controller.deposit(accountNumber, depositReq);

        // assert
        assertSame(updated, result);
        verify(depositReq).getAmount();
        verify(service).deposit(accountNumber, 150.0);
    }

    @Test
    void withdraw_shouldCallServiceWithAmountAndReturnUpdatedAccount() {
        // arrange
        String accountNumber = "acc-2";
        WithdrawRequest withdrawReq = mock(WithdrawRequest.class);
        when(withdrawReq.getAmount()).thenReturn(75.5);
        Account updated = mock(Account.class);
        when(service.withdraw(accountNumber, 75.5)).thenReturn(updated);

        // act
        Account result = controller.withdraw(accountNumber, withdrawReq);

        // assert
        assertSame(updated, result);
        verify(withdrawReq).getAmount();
        verify(service).withdraw(accountNumber, 75.5);
    }

    @Test
    void transfer_shouldCallServiceAndReturnOkResponse() {
        // arrange
        TransferRequest transferReq = mock(TransferRequest.class);
        when(transferReq.getFromAccount()).thenReturn("from-acc");
        when(transferReq.getToAccount()).thenReturn("to-acc");
        when(transferReq.getAmount()).thenReturn(200.0);

        // service.transfer is void — use doNothing (default) and verify afterwards
        doNothing().when(service).transfer("from-acc", "to-acc", 200.0);

        // act
        ResponseEntity<String> response = controller.transfer(transferReq);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Transfer successful", response.getBody());
        verify(service).transfer("from-acc", "to-acc", 200.0);
    }

    @Test
    void getAllAccounts_shouldReturnListFromService() {
        // arrange
        Account a1 = mock(Account.class);
        Account a2 = mock(Account.class);
        List<Account> expected = List.of(a1, a2);
        when(service.getAllAccounts()).thenReturn(expected);

        // act
        List<Account> actual = controller.getAllAccounts();

        // assert
        assertSame(expected, actual);
        verify(service).getAllAccounts();
    }

    // Optional: test that create handles missing holderName gracefully (if controller doesn't guard it).
    @Test
    void create_withMissingHolderName_callsServiceWithNull() {
        Account req = new Account(); // holderName is null by default
        Account expected = mock(Account.class);
        when(service.createAccount(null)).thenReturn(expected);

        Account actual = controller.create(req);

        assertSame(expected, actual);
        verify(service).createAccount(null);
    }
}
