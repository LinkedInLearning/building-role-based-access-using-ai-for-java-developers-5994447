package com.ll.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ll.model.Contract;
import com.ll.model.OrganizationAccount;
import com.ll.model.OrganizationRole;
import com.ll.model.PersonalAccount;
import com.ll.repository.ContractRepository;
import com.ll.repository.AccountRepository;

@Service
public class OrganizationContractService {
  private final ContractRepository contractRepository;
  private final AccountRepository accountRepository;

  OrganizationContractService(ContractRepository contractRepository, AccountRepository accountRepository) {
    this.contractRepository = contractRepository;
    this.accountRepository = accountRepository;
  }

  public Contract createContract(PersonalAccount requestingAccount, OrganizationAccount orgAccount, String name,
      String description) {
    // Get the latest organization state
    OrganizationAccount currentOrg = accountRepository.findOrganizationById(orgAccount.getId())
        .orElseThrow(() -> new IllegalStateException("Organization not found"));

    if (!currentOrg.isMember(requestingAccount.getId())) {
      throw new IllegalStateException("Not a member of the organization");
    }

    OrganizationRole role = currentOrg.getMemberRole(requestingAccount.getId());
    if (role != OrganizationRole.OWNER && role != OrganizationRole.EDITOR) {
      throw new IllegalStateException("Insufficient permissions to create contract");
    }

    Contract contract = new Contract(currentOrg, name, description);
    return contractRepository.save(contract);
  }

  public Contract updateContract(PersonalAccount requestingAccount, OrganizationAccount org, String contractId,
      String name, String description) {
    Optional<Contract> contractOpt = contractRepository.findById(contractId);
    if (contractOpt.isEmpty()) {
      throw new IllegalArgumentException("Contract not found");
    }

    Contract contract = contractOpt.get();
    if (!contract.getOwner().getId().equals(org.getId())) {
      throw new IllegalArgumentException("Contract does not belong to this organization");
    }

    // Get the latest organization state
    OrganizationAccount currentOrg = accountRepository.findOrganizationById(org.getId())
        .orElseThrow(() -> new IllegalStateException("Organization not found"));

    // Verify that the requesting account has appropriate permissions
    OrganizationRole role = currentOrg.getMemberRole(requestingAccount.getId());
    if (role != OrganizationRole.OWNER && role != OrganizationRole.EDITOR) {
      throw new IllegalStateException("Insufficient permissions to update organization contract");
    }

    contract.updateContract(name, description);
    return contractRepository.save(contract);
  }

  public void deleteContract(PersonalAccount requestingAccount, OrganizationAccount org, String contractId) {
    Optional<Contract> contractOpt = contractRepository.findById(contractId);
    if (contractOpt.isEmpty()) {
      throw new IllegalArgumentException("Contract not found");
    }

    Contract contract = contractOpt.get();
    if (!contract.getOwner().getId().equals(org.getId())) {
      throw new IllegalArgumentException("Contract does not belong to this organization");
    }

    OrganizationRole role = org.getMemberRole(requestingAccount.getId());
    if (role != OrganizationRole.OWNER) {
      throw new IllegalStateException("Only organization owners can delete contracts");
    }

    contractRepository.delete(contract);
  }

  public Optional<Contract> getContract(PersonalAccount requestingAccount, OrganizationAccount org, String contractId) {
    Optional<Contract> contractOpt = contractRepository.findById(contractId);
    if (contractOpt.isEmpty()) {
      return Optional.empty();
    }

    Contract contract = contractOpt.get();
    if (!contract.getOwner().getId().equals(org.getId())) {
      return Optional.empty();
    }

    // Get the latest organization state to ensure we have up-to-date member roles
    OrganizationAccount currentOrg = accountRepository.findOrganizationById(org.getId())
        .orElseThrow(() -> new IllegalStateException("Organization not found"));

    // Verify that the requesting account is a member of the organization
    if (!currentOrg.isMember(requestingAccount.getId())) {
      return Optional.empty();
    }

    // Any member (OWNER, EDITOR, or VIEWER) can read contracts
    return Optional.of(contract);
  }
}
