package com.medicare.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.medicare.util.DBConnection;

@WebServlet("/uploadReport")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 20     // 20MB
)
public class ReportUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"BRANCH_ADMIN".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String apptIdStr = req.getParameter("appointmentId");
        Part filePart = req.getPart("reportFile");

        if (apptIdStr == null || filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/admin?error=NoFileSelected");
            return;
        }

        int appointmentId = Integer.parseInt(apptIdStr);
        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String fileExt = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExt = originalFileName.substring(dotIndex).toLowerCase();
        }

        // Validate allowed file types: PDF or Images
        if (!fileExt.equals(".pdf") && !fileExt.equals(".png") && !fileExt.equals(".jpg") && !fileExt.equals(".jpeg")) {
            resp.sendRedirect(req.getContextPath() + "/admin?error=InvalidFileType");
            return;
        }

        // Generate clean unique filename: Report_ApptID_Timestamp.ext
        String savedFileName = "Report_" + appointmentId + "_" + System.currentTimeMillis() + fileExt;
        String uploadPath = req.getServletContext().getRealPath("/uploads/reports");
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fullSavePath = uploadPath + File.separator + savedFileName;
        filePart.write(fullSavePath);

        // Update database: set ReportStatus = 'AVAILABLE' and ReportFilePath
        String updateSql = "UPDATE dbo.Reports SET ReportStatus = 'AVAILABLE', ReportFilePath = ?, UploadedAt = GETDATE() WHERE AppointmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, "uploads/reports/" + savedFileName);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating report record: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin?reportUploaded=true");
    }
}