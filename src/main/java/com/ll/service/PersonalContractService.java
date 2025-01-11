package com.ll.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ll.model.Account;
import com.ll.model.Contract;
import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;
import com.ll.repository.ContractRepository;

@Service
public class PersonalContractService {
  private final ContractRepository contractRepository;
  private final AccountRepository accountRepository;

  PersonalContractService(ContractRepository contractRepository, AccountRepository accountRepository) {
    this.contractRepository = contractRepository;
    this.accountRepository = accountRepository;
  }

  public Contract createContract(PersonalAccount requestingAccount, String name, String description) {
    Optional<Account> accountOpt = accountRepository.findById(requestingAccount.getId());
    if (accountOpt.isEmpty()) {
      throw new IllegalArgumentException("Account not found");
    }

    Contract contract = new Contract(requestingAccount, name, description);
    return contractRepository.save(contract);
  }

  public Optional<Contract> getContract(PersonalAccount requestingAccount, String contractId) {
    Contract contract = contractRepository.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

    if (!contract.getOwner().getId().equals(requestingAccount.getId())) {
      throw new IllegalArgumentException("Not authorized to access this contract");
    }

    return Optional.of(contract);
  }

  public Contract updateContract(PersonalAccount requestingAccount, String contractId, String name,
      String description) {
    Contract contract = getContract(requestingAccount, contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

    contract.setName(name);
    contract.setDescription(description);

    return contractRepository.save(contract);
  }

  public void deleteContract(PersonalAccount requestingAccount, String contractId) {
    Contract contract = getContract(requestingAccount, contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
    contractRepository.delete(contract);
  }
}
