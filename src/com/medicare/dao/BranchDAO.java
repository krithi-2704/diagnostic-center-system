package com.medicare.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.medicare.model.Branch;
import com.medicare.util.DBConnection;

public class BranchDAO {

    public List<Branch> getAllActiveBranches() {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT BranchID, BranchName, Area, Address, ContactPhone, Email, IsActive FROM dbo.Branches WHERE IsActive = 1 ORDER BY BranchID";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Branch(
                    rs.getInt("BranchID"),
                    rs.getString("BranchName"),
                    rs.getString("Area"),
                    rs.getString("Address"),
                    rs.getString("ContactPhone"),
                    rs.getString("Email"),
                    rs.getBoolean("IsActive")
                ));
            }
        } catch (SQLException e) {
            System.err.println("BranchDAO Error: " + e.getMessage());
        }
        return list;
    }

    public Branch getBranchById(int branchId) {
        String sql = "SELECT BranchID, BranchName, Area, Address, ContactPhone, Email, IsActive FROM dbo.Branches WHERE BranchID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Branch(
                        rs.getInt("BranchID"),
                        rs.getString("BranchName"),
                        rs.getString("Area"),
                        rs.getString("Address"),
                        rs.getString("ContactPhone"),
                        rs.getString("Email"),
                        rs.getBoolean("IsActive")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("BranchDAO Error: " + e.getMessage());
        }
        return null;
    }
}