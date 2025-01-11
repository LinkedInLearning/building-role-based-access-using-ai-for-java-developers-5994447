package com.ll.repository;

import com.ll.model.Account;
import com.ll.model.OrganizationAccount;
import com.ll.model.PersonalAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {
  @Query(value = "{'accountType': 'PERSONAL', 'email': ?0}")
  Optional<PersonalAccount> findPersonalAccountByEmail(String email);

  default PersonalAccount savePersonalAccount(PersonalAccount account) {
    return (PersonalAccount) save(account);
  }

  @Query(value = "{'accountType': 'PERSONAL', 'email': ?0}", exists = true)
  boolean existsByEmail(String email);

  @Query(value = "{'accountType': 'PERSONAL', 'email': ?0}", delete = true)
  void deletePersonalAccountByEmail(String email);

  @Query("{ '_class' : 'com.ll.model.OrganizationAccount', 'ownerAccountId' :  ?0 }")
  List<OrganizationAccount> findOrganizationsByOwnerId(String ownerId);

  @Query("{ '_id' : ?0, '_class' : 'com.ll.model.OrganizationAccount' }")
  Optional<OrganizationAccount> findOrganizationById(String orgId);

  default OrganizationAccount saveOrganizationAccount(OrganizationAccount account) {
    return (OrganizationAccount) save(account);
  }

  @Query(value = "{'_class': 'com.ll.model.OrganizationAccount', '_id': ?0}", delete = true)
  void deleteOrganizationById(String orgId);
}
