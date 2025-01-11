package com.ll.dto;

public class PersonalAccountRequest {
  private String email;
  private String password;

  // Default constructor for JSON deserialization
  public PersonalAccountRequest() {
  }

  public PersonalAccountRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
