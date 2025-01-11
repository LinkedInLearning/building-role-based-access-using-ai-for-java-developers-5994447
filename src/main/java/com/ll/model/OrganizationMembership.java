package com.ll.model;

public class OrganizationMembership {
  private String memberId;
  private OrganizationRole role;

  public OrganizationMembership() {
  }

  public OrganizationMembership(String memberId, OrganizationRole role) {
    this.memberId = memberId;
    this.role = role;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public OrganizationRole getRole() {
    return role;
  }

  public void setRole(OrganizationRole role) {
    this.role = role;
  }
}
