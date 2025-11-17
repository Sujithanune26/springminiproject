package com.controller;

import com.dto.DepositRequest;
import com.dto.TransferRequest;
import com.dto.WithdrawRequest;
import com.model.Account;
import com.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

//    @PostMapping
//    public ResponseEntity<?> create(@RequestParam String holderName) {
//        return ResponseEntity.status(201).body(service.createAccount(holderName));
//    }
    @PostMapping
    public Account create(@RequestBody Map<String, String> request) {
        String holderName = request.get("holderName");
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

        service.transfer(
                req.getFromAccount(),
                req.getToAccount(),
                req.getAmount()
        );

        return ResponseEntity.ok("Transfer successful");
    }


    // GET /api/accounts  → List all accounts
    @GetMapping
    public List<Account> getAllAccounts() {
        return service.getAllAccounts();
    }
}
