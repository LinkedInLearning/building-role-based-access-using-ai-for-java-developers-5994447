package com.ll.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
public class PersonalAccountServiceTest {

  @Autowired
  private PersonalAccountService personalAccountService;

  @Autowired
  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
  }

  @Test
  void testCreatePersonalAccount() {
    // Test successful creation
    PersonalAccount account = personalAccountService.createPersonalAccount("john.doe@example.com", "password123");

    assertNotNull(account);
    assertNotNull(account.getId());
    assertEquals("john.doe@example.com", account.getEmail());

    // Test duplicate email
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      personalAccountService.createPersonalAccount("john.doe@example.com", "anotherPassword");
    });

    assertEquals("Account with email john.doe@example.com already exists", exception.getMessage());
  }

  @Test
  void testGetPersonalAccount() {
    // Create test account
    PersonalAccount account = personalAccountService.createPersonalAccount("test@example.com", "password123");

    // Test successful retrieval
    PersonalAccount retrieved = personalAccountService.getPersonalAccount(account, account.getId());
    assertEquals(account.getId(), retrieved.getId());
    assertEquals(account.getEmail(), retrieved.getEmail());

    // Test unauthorized access
    PersonalAccount otherAccount = personalAccountService.createPersonalAccount("other@example.com", "password456");
    assertThrows(SecurityException.class, () -> {
      personalAccountService.getPersonalAccount(otherAccount, account.getId());
    });
  }

  @Test
  void testUpdatePersonalAccount() {
    PersonalAccount account = personalAccountService.createPersonalAccount("update@example.com", "password123");

    // Test successful update
    PersonalAccount updated = personalAccountService.updatePersonalAccount(
        account,
        account.getId(),
        "updated@example.com",
        "newPassword");

    assertEquals("updated@example.com", updated.getEmail());
  }

  @Test
  void testDeletePersonalAccount() {
    PersonalAccount account = personalAccountService.createPersonalAccount("delete@example.com", "password123");

    // Test successful deletion
    personalAccountService.deletePersonalAccount(account, account.getId());

    assertThrows(IllegalArgumentException.class, () -> {
      personalAccountService.getPersonalAccount(account, account.getId());
    });
  }
}
