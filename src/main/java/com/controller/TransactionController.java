package com.controller;

import com.repository.TransactionRepository;
import com.model.Transaction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class TransactionController {

    private final TransactionRepository repo;

    public TransactionController(TransactionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{accNo}/transactions")
    public List<Transaction> getTxns(@PathVariable String accNo) {
        return repo.findBySourceAccountOrDestinationAccount(accNo, accNo);
    }
}
