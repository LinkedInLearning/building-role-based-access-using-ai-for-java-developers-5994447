package com.ll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.Contract;
import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;
import com.ll.repository.ContractRepository;

@SpringBootTest
public class PersonalContractServiceTest {

  @Autowired
  private PersonalContractService personalContractService;

  @Autowired
  private ContractRepository contractRepository;

  @Autowired
  private AccountRepository accountRepository;

  @AfterEach
  void tearDown() {
    contractRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void testCreateContract() {
    // Create test account
    PersonalAccount account = new PersonalAccount("octocat@github.com", "password");
    account = accountRepository.save(account);

    // Create contract
    String contractName = "Test Contract";
    String contractDescription = "Test Description";
    Contract contract = personalContractService.createContract(account, contractName, contractDescription);

    // Verify
    assertNotNull(contract);
    assertEquals(contractName, contract.getName());
    assertEquals(contractDescription, contract.getDescription());
    assertEquals(account.getId(), contract.getOwner().getId());
  }

  /**
   * 1. Create a Personal Account for Alice
   * 2. Create a Contract for Alice
   * 3. Validate that contract has been created.
   * 4. Update the contract name and description as Alice
   * 5. Validate that contract has been updated.
   * 6. Delete the contract as Alice
   * 7. Validate that contract has been deleted.
   */
  @Test
  void testOwnerContractFlow() {
    // 1. Create a Personal Account for Alice
    PersonalAccount aliceAccount = new PersonalAccount("alice@github.com", "password123");
    aliceAccount = accountRepository.save(aliceAccount);

    // 2. Create a Contract for Alice
    String contractName = "Alice Contract";
    String contractDescription = "Alice's Test Contract";
    Contract contract = personalContractService.createContract(aliceAccount, contractName, contractDescription);

    // 3. Validate contract creation
    assertNotNull(contract);
    assertEquals(contractName, contract.getName());
    assertEquals(contractDescription, contract.getDescription());
    assertEquals(aliceAccount.getId(), contract.getOwner().getId());

    // 4. Update the contract
    String updatedName = "Updated Contract";
    String updatedDescription = "Updated Description";
    Contract updatedContract = personalContractService.updateContract(aliceAccount, contract.getId(), updatedName, updatedDescription);

    // 5. Validate contract update
    assertEquals(updatedName, updatedContract.getName());
    assertEquals(updatedDescription, updatedContract.getDescription());
    assertEquals(aliceAccount.getId(), updatedContract.getOwner().getId());

    // 6. Delete the contract
    personalContractService.deleteContract(aliceAccount, contract.getId());

    // 7. Validate contract deletion
    assertEquals(0, contractRepository.count());
  }

}
