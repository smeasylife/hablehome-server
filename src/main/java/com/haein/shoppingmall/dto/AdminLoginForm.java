package com.haein.shoppingmall.dto;

public class AdminLoginForm {

    private String email;
    private String password;

    public LoginRequest toLoginRequest() {
        return new LoginRequest(email, password);
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
