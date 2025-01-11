package com.ll.service;

import org.springframework.stereotype.Service;

import com.ll.model.OrganizationAccount;
import com.ll.model.OrganizationRole;
import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;

@Service
public class OrganizationAccountService {
  private final AccountRepository accountRepository;
  private final PersonalAccountService personalAccountService;

  public OrganizationAccountService(AccountRepository accountRepository,
      PersonalAccountService personalAccountService) {
    this.accountRepository = accountRepository;
    this.personalAccountService = personalAccountService;
  }

  public OrganizationAccount createOrganization(PersonalAccount requestingAccount, String name, String description) {
    PersonalAccount owner = personalAccountService.getPersonalAccount(requestingAccount.getId())
        .orElseThrow(() -> new IllegalArgumentException("Owner account not found"));

    OrganizationAccount org = new OrganizationAccount(owner, name, description);
    return accountRepository.save(org);
  }

  public OrganizationAccount getOrganization(PersonalAccount requestingAccount, String organizationId) {
    return accountRepository.findOrganizationById(organizationId)
        .filter(org -> org.isMember(requestingAccount.getId()))
        .orElseThrow(() -> new IllegalArgumentException("Organization not found or access denied"));
  }

  public OrganizationAccount addMember(PersonalAccount requestingAccount, String organizationId,
      String newMemberId, OrganizationRole role) {
    OrganizationAccount org = accountRepository.findOrganizationById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!org.getOwnerId().equals(requestingAccount.getId())) {
      throw new IllegalArgumentException("Only owner can add members");
    }

    org.addMember(newMemberId, role);
    return accountRepository.save(org);
  }

  public OrganizationAccount updateMemberRole(PersonalAccount requestingAccount, String organizationId,
      String memberId, OrganizationRole newRole) {
    OrganizationAccount org = accountRepository.findOrganizationById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!org.getOwnerId().equals(requestingAccount.getId())) {
      throw new IllegalArgumentException("Only owner can update member roles");
    }

    org.updateMemberRole(memberId, newRole);
    return accountRepository.save(org);
  }

  public OrganizationAccount removeMember(PersonalAccount requestingAccount, String organizationId,
      String memberId) {
    OrganizationAccount org = accountRepository.findOrganizationById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!org.getOwnerId().equals(requestingAccount.getId())) {
      throw new IllegalArgumentException("Only owner can remove members");
    }

    org.removeMember(memberId);
    return accountRepository.save(org);
  }

  public void deleteOrganization(PersonalAccount requestingAccount, String organizationId) {
    OrganizationAccount org = accountRepository.findOrganizationById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!org.getOwnerId().equals(requestingAccount.getId())) {
      throw new IllegalArgumentException("Only owner can delete the organization");
    }

    accountRepository.deleteOrganizationById(organizationId);
  }
}