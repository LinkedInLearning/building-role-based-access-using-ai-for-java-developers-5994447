package com.ll.model;

import java.time.Instant;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contracts")
public final class Contract extends Resource<Contract> {
  private String name;
  private String description;

  // Required by MongoDB
  protected Contract() {
    super();
  }

  public Contract(Account owner, String name, String description) {
    super(owner);
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void updateContract(String name, String description) {
    this.name = name;
    this.description = description;
    setUpdatedAt(Instant.now());
  }

  // Add setters for MongoDB mapping
  protected void setName(String name) {
    this.name = name;
  }

  protected void setDescription(String description) {
    this.description = description;
  }
}
