package com.ll.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Document(collection = "accounts")
public class PersonalAccount extends Account {
    @Indexed(unique = true)
    private String email;
    private String hashedPassword;
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PersonalAccount() {
        super(AccountType.PERSONAL);
    }

    public PersonalAccount(String email, String rawPassword) {
        super(AccountType.PERSONAL);
        this.email = email;
        this.setPassword(rawPassword);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setPassword(String rawPassword) {
        this.hashedPassword = passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.hashedPassword);
    }
}
