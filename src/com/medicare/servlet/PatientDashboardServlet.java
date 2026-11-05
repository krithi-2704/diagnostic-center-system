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
import com.medicare.model.Appointment;

@WebServlet("/dashboard")
public class PatientDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        int patientId = (Integer) session.getAttribute("userId");
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
        req.setAttribute("appointmentsList", appointments);

        req.getRequestDispatcher("/WEB-INF/views/patientDashboard.jsp").forward(req, resp);
    }
}