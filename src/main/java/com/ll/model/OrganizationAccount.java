package com.ll.model;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "accounts")
public class OrganizationAccount extends Account {
  private String ownerId;
  private List<OrganizationMembership> members;
  private String name;
  private String description;

  public OrganizationAccount() {
    super(AccountType.ORGANIZATION);
    this.members = new ArrayList<>();
  }

  public OrganizationAccount(String ownerId, String name) {
    super(AccountType.ORGANIZATION);
    this.ownerId = ownerId;
    this.name = name;
    this.members = new ArrayList<>();
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public List<OrganizationMembership> getMembers() {
    return members;
  }

  public void setMembers(List<OrganizationMembership> members) {
    this.members = members;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
