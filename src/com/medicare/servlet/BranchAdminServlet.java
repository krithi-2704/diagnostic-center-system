package com.medicare.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.medicare.dao.AppointmentDAO;
import com.medicare.dao.BranchDAO;
import com.medicare.exception.BranchUnauthorizedException;
import com.medicare.model.Appointment;
import com.medicare.model.Branch;
import com.medicare.model.SlotAvailability;

@WebServlet("/admin")
public class BranchAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private BranchDAO branchDAO = new BranchDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null ||
            !"BRANCH_ADMIN".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer branchId = (Integer) session.getAttribute("assignedBranchId");
        if (branchId == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No assigned branch found.");
            return;
        }

        try {
            Branch branch = branchDAO.getBranchById(branchId);
            List<Appointment> branchAppts = appointmentDAO.getAppointmentsByBranchId(branchId);
            List<SlotAvailability> branchSlots = appointmentDAO.getBranchSlotsForAdmin(branchId);
            int[] stats = appointmentDAO.getBranchStatistics(branchId);

            req.setAttribute("assignedBranch", branch);
            req.setAttribute("appointmentsList", branchAppts);
            req.setAttribute("branchSlots", branchSlots);
            req.setAttribute("stats", stats);

            req.getRequestDispatcher("/WEB-INF/views/adminDashboard.jsp").forward(req, resp);
        } catch (BranchUnauthorizedException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"BRANCH_ADMIN".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer branchId = (Integer) session.getAttribute("assignedBranchId");
        String action = req.getParameter("action");

        if ("updateSlot".equalsIgnoreCase(action)) {
            String timeSlot = req.getParameter("timeSlot");
            int capacity = Integer.parseInt(req.getParameter("capacity"));
            boolean isActive = "on".equalsIgnoreCase(req.getParameter("isActive")) || "true".equalsIgnoreCase(req.getParameter("isActive"));
            appointmentDAO.updateSlotCapacity(branchId, timeSlot, capacity, isActive);
        } else {
            // Update appointment status and report
            String apptIdStr = req.getParameter("appointmentId");
            String status = req.getParameter("status");
            String reportStatus = req.getParameter("reportStatus");
            String reportFile = req.getParameter("reportFile");

            if (apptIdStr != null && status != null) {
                int apptId = Integer.parseInt(apptIdStr);
                appointmentDAO.updateAppointmentAndReport(apptId, status, reportStatus, reportFile, branchId);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin?updated=true");
    }
}