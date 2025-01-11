package com.ll.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document
public abstract class Resource<T extends Resource<T>> {
  @Id
  private String id;

  @DBRef
  private Account owner;

  private Instant createdAt;
  private Instant updatedAt;

  // Required by MongoDB
  protected Resource() {
  }

  protected Resource(Account owner) {
    this();
    this.id = UUID.randomUUID().toString();
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    this.owner = owner;
  }

  public String getId() {
    return id;
  }

  public Account getOwner() {
    return owner;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  protected void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  protected void setId(String id) {
    this.id = id;
  }
}
