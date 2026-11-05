package com.medicare.dao;

import java.sql.*;
import com.medicare.exception.AuthenticationException;
import com.medicare.exception.InvalidInputException;
import com.medicare.model.Patient;
import com.medicare.model.User;
import com.medicare.util.DBConnection;

public class UserDAO {

    public User authenticate(String email, String password) throws AuthenticationException {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            throw new AuthenticationException("Please provide your registered Email Address and Password.");
        }

        String sql = "SELECT u.UserID, u.Username, u.Password, u.FullName, u.Email, u.Role, u.AssignedBranchID, " +
                     "p.PhoneNumber, p.Age, p.Gender, p.BloodGroup, p.Address " +
                     "FROM dbo.Users u LEFT JOIN dbo.Patients p ON u.UserID = p.PatientID " +
                     "WHERE LOWER(u.Email) = LOWER(?) AND u.Password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("UserID");
                    String uname = rs.getString("Username");
                    String fullName = rs.getString("FullName");
                    String uEmail = rs.getString("Email");
                    String role = rs.getString("Role");
                    Integer branchId = (Integer) rs.getObject("AssignedBranchID");

                    if ("PATIENT".equalsIgnoreCase(role)) {
                        return new Patient(userId, uname, rs.getString("Password"), fullName, uEmail,
                                rs.getString("PhoneNumber"), rs.getInt("Age"), rs.getString("Gender"),
                                rs.getString("BloodGroup"), rs.getString("Address"));
                    } else {
                        return new User(userId, uname, rs.getString("Password"), fullName, uEmail, role, branchId);
                    }
                } else {
                    throw new AuthenticationException("Invalid email address or password. Please verify your credentials.");
                }
            }
        } catch (SQLException e) {
            System.err.println("DB Auth Error: " + e.getMessage());
            throw new AuthenticationException("Database error during login. Please ensure SQL Server is running.");
        }
    }

    public boolean registerPatient(Patient patient) throws InvalidInputException {
        String insertUserSql = "INSERT INTO dbo.Users (Username, Password, FullName, Email, Role) VALUES (?, ?, ?, ?, 'PATIENT')";
        String insertPatientSql = "INSERT INTO dbo.Patients (PatientID, PhoneNumber, Age, Gender, BloodGroup, Address) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedUserId = 0;
            try (PreparedStatement psUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, patient.getEmail().trim().toLowerCase());
                psUser.setString(2, patient.getPassword());
                psUser.setString(3, patient.getFullName().trim());
                psUser.setString(4, patient.getEmail().trim().toLowerCase());
                psUser.executeUpdate();

                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) generatedUserId = rs.getInt(1);
                    else throw new SQLException("Failed to obtain User ID.");
                }
            }

            try (PreparedStatement psPat = conn.prepareStatement(insertPatientSql)) {
                psPat.setInt(1, generatedUserId);
                psPat.setString(2, patient.getPhoneNumber().trim());
                psPat.setInt(3, patient.getAge());
                psPat.setString(4, patient.getGender());
                psPat.setString(5, patient.getBloodGroup());
                psPat.setString(6, patient.getAddress() != null ? patient.getAddress().trim() : "Bangalore");
                psPat.executeUpdate();
            }

            conn.commit();
            patient.setUserId(generatedUserId);
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            if (e.getMessage() != null && (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate"))) {
                throw new InvalidInputException("An account with this email address already exists. Please log in.");
            }
            throw new InvalidInputException("Registration failed: " + e.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {} }
        }
    }

    public boolean registerBranchAdmin(User admin, int branchId) throws InvalidInputException {
        String insertUserSql = "INSERT INTO dbo.Users (Username, Password, FullName, Email, Role, AssignedBranchID) VALUES (?, ?, ?, ?, 'BRANCH_ADMIN', ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
            ps.setString(1, admin.getEmail().trim().toLowerCase());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getFullName().trim());
            ps.setString(4, admin.getEmail().trim().toLowerCase());
            ps.setInt(5, branchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate"))) {
                throw new InvalidInputException("An administrator with this email address already exists.");
            }
            throw new InvalidInputException("Admin registration failed: " + e.getMessage());
        }
    }
}