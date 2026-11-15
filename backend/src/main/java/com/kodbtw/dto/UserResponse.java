package com.kodbtw.dto;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Boolean verificationEmailSent;
    private String message;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserResponse(Long id, String name, String email, Boolean verificationEmailSent, String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.verificationEmailSent = verificationEmailSent;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getVerificationEmailSent() {
        return verificationEmailSent;
    }

    public void setVerificationEmailSent(Boolean verificationEmailSent) {
        this.verificationEmailSent = verificationEmailSent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
