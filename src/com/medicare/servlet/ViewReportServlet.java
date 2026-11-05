package com.medicare.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.medicare.util.DBConnection;

@WebServlet("/downloadReport")
public class ViewReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int loggedInUserId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");
        String apptIdStr = req.getParameter("appointmentId");

        if (apptIdStr == null || apptIdStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Appointment ID");
            return;
        }

        int appointmentId = Integer.parseInt(apptIdStr);
        String filePath = null;
        String bookingRef = "Report";

        // Security check: Patient can ONLY download their own report; Admin can download their branch's report
        String sql = "PATIENT".equalsIgnoreCase(role)
            ? "SELECT r.ReportFilePath, a.BookingReference FROM dbo.Reports r JOIN dbo.Appointments a ON r.AppointmentID = a.AppointmentID WHERE r.AppointmentID = ? AND a.PatientID = ? AND r.ReportStatus = 'AVAILABLE'"
            : "SELECT r.ReportFilePath, a.BookingReference FROM dbo.Reports r JOIN dbo.Appointments a ON r.AppointmentID = a.AppointmentID WHERE r.AppointmentID = ? AND a.BranchID = ? AND r.ReportStatus = 'AVAILABLE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            if ("PATIENT".equalsIgnoreCase(role)) {
                ps.setInt(2, loggedInUserId);
            } else {
                ps.setInt(2, (Integer) session.getAttribute("assignedBranchId"));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    filePath = rs.getString("ReportFilePath");
                    bookingRef = rs.getString("BookingReference");
                }
            }
        } catch (Exception e) {
            System.err.println("Report access error: " + e.getMessage());
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Report file not found or not yet uploaded by branch administrator.");
            return;
        }

        String appPath = req.getServletContext().getRealPath("");
        File downloadFile = new File(appPath + File.separator + filePath);

        if (!downloadFile.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical file not found on server.");
            return;
        }

        // Determine file extension
        String fileExt = ".pdf";
        int dot = downloadFile.getName().lastIndexOf('.');
        if (dot >= 0) {
            fileExt = downloadFile.getName().substring(dot);
        }

        String cleanDownloadName = "MediCare_Report_" + bookingRef + fileExt;

        // Force browser to open native Save / Download Dialog
        resp.setContentType("application/octet-stream");
        resp.setContentLength((int) downloadFile.length());
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + cleanDownloadName + "\"");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        try (FileInputStream inStream = new FileInputStream(downloadFile);
             OutputStream outStream = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
        }
    }
}