package com.medicare.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.medicare.dao.AppointmentDAO;
import com.medicare.exception.BookingException;

@WebServlet("/cancel")
public class CancelAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int patientId = (Integer) session.getAttribute("userId");
        String apptIdStr = req.getParameter("appointmentId");

        try {
            int apptId = Integer.parseInt(apptIdStr);
            appointmentDAO.cancelAppointment(apptId, patientId);
            resp.sendRedirect(req.getContextPath() + "/dashboard?cancelled=true");
        } catch (BookingException | NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=" + e.getMessage());
        }
    }
}