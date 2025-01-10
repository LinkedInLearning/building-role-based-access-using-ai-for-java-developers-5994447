package com.ll.repository;

import com.ll.model.PersonalAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class AccountRepositoryTest {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  private PersonalAccount testAccount;

  @BeforeEach
  void setUp() {
    testAccount = new PersonalAccount("test@example.com", "password123");
    accountRepository.save(testAccount);
  }

  @AfterEach
  void tearDown() {
    accountRepository.deleteAll();
  }

  @Test
  void testDeletePersonalAccountByEmail() {
    // Given
    assertTrue(accountRepository.existsByEmail("test@example.com"));

    // When
    accountRepository.deletePersonalAccountByEmail("test@example.com");

    // Then
    assertFalse(accountRepository.existsByEmail("test@example.com"));
  }

  @Test
  void testExistsByEmail() {
    // Given & When
    boolean exists = accountRepository.existsByEmail("test@example.com");
    boolean notExists = accountRepository.existsByEmail("nonexistent@example.com");

    // Then
    assertTrue(exists);
    assertFalse(notExists);
  }

  @Test
  void testFindPersonalAccountByEmail() {
    // When
    var foundAccount = accountRepository.findPersonalAccountByEmail("test@example.com");
    var notFoundAccount = accountRepository.findPersonalAccountByEmail("nonexistent@example.com");

    // Then
    assertTrue(foundAccount.isPresent());
    assertEquals("test@example.com", foundAccount.get().getEmail());
    assertTrue(notFoundAccount.isEmpty());
  }

  @Test
  void testSavePersonalAccount() {
    // Given
    PersonalAccount newAccount = new PersonalAccount("new@example.com", "newpass123");

    // When
    PersonalAccount saved = accountRepository.savePersonalAccount(newAccount);

    // Then
    assertNotNull(saved.getId());
    assertEquals("new@example.com", saved.getEmail());
    assertTrue(saved.verifyPassword("newpass123"));
  }
}
