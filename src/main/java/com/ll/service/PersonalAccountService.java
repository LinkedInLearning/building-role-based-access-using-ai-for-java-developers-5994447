package com.ll.service;

import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonalAccountService {
  private final AccountRepository accountRepository;

  public PersonalAccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public PersonalAccount createPersonalAccount(String email, String password) {
    if (accountRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Account with email " + email + " already exists");
    }

    PersonalAccount account = new PersonalAccount();
    account.setEmail(email);
    account.setPassword(password);

    return accountRepository.savePersonalAccount(account);
  }

  public PersonalAccount getPersonalAccount(PersonalAccount requestingAccount, String id) {
    if (!requestingAccount.getId().equals(id)) {
      throw new SecurityException("Not authorized to access this account");
    }

    return accountRepository.findById(id)
        .map(account -> {
          if (!(account instanceof PersonalAccount)) {
            throw new IllegalArgumentException("Account is not a personal account");
          }
          return (PersonalAccount) account;
        })
        .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
  }

  public PersonalAccount updatePersonalAccount(PersonalAccount requestingAccount, String id,
      String newEmail, String newPassword) {
    if (!requestingAccount.getId().equals(id)) {
      throw new SecurityException("Not authorized to modify this account");
    }

    PersonalAccount existingAccount = accountRepository.findById(id)
        .map(account -> {
          if (!(account instanceof PersonalAccount)) {
            throw new IllegalArgumentException("Account is not a personal account");
          }
          return (PersonalAccount) account;
        })
        .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

    if (newEmail != null && !newEmail.equals(existingAccount.getEmail())
        && accountRepository.existsByEmail(newEmail)) {
      throw new IllegalArgumentException("Email already in use: " + newEmail);
    }

    if (newEmail != null)
      existingAccount.setEmail(newEmail);
    if (newPassword != null)
      existingAccount.setPassword(newPassword);

    return accountRepository.savePersonalAccount(existingAccount);
  }

  public void deletePersonalAccount(PersonalAccount requestingAccount, String id) {
    if (!requestingAccount.getId().equals(id)) {
      throw new SecurityException("Not authorized to delete this account");
    }

    if (!accountRepository.existsById(id)) {
      throw new IllegalArgumentException("Account not found with id: " + id);
    }

    accountRepository.deleteById(id);
  }

}
