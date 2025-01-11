package com.ll.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.ll.dto.PersonalAccountRequest;
import com.ll.model.PersonalAccount;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PersonalAccountControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private static final String BASE_URL = "/api/accounts";

  @Test
  public void testPersonalAccountValidDetails() {
    // Arrange
    PersonalAccountRequest request = new PersonalAccountRequest("john.doe@example.com",
        "securePassword123");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonalAccountRequest> requestEntity = new HttpEntity<>(request, headers);

    // Act
    ResponseEntity<PersonalAccount> response = restTemplate.postForEntity(
        BASE_URL,
        requestEntity,
        PersonalAccount.class);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getHeaders().get("x-api-key"));
    assertEquals(request.getEmail(), response.getBody().getEmail());
  }

  /**
   * 1. Create a new CreateAccountRequest object with invalid details.
   * 2. Make a POST request to the /api/accounts endpoint with the invalid request
   * object and headers.
   * 3. Assert that the response status code is HttpStatus.BAD_REQUEST.
   */
  @Test
  public void testPersonalAccountInvalidDetails() {
    // Arrange
    PersonalAccountRequest request = new PersonalAccountRequest("", ""); // Invalid email and short password
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonalAccountRequest> requestEntity = new HttpEntity<>(request, headers);

    // Act
    ResponseEntity<PersonalAccount> response = restTemplate.postForEntity(
        BASE_URL,
        requestEntity,
        PersonalAccount.class);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  /**
   * 1. Create a valid account for Alice by Making API call to /api/accounts
   * endpoint.
   * 2. Capture the x-api-key from the response headers.
   * 3. Make a GET request to the /api/accounts/{id} endpoint with the captured
   * x-api-key.
   * 4. Assert that the response status code is HttpStatus.OK.
   * 5. Assert that the response body is not null.
   * 6. Assert that the response body email is equal to Alice's email.
   * 7. Assert that the response body id is not null.
   */
  @Test
  void testGetPersonalAccountById() {

    // Arrange - Create Alice's account
    PersonalAccountRequest aliceRequest = new PersonalAccountRequest("alice@example.com",
        "securePassword123");
    HttpHeaders createHeaders = new HttpHeaders();
    createHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PersonalAccountRequest> createRequestEntity = new HttpEntity<>(aliceRequest, createHeaders);

    ResponseEntity<PersonalAccount> createResponse = restTemplate.postForEntity(
        BASE_URL,
        createRequestEntity,
        PersonalAccount.class);

    String token = createResponse.getHeaders().getFirst("x-api-key");
    PersonalAccount account = createResponse.getBody();

    // Act - Get account details
    HttpHeaders headers = new HttpHeaders();
    headers.set("x-api-key", token);
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<PersonalAccount> response = restTemplate.exchange(
        BASE_URL + "/" + account.getId(),
        org.springframework.http.HttpMethod.GET,
        requestEntity,
        PersonalAccount.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("alice@example.com", response.getBody().getEmail());
    assertNotNull(response.getBody().getId());
  }

}
