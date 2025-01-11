package com.ll.model;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

  public OrganizationAccount(PersonalAccount owner, String name, String description) {
    super(AccountType.ORGANIZATION);
    this.ownerId = owner.getId();
    this.name = name;
    this.description = description;
    this.members = new ArrayList<>();
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public List<OrganizationMembership> getMembers() {
    return Collections.unmodifiableList(members);
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

  public void addMember(String memberId, OrganizationRole role) {
    Objects.requireNonNull(memberId, "Member ID cannot be null");
    Objects.requireNonNull(role, "Role cannot be null");

    if (memberId.equals(ownerId)) {
      throw new IllegalArgumentException("Owner is already a member");
    }

    if (isMember(memberId)) {
      throw new IllegalArgumentException("User is already a member");
    }

    members.add(new OrganizationMembership(memberId, role));
  }

  public void removeMember(String memberId) {
    Objects.requireNonNull(memberId, "Member ID cannot be null");

    if (memberId.equals(ownerId)) {
      throw new IllegalArgumentException("Cannot remove the owner");
    }

    members.removeIf(member -> member.getMemberId().equals(memberId));
  }

  public void updateMemberRole(String memberId, OrganizationRole newRole) {
    Objects.requireNonNull(memberId, "Member ID cannot be null");
    Objects.requireNonNull(newRole, "Role cannot be null");

    if (memberId.equals(ownerId)) {
      throw new IllegalArgumentException("Cannot change owner's role");
    }

    members.stream()
        .filter(member -> member.getMemberId().equals(memberId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Member not found"))
        .setRole(newRole);
  }

  public OrganizationRole getMemberRole(String memberId) {
    if (memberId.equals(ownerId)) {
      return OrganizationRole.OWNER;
    }

    return members.stream()
        .filter(member -> member.getMemberId().equals(memberId))
        .findFirst()
        .map(OrganizationMembership::getRole)
        .orElse(null);
  }

  public boolean isMember(String memberId) {
    if (memberId.equals(ownerId)) {
      return true;
    }
    return members.stream().anyMatch(member -> member.getMemberId().equals(memberId));
  }
}
