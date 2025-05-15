package com.example.hcm25_cpl_ks_java_01_lms.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
