package com.service;

import com.model.Account;
import com.model.Transaction;

import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.exception.InvalidAmountException;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;

    public AccountServiceImpl(AccountRepository accountRepo, TransactionRepository txnRepo) {
        this.accountRepo = accountRepo;
        this.txnRepo = txnRepo;
    }

    private String generateAccNo(String name) {
        String initials = name.substring(0, 3).toUpperCase();
        int random = new Random().nextInt(9000) + 1000;
        return initials + random;
    }

    @Override
    public Account createAccount(String name) {
        Account account = new Account();
        account.setHolderName(name);
        account.setAccountNumber(generateAccNo(name));
        return accountRepo.save(account);
    }

    @Override
    public Account getAccount(String accNo) {
        Account acc = accountRepo.findByAccountNumber(accNo);
        if (acc == null)
            throw new AccountNotFoundException("Account does not exist");
        return acc;
    }

    @Override
    public Account deposit(String accNo, double amount) {
        if (amount <= 0) throw new InvalidAmountException("Amount must be positive");

        Account acc = getAccount(accNo);
        acc.setBalance(acc.getBalance() + amount);
        accountRepo.save(acc);

        createTxn("DEPOSIT", amount, accNo, null);

        return acc;
    }

    @Override
    public Account withdraw(String accNo, double amount) {
        if (amount <= 0) throw new InvalidAmountException("Amount must be positive");

        Account acc = getAccount(accNo);
        if (acc.getBalance() < amount)
            throw new InsufficientBalanceException("Low balance!");

        acc.setBalance(acc.getBalance() - amount);
        accountRepo.save(acc);

        createTxn("WITHDRAW", amount, accNo, null);

        return acc;
    }

    @Override
    public void transfer(String from, String to, double amount) {
        if (amount <= 0) throw new InvalidAmountException("Invalid amount");

        Account source = withdraw(from, amount);
        deposit(to, amount);

        createTxn("TRANSFER", amount, from, to);
    }

    private void createTxn(String type, double amount, String src, String dest) {
        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + System.currentTimeMillis());
        t.setType(type);
        t.setAmount(amount);
        t.setStatus("SUCCESS");
        t.setSourceAccount(src);
        t.setDestinationAccount(dest);
        txnRepo.save(t);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }
}
