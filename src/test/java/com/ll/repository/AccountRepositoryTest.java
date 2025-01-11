package com.ll.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.OrganizationAccount;
import com.ll.model.OrganizationRole;
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

  /**
   * 1. Create a Personal Account for Alice
   * 2. Create a Personal Account for Bob
   * 3. Create a Personal Account for Charlie
   * 4. Create an Organization Account for Alice
   * 5. Validate that the account was created
   * 6. Add Bob as Editor to the Organization
   * 7. Add Charlie as Viewer to the Organization
   * 8. Validate that Bob and Charlie are members of the Organization along with
   * their roles
   * 9. Remove Charlie from the Organization
   * 10. Validate that Charlie is no longer a member of the Organization
   * 11. Change Bob's role to Viewer
   * 12. Validate that Bob's role is now Viewer
   */
  @Test
  void testOrganizationMemberships() {
    // Create personal accounts
    PersonalAccount aliceAccount = accountRepository.savePersonalAccount(
        new PersonalAccount("alice@github.com", "alice123"));
    PersonalAccount bobAccount = accountRepository.savePersonalAccount(
        new PersonalAccount("bob@github.com", "bob123"));
    PersonalAccount charlieAccount = accountRepository.savePersonalAccount(
        new PersonalAccount("charlie@github.com", "charlie123"));

    // Create organization
    OrganizationAccount orgAccount = new OrganizationAccount(aliceAccount.getId(), "Alice's Org");
    OrganizationAccount savedOrg = accountRepository.saveOrganizationAccount(orgAccount);

    // Verify organization creation
    assertNotNull(savedOrg.getId());
    assertEquals(aliceAccount.getId(), savedOrg.getOwnerId());
    assertEquals(OrganizationRole.OWNER, savedOrg.getMemberRole(aliceAccount.getId()));

    // Add Bob as Editor
    savedOrg.addMember(bobAccount.getId(), OrganizationRole.EDITOR);
    // Add Charlie as Viewer
    savedOrg.addMember(charlieAccount.getId(), OrganizationRole.VIEWER);
    savedOrg = accountRepository.saveOrganizationAccount(savedOrg);

    // Verify memberships
    assertTrue(savedOrg.isMember(bobAccount.getId()));
    assertTrue(savedOrg.isMember(charlieAccount.getId()));
    assertEquals(OrganizationRole.EDITOR, savedOrg.getMemberRole(bobAccount.getId()));
    assertEquals(OrganizationRole.VIEWER, savedOrg.getMemberRole(charlieAccount.getId()));

    // Remove Charlie
    savedOrg.removeMember(charlieAccount.getId());
    savedOrg = accountRepository.saveOrganizationAccount(savedOrg);

    // Verify Charlie's removal
    assertFalse(savedOrg.isMember(charlieAccount.getId()));
    assertNull(savedOrg.getMemberRole(charlieAccount.getId()));

    // Change Bob's role to Viewer
    savedOrg.updateMemberRole(bobAccount.getId(), OrganizationRole.VIEWER);
    savedOrg = accountRepository.saveOrganizationAccount(savedOrg);

    // Verify Bob's role change
    assertEquals(OrganizationRole.VIEWER, savedOrg.getMemberRole(bobAccount.getId()));
  }
}
