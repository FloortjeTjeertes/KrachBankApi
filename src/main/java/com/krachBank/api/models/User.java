package com.krachbank.api.models;

import java.time.LocalDateTime;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "users")
public class User implements Model {

    @Id
    private Long id;

    private String name;

    private String dailyLimit;

    private String password;

    private LocalDateTime createdAt;

    private boolean verified;
    private boolean active;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private int bsn;

    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(String dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getBsn() {
        return bsn;
    }

    public void setBsn(int bsn) {
        this.bsn = bsn;
    }

}
