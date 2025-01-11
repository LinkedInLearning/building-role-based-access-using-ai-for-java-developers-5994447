package com.ll.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.OrganizationAccount;
import com.ll.model.OrganizationRole;
import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;

@SpringBootTest
public class OrganizationAccountServiceTest {
  @Autowired
  private OrganizationAccountService organizationAccountService;

  @Autowired
  private AccountRepository accountRepository;

  @BeforeEach
  void cleanup() {
    accountRepository.deleteAll();
  }

  @Test
  void testCreateOrganization() {
    // Create a personal account first
    PersonalAccount owner = new PersonalAccount("octocat@github.com", "Octocat");
    owner = accountRepository.savePersonalAccount(owner);

    // Create organization
    String orgName = "Test Organization";
    String orgDescription = "Test Description";
    OrganizationAccount org = organizationAccountService.createOrganization(owner, orgName, orgDescription);

    // Verify organization
    assertNotNull(org);
    assertNotNull(org.getId());
    assertEquals(orgName, org.getName());
    assertEquals(orgDescription, org.getDescription());
    assertEquals(owner.getId(), org.getOwnerId());
    assertTrue(org.isMember(owner.getId()));
  }

  /**
   * 1. Create a personal account for Alice
   * 2. Create an organization account for Tech Corp with Alice as the owner
   * 3. Verify Alice is the owner of Tech Corp
   * 4. Delete the organization account
   * 5. Verify the organization account is deleted
   */
  @Test
  void testOwnerOrganizationManagementFlow() {
    // Create Alice's personal account
    PersonalAccount alice = new PersonalAccount("alice@example.com", "Alice");
    alice = accountRepository.savePersonalAccount(alice);

    // Create organization with Alice as owner
    String orgName = "Tech Corp";
    String orgDescription = "Technology Corporation";
    OrganizationAccount techCorp = organizationAccountService.createOrganization(alice, orgName, orgDescription);

    // Verify organization creation and ownership
    assertNotNull(techCorp);
    assertEquals(orgName, techCorp.getName());
    assertEquals(orgDescription, techCorp.getDescription());
    assertEquals(alice.getId(), techCorp.getOwnerId());
    assertTrue(techCorp.isMember(alice.getId()));
    assertEquals(OrganizationRole.OWNER, techCorp.getMemberRole(alice.getId()));

    // Delete organization
    String orgId = techCorp.getId();
    organizationAccountService.deleteOrganization(alice, orgId);

    // Verify organization is deleted
    assertTrue(accountRepository.findOrganizationById(orgId).isEmpty());
  }

  /**
   * 1. Create personal accounts for Alice, Bob, and Charlie
   * 2. Create an organization account for Tech Corp with Alice as the owner
   * 3. Add Bob as Editor and Charlie as Viewer
   * 4. Verify Bob and Charlie are members of Tech Corp
   * 5. Verify Bob is an Editor and Charlie is a Viewer
   * 6. Verify that Bob cannot add members
   * 7. Verify that Bob cannot update member roles
   * 8. Verify that Bob cannot remove members
   * 9. Verify that Bob cannot delete the organization
   * 10. Verify that Charlie cannot add members
   * 11. Verify that Charlie cannot update member roles
   * 12. Verify that Charlie cannot remove members
   * 13. Verify that Charlie cannot delete the organization
   * 14. Verify that Alice can delete the organization
   */
  @Test
  void testMemberOrganizationManagementFlow() {
    // Create personal accounts
    PersonalAccount alice = accountRepository.savePersonalAccount(new PersonalAccount("alice@example.com", "Alice"));
    PersonalAccount bob = accountRepository.savePersonalAccount(new PersonalAccount("bob@example.com", "Bob"));
    PersonalAccount charlie = accountRepository
        .savePersonalAccount(new PersonalAccount("charlie@example.com", "Charlie"));

    // Create organization with Alice as owner
    OrganizationAccount techCorp = organizationAccountService.createOrganization(alice, "Tech Corp",
        "Technology Corporation");

    // Add Bob as Editor and Charlie as Viewer
    organizationAccountService.addMember(alice, techCorp.getId(), bob.getId(), OrganizationRole.EDITOR);
    organizationAccountService.addMember(alice, techCorp.getId(), charlie.getId(), OrganizationRole.VIEWER);

    // Verify memberships and roles
    techCorp = organizationAccountService.getOrganization(alice, techCorp.getId());
    assertTrue(techCorp.isMember(bob.getId()));
    assertTrue(techCorp.isMember(charlie.getId()));
    assertEquals(OrganizationRole.EDITOR, techCorp.getMemberRole(bob.getId()));
    assertEquals(OrganizationRole.VIEWER, techCorp.getMemberRole(charlie.getId()));

    // Test Bob's restricted permissions
    String orgId = techCorp.getId();
    PersonalAccount david = accountRepository.savePersonalAccount(new PersonalAccount("david@example.com", "David"));

    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.addMember(bob, orgId, david.getId(), OrganizationRole.VIEWER));
    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.updateMemberRole(bob, orgId, charlie.getId(), OrganizationRole.EDITOR));
    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.removeMember(bob, orgId, charlie.getId()));
    assertThrows(IllegalArgumentException.class, () -> organizationAccountService.deleteOrganization(bob, orgId));

    // Test Charlie's restricted permissions
    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.addMember(charlie, orgId, david.getId(), OrganizationRole.VIEWER));
    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.updateMemberRole(charlie, orgId, bob.getId(), OrganizationRole.VIEWER));
    assertThrows(IllegalArgumentException.class,
        () -> organizationAccountService.removeMember(charlie, orgId, bob.getId()));
    assertThrows(IllegalArgumentException.class, () -> organizationAccountService.deleteOrganization(charlie, orgId));

    // Verify Alice can delete the organization
    assertDoesNotThrow(() -> organizationAccountService.deleteOrganization(alice, orgId));
    assertTrue(accountRepository.findOrganizationById(orgId).isEmpty());
  }
}
