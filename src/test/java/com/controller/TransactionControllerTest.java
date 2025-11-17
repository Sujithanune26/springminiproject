package com.controller;

import com.model.Transaction;
import com.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionRepository repo;

    @InjectMocks
    private TransactionController controller;

    @BeforeEach
    void init() {
        controller = new TransactionController(repo);
    }

    @Test
    void getTxns_shouldReturnTransactionsFromRepository() {
        // arrange
        String accNo = "ACC123";
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        List<Transaction> expected = List.of(t1, t2);

        when(repo.findBySourceAccountOrDestinationAccount(accNo, accNo))
                .thenReturn(expected);

        // act
        List<Transaction> actual = controller.getTxns(accNo);

        // assert
        assertSame(expected, actual);
        verify(repo, times(1)).findBySourceAccountOrDestinationAccount(accNo, accNo);
    }
}
