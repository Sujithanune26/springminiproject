package com.controller;

import com.dto.DepositRequest;
import com.dto.TransferRequest;
import com.dto.UpdateAccountRequest;
import com.dto.WithdrawRequest;
import com.model.Account;
import com.service.AccountService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import com.model.Account.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    public AccountController(AccountService service) {
        this.service = service;
    }


    @PostMapping
    public Account create(@Valid @RequestBody Account request) {
        String holderName = request.getHolderName();
        log.info("Creating account with holderName {}", holderName);
        return service.createAccount(holderName);
    }


    @GetMapping("/{accNo}")
    public Account get(@PathVariable String accNo) {
        return service.getAccount(accNo);
    }

    @PutMapping("/{accountNumber}/deposit")
    public Account deposit(
            @PathVariable String accountNumber,
            @RequestBody DepositRequest request) {
        log.info("Deposit request {}", request);
        return service.deposit(accountNumber, request.getAmount());
    }

    @PutMapping("/{accountNumber}/withdraw")
    public Account withdraw(
            @PathVariable String accountNumber,
            @RequestBody WithdrawRequest request) {

        return service.withdraw(accountNumber, request.getAmount());
    }


    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest req) {
        log.info("Transfer request {}", req);
        service.transfer(
                req.getFromAccount(),
                req.getToAccount(),
                req.getAmount()
        );

        return ResponseEntity.ok("Transfer successful");
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<String> deleteAccount(@PathVariable String accountNumber) {
        service.deleteAccount(accountNumber);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<Account> updateHolderName(
            @PathVariable String accountNumber,
            @RequestBody @Valid UpdateAccountRequest req) {

        Account updated = service.updateHolderName(accountNumber, req.getHolderName());
        return ResponseEntity.ok(updated);
    }


    // GET /api/accounts  → List all accounts
    @GetMapping
    public List<Account> getAllAccounts() {
        log.info("Getting all accounts");
        return service.getAllAccounts();
    }
}
