package com.medicare.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.medicare.exception.BookingException;
import com.medicare.exception.BranchUnauthorizedException;
import com.medicare.model.Appointment;
import com.medicare.model.AppointmentItem;
import com.medicare.model.SlotAvailability;
import com.medicare.util.DBConnection;

public class AppointmentDAO {

    public List<SlotAvailability> getAvailableSlots(int branchId, Date appointmentDate) {
        List<SlotAvailability> slots = new ArrayList<>();
        String sql = "SELECT c.TimeSlot, c.MaxCapacity, " +
                     "COALESCE(COUNT(a.AppointmentID), 0) AS BookedCount " +
                     "FROM dbo.BranchSlotCapacity c " +
                     "LEFT JOIN dbo.Appointments a ON c.BranchID = a.BranchID " +
                     "   AND c.TimeSlot = a.TimeSlot " +
                     "   AND a.AppointmentDate = ? " +
                     "   AND a.Status != 'CANCELLED' " +
                     "WHERE c.BranchID = ? AND c.IsActive = 1 " +
                     "GROUP BY c.TimeSlot, c.MaxCapacity " +
                     "ORDER BY c.TimeSlot";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, appointmentDate);
            ps.setInt(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String slotTime = rs.getString("TimeSlot");
                    int maxCap = rs.getInt("MaxCapacity");
                    int booked = rs.getInt("BookedCount");
                    slots.add(new SlotAvailability(slotTime, maxCap, booked));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available slots: " + e.getMessage());
        }
        return slots;
    }

    public Appointment createCartBooking(Appointment appt, List<AppointmentItem> items, String paymentMethod) throws BookingException {
        String bookingRef = "MCB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String insertApptSql = "INSERT INTO dbo.Appointments (BookingReference, PatientID, BranchID, ServiceType, CollectionAddress, AppointmentDate, TimeSlot, TotalAmount, Status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
        String insertItemSql = "INSERT INTO dbo.AppointmentItems (AppointmentID, TestID, PackageID, ItemPrice, ItemName) VALUES (?, ?, ?, ?, ?)";
        String insertPaySql = "INSERT INTO dbo.Payments (TransactionID, AppointmentID, PaymentMethod, AmountPaid, PaymentStatus) VALUES (?, ?, ?, ?, ?)";
        String insertReportSql = "INSERT INTO dbo.Reports (AppointmentID, ReportStatus) VALUES (?, 'PENDING')";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedApptId = 0;
            try (PreparedStatement psAppt = conn.prepareStatement(insertApptSql, Statement.RETURN_GENERATED_KEYS)) {
                psAppt.setString(1, bookingRef);
                psAppt.setInt(2, appt.getPatientId());
                psAppt.setInt(3, appt.getBranchId());
                psAppt.setString(4, appt.getServiceType());
                psAppt.setString(5, appt.getCollectionAddress());
                psAppt.setDate(6, appt.getAppointmentDate());
                psAppt.setString(7, appt.getTimeSlot());
                psAppt.setDouble(8, appt.getTotalAmount());
                psAppt.executeUpdate();

                try (ResultSet rs = psAppt.getGeneratedKeys()) {
                    if (rs.next()) generatedApptId = rs.getInt(1);
                    else throw new SQLException("Failed to obtain Appointment ID.");
                }
            }

            if (items != null && !items.isEmpty()) {
                try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                    for (AppointmentItem item : items) {
                        psItem.setInt(1, generatedApptId);
                        if (item.getTestId() != null && item.getTestId() > 0) psItem.setInt(2, item.getTestId());
                        else psItem.setNull(2, Types.INTEGER);
                        if (item.getPackageId() != null && item.getPackageId() > 0) psItem.setInt(3, item.getPackageId());
                        else psItem.setNull(3, Types.INTEGER);
                        psItem.setDouble(4, item.getItemPrice());
                        psItem.setString(5, item.getItemName());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }
            }

            String payStatus = "PAY_AT_CENTER".equalsIgnoreCase(paymentMethod) ? "PENDING" : "SUCCESS";
            String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            try (PreparedStatement psPay = conn.prepareStatement(insertPaySql)) {
                psPay.setString(1, txnId);
                psPay.setInt(2, generatedApptId);
                psPay.setString(3, paymentMethod != null ? paymentMethod : "UPI");
                psPay.setDouble(4, appt.getTotalAmount());
                psPay.setString(5, payStatus);
                psPay.executeUpdate();
            }

            try (PreparedStatement psRep = conn.prepareStatement(insertReportSql)) {
                psRep.setInt(1, generatedApptId);
                psRep.executeUpdate();
            }

            conn.commit();
            appt.setAppointmentId(generatedApptId);
            appt.setBookingReference(bookingRef);
            appt.setStatus("CONFIRMED");
            return appt;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            throw new BookingException("Booking failed: " + e.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {} }
        }
    }

    public List<Appointment> getAppointmentsByPatientId(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.BookingReference, a.PatientID, a.BranchID, " +
                     "a.ServiceType, a.CollectionAddress, a.AppointmentDate, a.TimeSlot, a.TotalAmount, a.Status, a.CreatedAt, " +
                     "b.BranchName, p.PaymentMethod, p.PaymentStatus, r.ReportStatus, r.ReportFilePath, " +
                     "COALESCE((SELECT STRING_AGG(ItemName, ', ') FROM dbo.AppointmentItems WHERE AppointmentID = a.AppointmentID), 'Diagnostic Tests') AS ItemBooked, " +
                     "CASE WHEN EXISTS (SELECT 1 FROM dbo.AppointmentItems ai JOIN dbo.DiagnosticTests dt ON ai.TestID = dt.TestID WHERE ai.AppointmentID = a.AppointmentID AND dt.IsHomeCollectionAllowed = 0) THEN 1 ELSE 0 END AS HasImaging " +
                     "FROM dbo.Appointments a " +
                     "JOIN dbo.Branches b ON a.BranchID = b.BranchID " +
                     "LEFT JOIN dbo.Payments p ON a.AppointmentID = p.AppointmentID " +
                     "LEFT JOIN dbo.Reports r ON a.AppointmentID = r.AppointmentID " +
                     "WHERE a.PatientID = ? ORDER BY a.AppointmentDate DESC, a.AppointmentID DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient appointments: " + e.getMessage());
        }
        return list;
    }

    public boolean cancelAppointment(int appointmentId, int patientId) throws BookingException {
        String checkSql = "SELECT a.Status, p.PaymentMethod FROM dbo.Appointments a " +
                          "LEFT JOIN dbo.Payments p ON a.AppointmentID = p.AppointmentID WHERE a.AppointmentID = ? AND a.PatientID = ?";
        String updateApptSql = "UPDATE dbo.Appointments SET Status = 'CANCELLED' WHERE AppointmentID = ?";
        String updatePaySql = "UPDATE dbo.Payments SET PaymentStatus = ? WHERE AppointmentID = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String currentStatus = "";
            String payMethod = "";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, appointmentId);
                psCheck.setInt(2, patientId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("Status");
                        payMethod = rs.getString("PaymentMethod");
                    } else {
                        throw new BookingException("Appointment not found or unauthorized.");
                    }
                }
            }

            if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
                throw new BookingException("Cannot cancel an appointment that has already been completed.");
            }
            if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                throw new BookingException("This appointment is already cancelled.");
            }

            try (PreparedStatement psUp = conn.prepareStatement(updateApptSql)) {
                psUp.setInt(1, appointmentId);
                psUp.executeUpdate();
            }

            String newPayStatus = "PAY_AT_CENTER".equalsIgnoreCase(payMethod) ? "NO_REFUND_REQUIRED" : "REFUND_INITIATED";
            try (PreparedStatement psPay = conn.prepareStatement(updatePaySql)) {
                psPay.setString(1, newPayStatus);
                psPay.setInt(2, appointmentId);
                psPay.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            throw new BookingException("Cancellation failed: " + e.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {} }
        }
    }

    public List<Appointment> getAppointmentsByBranchId(int authenticatedBranchId) throws BranchUnauthorizedException {
        if (authenticatedBranchId <= 0) throw new BranchUnauthorizedException("Unauthorized branch.");

        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.BookingReference, a.PatientID, a.BranchID, " +
                     "a.ServiceType, a.CollectionAddress, a.AppointmentDate, a.TimeSlot, a.TotalAmount, a.Status, a.CreatedAt, " +
                     "u.FullName AS PatientName, pat.PhoneNumber AS PatientPhone, b.BranchName, " +
                     "p.PaymentMethod, p.PaymentStatus, r.ReportStatus, r.ReportFilePath, " +
                     "COALESCE((SELECT STRING_AGG(ItemName, ', ') FROM dbo.AppointmentItems WHERE AppointmentID = a.AppointmentID), 'Diagnostic Tests') AS ItemBooked, " +
                     "CASE WHEN EXISTS (SELECT 1 FROM dbo.AppointmentItems ai JOIN dbo.DiagnosticTests dt ON ai.TestID = dt.TestID WHERE ai.AppointmentID = a.AppointmentID AND dt.IsHomeCollectionAllowed = 0) THEN 1 ELSE 0 END AS HasImaging " +
                     "FROM dbo.Appointments a " +
                     "JOIN dbo.Users u ON a.PatientID = u.UserID " +
                     "JOIN dbo.Patients pat ON u.UserID = pat.PatientID " +
                     "JOIN dbo.Branches b ON a.BranchID = b.BranchID " +
                     "LEFT JOIN dbo.Payments p ON a.AppointmentID = p.AppointmentID " +
                     "LEFT JOIN dbo.Reports r ON a.AppointmentID = r.AppointmentID " +
                     "WHERE a.BranchID = ? ORDER BY a.AppointmentDate DESC, a.AppointmentID DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authenticatedBranchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = mapAppointment(rs);
                    appt.setPatientName(rs.getString("PatientName"));
                    appt.setPatientPhone(rs.getString("PatientPhone"));
                    list.add(appt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching branch appointments: " + e.getMessage());
        }
        return list;
    }

    public boolean updateAppointmentAndReport(int appointmentId, String status, String reportStatus, String reportPath, int authenticatedBranchId) {
        String apptSql = "UPDATE dbo.Appointments SET Status = ? WHERE AppointmentID = ? AND BranchID = ?";
        String repSql = "UPDATE dbo.Reports SET ReportStatus = ?, ReportFilePath = ?, UploadedAt = GETDATE() WHERE AppointmentID = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(apptSql)) {
                ps1.setString(1, status);
                ps1.setInt(2, appointmentId);
                ps1.setInt(3, authenticatedBranchId);
                ps1.executeUpdate();
            }

            if (reportStatus != null && !reportStatus.isEmpty()) {
                try (PreparedStatement ps2 = conn.prepareStatement(repSql)) {
                    ps2.setString(1, reportStatus);
                    ps2.setString(2, reportPath != null ? reportPath : "Report_Verified_" + appointmentId + ".pdf");
                    ps2.setInt(3, appointmentId);
                    ps2.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {} }
        }
    }

    public List<SlotAvailability> getBranchSlotsForAdmin(int branchId) {
        List<SlotAvailability> list = new ArrayList<>();
        String sql = "SELECT TimeSlot, MaxCapacity, IsActive FROM dbo.BranchSlotCapacity WHERE BranchID = ? ORDER BY TimeSlot";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotAvailability s = new SlotAvailability();
                    s.setTimeSlot(rs.getString("TimeSlot"));
                    s.setMaxCapacity(rs.getInt("MaxCapacity"));
                    s.setAvailable(rs.getBoolean("IsActive"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching branch slots: " + e.getMessage());
        }
        return list;
    }

    public boolean updateSlotCapacity(int branchId, String timeSlot, int newCapacity, boolean isActive) {
        String sql = "UPDATE dbo.BranchSlotCapacity SET MaxCapacity = ?, IsActive = ? WHERE BranchID = ? AND TimeSlot = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCapacity);
            ps.setBoolean(2, isActive);
            ps.setInt(3, branchId);
            ps.setString(4, timeSlot);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public int[] getBranchStatistics(int authenticatedBranchId) {
        int[] stats = new int[5];
        String sql = "SELECT COUNT(*) AS TotalAppts, " +
                     "SUM(CASE WHEN ServiceType = 'LAB_VISIT' THEN 1 ELSE 0 END) AS LabVisits, " +
                     "SUM(CASE WHEN ServiceType = 'HOME_COLLECTION' THEN 1 ELSE 0 END) AS HomeCols, " +
                     "SUM(CASE WHEN Status = 'COMPLETED' THEN 1 ELSE 0 END) AS Completed " +
                     "FROM dbo.Appointments WHERE BranchID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authenticatedBranchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("TotalAppts");
                    stats[1] = rs.getInt("LabVisits");
                    stats[2] = rs.getInt("HomeCols");
                    stats[3] = rs.getInt("Completed");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating stats: " + e.getMessage());
        }
        return stats;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("AppointmentID"));
        a.setBookingReference(rs.getString("BookingReference"));
        a.setPatientId(rs.getInt("PatientID"));
        a.setBranchId(rs.getInt("BranchID"));
        a.setServiceType(rs.getString("ServiceType"));
        a.setCollectionAddress(rs.getString("CollectionAddress"));
        a.setAppointmentDate(rs.getDate("AppointmentDate"));
        a.setTimeSlot(rs.getString("TimeSlot"));
        a.setTotalAmount(rs.getDouble("TotalAmount"));
        a.setStatus(rs.getString("Status"));
        a.setCreatedAt(rs.getTimestamp("CreatedAt"));
        a.setBranchName(rs.getString("BranchName"));
        a.setItemBooked(rs.getString("ItemBooked"));
        a.setPaymentMethod(rs.getString("PaymentMethod"));
        a.setPaymentStatus(rs.getString("PaymentStatus"));
        a.setReportStatus(rs.getString("ReportStatus"));
        a.setReportFilePath(rs.getString("ReportFilePath"));
        a.setContainsImaging(rs.getInt("HasImaging") == 1);
        return a;
    }
}