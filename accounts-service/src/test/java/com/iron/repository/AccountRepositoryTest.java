package com.iron.repository;

import com.iron.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AccountRepository Tests")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        testAccount1 = Account.builder()
                .login("testuser1")
                .firstName("Test")
                .lastName("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .balance(BigDecimal.valueOf(1000.50))
                .build();

        testAccount2 = Account.builder()
                .login("testuser2")
                .firstName("Test")
                .lastName("User2")
                .birthday(LocalDate.of(1992, 5, 15))
                .balance(BigDecimal.valueOf(2500.75))
                .build();
    }

    @AfterEach
    void tearDown() {
        try {
            accountRepository.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup errors from failed tests
        }
    }

    @Test
    @DisplayName("Should save account successfully")
    void save_Success() {
        Account savedAccount = accountRepository.save(testAccount1);

        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getLogin()).isEqualTo("testuser1");
        assertThat(savedAccount.getFirstName()).isEqualTo("Test");
        assertThat(savedAccount.getLastName()).isEqualTo("User1");
        assertThat(savedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000.50));
    }

    @Test
    @DisplayName("Should find account by login successfully")
    void findByLogin_Success() {
        entityManager.persistAndFlush(testAccount1);

        Optional<Account> foundAccount = accountRepository.findByLogin("testuser1");

        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getLogin()).isEqualTo("testuser1");
        assertThat(foundAccount.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should return empty when account not found by login")
    void findByLogin_NotFound() {
        Optional<Account> foundAccount = accountRepository.findByLogin("nonexistent");

        assertThat(foundAccount).isEmpty();
    }

    @Test
    @DisplayName("Should find all accounts except specified login")
    void findAllByLoginNot_Success() {
        entityManager.persistAndFlush(testAccount1);
        entityManager.persistAndFlush(testAccount2);

        List<Account> accounts = accountRepository.findAllByLoginNot("testuser1");

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getLogin()).isEqualTo("testuser2");
    }

    @Test
    @DisplayName("Should return empty list when all accounts have specified login")
    void findAllByLoginNot_AllMatch() {
        entityManager.persistAndFlush(testAccount1);

        List<Account> accounts = accountRepository.findAllByLoginNot("testuser1");

        assertThat(accounts).isEmpty();
    }

    @Test
    @DisplayName("Should return all accounts when no account matches specified login")
    void findAllByLoginNot_NoMatch() {
        entityManager.persistAndFlush(testAccount1);
        entityManager.persistAndFlush(testAccount2);

        List<Account> accounts = accountRepository.findAllByLoginNot("nonexistent");

        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getLogin)
                .containsExactlyInAnyOrder("testuser1", "testuser2");
    }

    @Test
    @DisplayName("Should handle duplicate login constraint")
    void save_DuplicateLogin() {
        entityManager.persistAndFlush(testAccount1);

        Account duplicateAccount = Account.builder()
                .login("testuser1")  // Same login
                .firstName("Another")
                .lastName("User")
                .birthday(LocalDate.of(1995, 3, 10))
                .balance(BigDecimal.valueOf(500.00))
                .build();

        // This should throw an exception due to unique constraint on login
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            accountRepository.save(duplicateAccount);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should find account by login with case sensitivity")
    void findByLogin_CaseSensitive() {
        entityManager.persistAndFlush(testAccount1);

        Optional<Account> foundAccount = accountRepository.findByLogin("TESTUSER1");

        assertThat(foundAccount).isEmpty(); // Should be case sensitive
    }

    @Test
    @DisplayName("Should update account successfully")
    void update_Success() {
        Account savedAccount = entityManager.persistAndFlush(testAccount1);

        Account updatedAccount = Account.builder()
                .id(savedAccount.getId())
                .login(savedAccount.getLogin())
                .firstName("Updated")
                .lastName(savedAccount.getLastName())
                .birthday(savedAccount.getBirthday())
                .balance(BigDecimal.valueOf(1500.00))
                .build();

        Account result = accountRepository.save(updatedAccount);

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1500.00));
        assertThat(result.getFirstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Should delete account successfully")
    void delete_Success() {
        Account savedAccount = entityManager.persistAndFlush(testAccount1);

        accountRepository.delete(savedAccount);

        Optional<Account> foundAccount = accountRepository.findByLogin("testuser1");
        assertThat(foundAccount).isEmpty();
    }
}
