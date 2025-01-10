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
    testAccount = new PersonalAccount("octocat@github.com", "github123");
    accountRepository.save(testAccount);
  }

  @AfterEach
  void tearDown() {
    accountRepository.deleteAll();
  }

  @Test
  void testDeletePersonalAccountByEmail() {
    // Given
    assertTrue(accountRepository.existsByEmail("octocat@github.com"));

    // When
    accountRepository.deletePersonalAccountByEmail("octocat@github.com");

    // Then
    assertFalse(accountRepository.existsByEmail("octocat@github.com"));
  }

  @Test
  void testExistsByEmail() {
    // Given & When
    boolean exists = accountRepository.existsByEmail("octocat@github.com");
    boolean notExists = accountRepository.existsByEmail("nonexistent@github.com");

    // Then
    assertTrue(exists);
    assertFalse(notExists);
  }

  @Test
  void testFindPersonalAccountByEmail() {
    // When
    var foundAccount = accountRepository.findPersonalAccountByEmail("octocat@github.com");
    var notFoundAccount = accountRepository.findPersonalAccountByEmail("nonexistent@github.com");

    // Then
    assertTrue(foundAccount.isPresent());
    assertEquals("octocat@github.com", foundAccount.get().getEmail());
    assertTrue(notFoundAccount.isEmpty());
  }

  @Test
  void testSavePersonalAccount() {
    // Given
    PersonalAccount newAccount = new PersonalAccount("actions-bot@github.com", "actions123");

    // When
    PersonalAccount saved = accountRepository.savePersonalAccount(newAccount);

    // Then
    assertNotNull(saved.getId());
    assertEquals("actions-bot@github.com", saved.getEmail());
    assertTrue(saved.verifyPassword("actions123"));
  }

  @Test
  void testCompletePersonalAccountCRUDOperations() {
    // Create
    PersonalAccount newAccount = new PersonalAccount("developer@github.com", "dev123");
    PersonalAccount created = accountRepository.savePersonalAccount(newAccount);
    assertNotNull(created.getId());
    assertEquals("developer@github.com", created.getEmail());

    // Read
    var found = accountRepository.findPersonalAccountByEmail("developer@github.com");
    assertTrue(found.isPresent());
    assertEquals(created.getId(), found.get().getId());

    // Update
    found.get().setEmail("senior-dev@github.com");
    PersonalAccount updated = accountRepository.savePersonalAccount(found.get());
    assertEquals("senior-dev@github.com", updated.getEmail());
    assertEquals(created.getId(), updated.getId());

    // Delete
    accountRepository.deletePersonalAccountByEmail("senior-dev@github.com");
    assertFalse(accountRepository.existsByEmail("senior-dev@github.com"));
  }
}
