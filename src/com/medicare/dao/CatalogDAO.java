package com.medicare.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.medicare.model.DiagnosticTest;
import com.medicare.model.Package;
import com.medicare.util.DBConnection;

public class CatalogDAO {

    public List<DiagnosticTest> getAllTests() {
        List<DiagnosticTest> list = new ArrayList<>();
        String sql = "SELECT TestID, TestCode, TestName, Category, Price, PreparationInstructions, ReportTurnaroundTime, IsActive " +
                     "FROM dbo.DiagnosticTests WHERE IsActive = 1 ORDER BY Category, TestName";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new DiagnosticTest(
                    rs.getInt("TestID"),
                    rs.getString("TestCode"),
                    rs.getString("TestName"),
                    rs.getString("Category"),
                    rs.getDouble("Price"),
                    rs.getString("PreparationInstructions"),
                    rs.getString("ReportTurnaroundTime"),
                    rs.getBoolean("IsActive")
                ));
            }
        } catch (SQLException e) {
            System.err.println("CatalogDAO Tests Error: " + e.getMessage());
        }
        return list;
    }

    public List<Package> getAllPackages() {
        List<Package> list = new ArrayList<>();
        String sql = "SELECT PackageID, PackageCode, PackageName, Description, Price, DiscountPercentage, IsActive " +
                     "FROM dbo.Packages WHERE IsActive = 1 ORDER BY PackageName";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Package(
                    rs.getInt("PackageID"),
                    rs.getString("PackageCode"),
                    rs.getString("PackageName"),
                    rs.getString("Description"),
                    rs.getDouble("Price"),
                    rs.getDouble("DiscountPercentage"),
                    rs.getBoolean("IsActive")
                ));
            }
        } catch (SQLException e) {
            System.err.println("CatalogDAO Packages Error: " + e.getMessage());
        }
        return list;
    }
}