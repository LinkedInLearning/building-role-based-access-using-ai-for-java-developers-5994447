package com.ll;

import com.mongodb.client.*;
import org.bson.Document;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DbOperationsTest {
  private MongoClient mongoClient;
  private MongoDatabase database;
  private MongoCollection<Document> collection;

  // Employee class for testing
  static class Employee {
    private String id;
    private String name;
    private int age;
    private String department;

    public Employee(String name, int age, String department) {
      this.name = name;
      this.age = age;
      this.department = department;
    }

    public Document toDocument() {
      return new Document()
          .append("name", name)
          .append("age", age)
          .append("department", department);
    }
  }

  @BeforeEach
  void setUp() {
    mongoClient = MongoClients.create("mongodb://localhost:27017");
    database = mongoClient.getDatabase("testdb");
    collection = database.getCollection("employees");
  }

  @AfterEach
  void tearDown() {
    collection.drop();
    mongoClient.close();
  }

  @Test
  void testCreateEmployee() {
    Employee employee = new Employee("John Doe", 30, "Engineering");
    collection.insertOne(employee.toDocument());

    Document found = collection.find().first();
    assertNotNull(found);
    assertEquals("John Doe", found.getString("name"));
    assertEquals(30, found.getInteger("age"));
    assertEquals("Engineering", found.getString("department"));
  }

  @Test
  void testReadEmployee() {
    Employee employee = new Employee("Jane Smith", 25, "HR");
    collection.insertOne(employee.toDocument());

    Document query = new Document("name", "Jane Smith");
    Document found = collection.find(query).first();

    assertNotNull(found);
    assertEquals("Jane Smith", found.getString("name"));
    assertEquals(25, found.getInteger("age"));
    assertEquals("HR", found.getString("department"));
  }

  @Test
  void testUpdateEmployee() {
    Employee employee = new Employee("Bob Wilson", 35, "Sales");
    collection.insertOne(employee.toDocument());

    Document query = new Document("name", "Bob Wilson");
    Document update = new Document("$set", new Document("age", 36));
    collection.updateOne(query, update);

    Document found = collection.find(query).first();
    assertNotNull(found);
    assertEquals(36, found.getInteger("age"));
  }

  @Test
  void testDeleteEmployee() {
    Employee employee = new Employee("Alice Brown", 28, "Marketing");
    collection.insertOne(employee.toDocument());

    Document query = new Document("name", "Alice Brown");
    collection.deleteOne(query);

    Document found = collection.find(query).first();
    assertNull(found);
  }
}
