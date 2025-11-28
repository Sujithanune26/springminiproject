package com.service;

import com.model.Account;
import com.model.Transaction;

import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.exception.InvalidAmountException;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(AccountRepository accountRepo, TransactionRepository txnRepo) {
        this.accountRepo = accountRepo;
        this.txnRepo = txnRepo;
    }

    private String generateAccNo(String name) {
        String initials = name.substring(0, 3).toUpperCase();
        int random = new Random().nextInt(9000) + 1000;
        log.info("Account number generated: {}", initials + random);
        return initials + random;
    }

    @Override
    public Account createAccount(String name) {
        Account account = new Account();
        account.setHolderName(name);
        account.setAccountNumber(generateAccNo(name));
        log.info("Account created: {}", account);
        return accountRepo.save(account);
    }

    @Override
    public Account getAccount(String accNo) {
        Account acc = accountRepo.findByAccountNumber(accNo);
        log.info("Account found: {}", acc);
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
        log.info("Amount deposited to account {} is Rs {}", acc, amount);

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
        log.info("Amount withdraw from account {} is Rs {}", acc, amount);

        createTxn("WITHDRAW", amount, accNo, null);

        return acc;
    }

    @Override
    public void transfer(String from, String to, double amount) {
        if (amount <= 0) throw new InvalidAmountException("Invalid amount");

        Account source = withdraw(from, amount);
        deposit(to, amount);
        log.info("Amount transferred from {} to {} is Rs {}", from, to, amount);

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
    public void deleteAccount(String accountNumber) {
        Account account = accountRepo.findByAccountNumber(accountNumber);

        if (account == null) {
            log.error("Account does not exist");
            throw new AccountNotFoundException("Account does not exist");
        }
        log.info("Account deleted: {}", account);
        accountRepo.delete(account);
    }

    @Override
    public Account updateHolderName(String accountNumber, String newHolderName) {
        if (newHolderName == null || newHolderName.trim().isEmpty()) {
            log.error("New holder name is empty");
            throw new InvalidAmountException("holderName must not be blank"); // or create a BadRequestException
        }

        Account acc = accountRepo.findByAccountNumber(accountNumber);
        if (acc == null) throw new AccountNotFoundException("Account does not exist");

        acc.setHolderName(newHolderName.trim());
        log.info("Account {} updated with new holder name {}", acc, newHolderName);
        return accountRepo.save(acc);
    }


    @Override
    public List<Account> getAllAccounts() {
        log.info("All accounts found");
        return accountRepo.findAll();
    }
}
