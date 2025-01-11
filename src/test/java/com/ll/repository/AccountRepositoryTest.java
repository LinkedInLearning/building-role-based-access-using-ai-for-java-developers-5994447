package com.ll.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.OrganizationAccount;
import com.ll.model.PersonalAccount;

@SpringBootTest
class AccountRepositoryTest {

  @Autowired
  private AccountRepository accountRepository;

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

  /**
   * 1. Create a Personal Account for Alice
   * 2. Validate that the account was created
   * 3. Create an Organization Account for Alice
   * 4. Validate that the account was created
   */
  @Test
  void testCompleteOrganizationAccountCRUDOperations() {
    // Create Personal Account (Owner)
    PersonalAccount aliceAccount = new PersonalAccount("alice@github.com", "alice123");
    PersonalAccount savedAlice = accountRepository.savePersonalAccount(aliceAccount);
    assertNotNull(savedAlice.getId());
    assertEquals("alice@github.com", savedAlice.getEmail());

    // Create Organization Account
    OrganizationAccount orgAccount = new OrganizationAccount(savedAlice.getId(), "Alice's Org");
    orgAccount.setDescription("Organization for testing");
    OrganizationAccount savedOrg = accountRepository.saveOrganizationAccount(orgAccount);

    // Verify Creation
    assertNotNull(savedOrg.getId());
    assertEquals("Alice's Org", savedOrg.getName());
    assertEquals(savedAlice.getId(), savedOrg.getOwnerId());

    // Read
    var foundOrg = accountRepository.findOrganizationById(savedOrg.getId());
    assertTrue(foundOrg.isPresent());
    assertEquals(savedOrg.getId(), foundOrg.get().getId());
    assertEquals("Alice's Org", foundOrg.get().getName());

    // Update
    foundOrg.get().setName("Alice's Updated Org");
    foundOrg.get().setDescription("Updated description");
    OrganizationAccount updatedOrg = accountRepository.saveOrganizationAccount(foundOrg.get());
    assertEquals("Alice's Updated Org", updatedOrg.getName());
    assertEquals("Updated description", updatedOrg.getDescription());

    // Delete
    accountRepository.deleteOrganizationById(updatedOrg.getId());
    assertTrue(accountRepository.findOrganizationById(updatedOrg.getId()).isEmpty());
  }
}
