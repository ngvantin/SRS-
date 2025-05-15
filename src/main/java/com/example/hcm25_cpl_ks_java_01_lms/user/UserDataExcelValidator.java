package com.example.hcm25_cpl_ks_java_01_lms.user;

import java.util.ArrayList;
import java.util.List;

public class UserDataExcelValidator {
    public static List<User> filterDuplicateUsers(List<User> users) {
        List<User> filteredUsers = new ArrayList<>();
        List<String> uniqueUsernames = new ArrayList<>();
        List<String> uniqueEmails = new ArrayList<>();
        for (User user : users) {
            if (!uniqueUsernames.contains(user.getUsername()) && !uniqueEmails.contains(user.getEmail())) {
                uniqueUsernames.add(user.getUsername());
                uniqueEmails.add(user.getEmail());
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }
}
