package com.service;

import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.exception.InvalidAmountException;
import com.model.Account;
import com.model.Transaction;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository txnRepo;

    @InjectMocks
    private AccountServiceImpl service;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Captor
    private ArgumentCaptor<Transaction> txnCaptor;

    @BeforeEach
    void setup() {
        // Mockito will inject mocks into service via @InjectMocks
        // but ensure service isn't null
        assertNotNull(service);
    }

    @Test
    void createAccount_shouldSaveAccount_withGeneratedAccNoAndHolderName() {
        // arrange
        String name = "sachin"; // first 3 chars "sac" -> uppercase "SAC"
        // stub save to echo back the account passed in (simulate JPA save)
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        Account saved = service.createAccount(name);

        // assert
        assertNotNull(saved);
        assertEquals(name, saved.getHolderName());
        String accNo = saved.getAccountNumber();
        assertNotNull(accNo, "account number should be generated");
        // generated format: first 3 uppercase letters + 4 digit random number -> length 7
        assertEquals(7, accNo.length(), "expected generated accNo length 7");
        assertEquals(name.substring(0, 3).toUpperCase(), accNo.substring(0, 3),
                "account number should start with first 3 uppercase letters of name");

        verify(accountRepo).save(accountCaptor.capture());
        Account passed = accountCaptor.getValue();
        assertEquals(name, passed.getHolderName());
        assertEquals(accNo, passed.getAccountNumber());
    }

    @Test
    void getAccount_whenExists_returnsAccount() {
        // arrange
        Account a = new Account();
        a.setAccountNumber("ACC1001");
        when(accountRepo.findByAccountNumber("ACC1001")).thenReturn(a);

        // act
        Account result = service.getAccount("ACC1001");

        // assert
        assertSame(a, result);
        verify(accountRepo).findByAccountNumber("ACC1001");
    }

    @Test
    void getAccount_whenMissing_throwsAccountNotFoundException() {
        when(accountRepo.findByAccountNumber("MISSING")).thenReturn(null);

        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class,
                () -> service.getAccount("MISSING"));
        assertTrue(ex.getMessage().toLowerCase().contains("does not exist"));
        verify(accountRepo).findByAccountNumber("MISSING");
    }

    @Test
    void deposit_withInvalidAmount_throwsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> service.deposit("any", 0.0));
        assertThrows(InvalidAmountException.class, () -> service.deposit("any", -10.0));
        verifyNoInteractions(txnRepo);
        verifyNoInteractions(accountRepo);
    }

    @Test
    void deposit_success_updatesBalance_andCreatesTransaction() {
        // arrange
        Account a = new Account();
        a.setAccountNumber("A1");
        a.setHolderName("Alice");
        a.setBalance(100.0);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(a);
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // act
        Account updated = service.deposit("A1", 50.0);

        // assert
        assertEquals(150.0, updated.getBalance());
        verify(accountRepo).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();
        assertEquals(150.0, saved.getBalance());

        verify(txnRepo, times(1)).save(txnCaptor.capture());
        Transaction t = txnCaptor.getValue();
        assertEquals("DEPOSIT", t.getType());
        assertEquals(50.0, t.getAmount());
        assertEquals("A1", t.getSourceAccount());
        assertNull(t.getDestinationAccount());
        assertEquals("SUCCESS", t.getStatus());
    }

    @Test
    void withdraw_withInvalidAmount_throwsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> service.withdraw("any", 0.0));
        assertThrows(InvalidAmountException.class, () -> service.withdraw("any", -5.0));
        verifyNoInteractions(txnRepo);
        verifyNoInteractions(accountRepo);
    }

    @Test
    void withdraw_withInsufficientBalance_throwsInsufficientBalanceException() {
        Account a = new Account();
        a.setAccountNumber("B1");
        a.setBalance(30.0);
        when(accountRepo.findByAccountNumber("B1")).thenReturn(a);

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> service.withdraw("B1", 50.0));
        assertTrue(ex.getMessage().toLowerCase().contains("low balance"));
        // withdraw should call getAccount but should not save when failing
        verify(accountRepo).findByAccountNumber("B1");
        verify(accountRepo, never()).save(any());
        verify(txnRepo, never()).save(any());
    }

    @Test
    void withdraw_success_updatesBalance_andCreatesTransaction() {
        Account a = new Account();
        a.setAccountNumber("B2");
        a.setBalance(200.0);

        when(accountRepo.findByAccountNumber("B2")).thenReturn(a);
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Account after = service.withdraw("B2", 75.5);
        assertEquals(124.5, after.getBalance(), 1e-9);

        verify(accountRepo).save(accountCaptor.capture());
        assertEquals(124.5, accountCaptor.getValue().getBalance(), 1e-9);

        verify(txnRepo, times(1)).save(txnCaptor.capture());
        assertEquals("WITHDRAW", txnCaptor.getValue().getType());
        assertEquals(75.5, txnCaptor.getValue().getAmount());
        assertEquals("B2", txnCaptor.getValue().getSourceAccount());
    }

    @Test
    void transfer_withInvalidAmount_throwsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> service.transfer("x", "y", 0.0));
        assertThrows(InvalidAmountException.class, () -> service.transfer("x", "y", -10.0));
        verifyNoInteractions(accountRepo);
        verifyNoInteractions(txnRepo);
    }

    @Test
    void transfer_success_movesMoney_andCreatesThreeTransactions() {
        // arrange accounts
        Account from = new Account();
        from.setAccountNumber("F1");
        from.setBalance(500.0);

        Account to = new Account();
        to.setAccountNumber("T1");
        to.setBalance(100.0);

        // findByAccountNumber used by getAccount inside withdraw/deposit
        when(accountRepo.findByAccountNumber("F1")).thenReturn(from);
        when(accountRepo.findByAccountNumber("T1")).thenReturn(to);

        // save should echo the account
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // act
        service.transfer("F1", "T1", 200.0);

        // assert balances updated
        assertEquals(300.0, from.getBalance());
        assertEquals(300.0, to.getBalance());

        // verify account saves happened for withdraw and deposit (at least twice)
        verify(accountRepo, atLeast(2)).save(any(Account.class));

        // verify transactions: withdraw, deposit and transfer => 3 saves
        verify(txnRepo, times(3)).save(txnCaptor.capture());
        List<Transaction> savedTxns = txnCaptor.getAllValues();
        // last txn should be TRANSFER (because createTxn for TRANSFER is called last)
        Transaction last = savedTxns.get(savedTxns.size() - 1);
        assertEquals("TRANSFER", last.getType());
        assertEquals(200.0, last.getAmount());
        assertEquals("F1", last.getSourceAccount());
        assertEquals("T1", last.getDestinationAccount());
    }

    @Test
    void getAllAccounts_delegatesToRepository() {
        Account a1 = new Account();
        Account a2 = new Account();
        when(accountRepo.findAll()).thenReturn(List.of(a1, a2));

        List<Account> all = service.getAllAccounts();
        assertEquals(2, all.size());
        verify(accountRepo).findAll();
    }

    @Test
    void deleteAccount_existingAccount_deletesAndDoesNotThrow() {
        // arrange
        String acctNum = "ACC123";
        Account account = new Account();
        account.setAccountNumber(acctNum);
        when(accountRepo.findByAccountNumber(acctNum)).thenReturn(account);

        // act & assert (no exception)
        assertDoesNotThrow(() -> service.deleteAccount(acctNum));

        // verify deletion called
        verify(accountRepo, times(1)).delete(account);
    }

    @Test
    void deleteAccount_nonExistingAccount_throwsAccountNotFoundException() {
        // arrange
        String acctNum = "NOT_FOUND";
        when(accountRepo.findByAccountNumber(acctNum)).thenReturn(null);

        // act & assert
        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class,
                () -> service.deleteAccount(acctNum));

        assertEquals("Account does not exist", ex.getMessage());
        verify(accountRepo, never()).delete(any());
    }

    // --- updateHolderName tests ---

    @Test
    void updateHolderName_withValidName_updatesAndReturnsSavedAccount() {
        // arrange
        String acctNum = "ACC456";
        String newName = "  Alice Smith  ";
        Account existing = new Account();
        existing.setAccountNumber(acctNum);
        existing.setHolderName("Old Name");

        Account saved = new Account();
        saved.setAccountNumber(acctNum);
        saved.setHolderName(newName.trim());

        when(accountRepo.findByAccountNumber(acctNum)).thenReturn(existing);
        when(accountRepo.save(any(Account.class))).thenReturn(saved);

        // act
        Account result = service.updateHolderName(acctNum, newName);

        // assert
        assertNotNull(result);
        assertEquals(newName.trim(), result.getHolderName());

        // capture the saved entity to ensure setHolderName was called with trimmed value
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo).save(captor.capture());
        assertEquals(newName.trim(), captor.getValue().getHolderName());
    }

    @Test
    void updateHolderName_withBlankName_throwsInvalidAmountException() {
        // arrange
        String acctNum = "ACC789";
        String blankName = "   ";

        // act & assert
        InvalidAmountException ex = assertThrows(InvalidAmountException.class,
                () -> service.updateHolderName(acctNum, blankName));

        assertEquals("holderName must not be blank", ex.getMessage());
        verify(accountRepo, never()).findByAccountNumber(anyString());
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateHolderName_nonExistingAccount_throwsAccountNotFoundException() {
        // arrange
        String acctNum = "UNKNOWN";
        String newName = "Bob";
        when(accountRepo.findByAccountNumber(acctNum)).thenReturn(null);

        // act & assert
        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class,
                () -> service.updateHolderName(acctNum, newName));

        assertEquals("Account does not exist", ex.getMessage());
        verify(accountRepo).findByAccountNumber(acctNum);
        verify(accountRepo, never()).save(any());
    }
}
