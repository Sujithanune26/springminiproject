package com.service;

import com.model.Account;

import java.util.List;

public interface AccountService {
    Account createAccount(String name);
    void deleteAccount(String accountNumber);
    Account updateHolderName(String accountNumber, String newHolderName);
    Account getAccount(String accountNumber);
    Account deposit(String accountNumber, double amount);
    Account withdraw(String accountNumber, double amount);
    void transfer(String fromAcc, String toAcc, double amount);
    List<Account> getAllAccounts();
}
