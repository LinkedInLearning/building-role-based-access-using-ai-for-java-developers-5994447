package com.ll.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dto.PersonalAccountRequest;
import com.ll.model.PersonalAccount;
import com.ll.security.JwtService;
import com.ll.service.PersonalAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Personal Account Management", description = "APIs for managing personal accounts")
public class PersonalAccountController {
  private final PersonalAccountService personalAccountService;
  private final JwtService jwtService;

  public PersonalAccountController(PersonalAccountService personalAccountService,
      
      JwtService jwtService) {
    this.personalAccountService = personalAccountService;
    this.jwtService = jwtService;
  }

  @PostMapping
  @Operation(summary = "Create a new personal account")
  public ResponseEntity<PersonalAccount> createAccount(@RequestBody PersonalAccountRequest request) {
    try {

      // ensure inputs are valid
      if (request.getEmail() == null || request.getEmail().trim().length() == 0
          || request.getPassword() == null || request.getPassword().trim().length() == 0) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      PersonalAccount account = personalAccountService.createPersonalAccount(
          request.getEmail(),
          request.getPassword());

      String token = jwtService.createToken(account);
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .header("x-api-key", token)
          .body(account);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a personal account by ID")
  public ResponseEntity<PersonalAccount> getAccount(
      @PathVariable("id") String id) {
    try {
      PersonalAccount authenticatedAccount = (PersonalAccount) SecurityContextHolder
          .getContext().getAuthentication().getPrincipal();

      return personalAccountService.getPersonalAccount(authenticatedAccount, id)
          .map(account -> new ResponseEntity<>(account, HttpStatus.OK))
          .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a personal account")
  public ResponseEntity<Void> deleteAccount(@PathVariable("id") String id) {
    try {
      PersonalAccount authenticatedAccount = (PersonalAccount) SecurityContextHolder
          .getContext().getAuthentication().getPrincipal();
      if (personalAccountService.deletePersonalAccount(authenticatedAccount, id)) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
