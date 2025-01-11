package com.ll.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.Contract;
import com.ll.model.PersonalAccount;

@SpringBootTest
public class ContractRepositoryTest {

  @Autowired
  private ContractRepository contractRepository;

  @Autowired
  private AccountRepository accountRepository;

  @AfterEach
  void cleanup() {
    contractRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void shouldCreateContract() {
    // Given
    PersonalAccount account = new PersonalAccount("octocat@github.com", "password123");
    accountRepository.save(account);

    Contract contract = new Contract(account, "GitHub Enterprise License",
        "Annual license agreement for GitHub Enterprise");

    // When
    Contract savedContract = contractRepository.save(contract);

    // Then
    assertNotNull(savedContract.getId());
    assertEquals("GitHub Enterprise License", savedContract.getName());
    assertEquals("Annual license agreement for GitHub Enterprise", savedContract.getDescription());
    assertEquals(account.getId(), savedContract.getOwner().getId());
  }

  @Test
  void shouldReadContract() {
    // Given
    PersonalAccount account = new PersonalAccount("torvalds@github.com", "password123");
    accountRepository.save(account);

    Contract contract = new Contract(account, "Linux Foundation Agreement",
        "Collaboration agreement for kernel development");
    Contract savedContract = contractRepository.save(contract);

    // When
    Contract foundContract = contractRepository.findById(savedContract.getId()).orElse(null);

    // Then
    assertNotNull(foundContract);
    assertEquals("Linux Foundation Agreement", foundContract.getName());
    assertEquals("Collaboration agreement for kernel development", foundContract.getDescription());
  }

  @Test
  void shouldUpdateContract() {
    // Given
    PersonalAccount account = new PersonalAccount("ada@github.com", "password123");
    accountRepository.save(account);

    Contract contract = new Contract(account, "Initial Agreement", "First version");
    Contract savedContract = contractRepository.save(contract);

    // When
    savedContract.updateContract("Updated Agreement", "Second version");
    Contract updatedContract = contractRepository.save(savedContract);

    // Then
    assertEquals("Updated Agreement", updatedContract.getName());
    assertEquals("Second version", updatedContract.getDescription());
    assertTrue(updatedContract.getUpdatedAt().isAfter(updatedContract.getCreatedAt()));
  }

  @Test
  void shouldDeleteContract() {
    // Given
    PersonalAccount account = new PersonalAccount("turing@github.com", "password123");
    accountRepository.save(account);

    Contract contract = new Contract(account, "Computing Services Agreement", "Terms for computing services");
    Contract savedContract = contractRepository.save(contract);

    // When
    contractRepository.deleteById(savedContract.getId());

    // Then
    assertFalse(contractRepository.findById(savedContract.getId()).isPresent());
  }
}
