package com.medicare.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
    private Integer assignedBranchId;

    public User() {}

    public User(int userId, String username, String password, String fullName, String email, String role, Integer assignedBranchId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.assignedBranchId = assignedBranchId;
    }

    public static boolean isValidPassword(String p) {
        return p != null && p.length() >= 6 && p.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^+=_-]).{6,}$");
    }

    public static boolean isValidEmail(String e) {
        return e != null && e.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getAssignedBranchId() { return assignedBranchId; }
    public void setAssignedBranchId(Integer assignedBranchId) { this.assignedBranchId = assignedBranchId; }
}