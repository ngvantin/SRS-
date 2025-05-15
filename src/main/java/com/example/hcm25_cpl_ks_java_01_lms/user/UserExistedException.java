package com.example.hcm25_cpl_ks_java_01_lms.user;

public class UserExistedException extends RuntimeException {
    public UserExistedException(String message) {
        super(message);
    }
}
