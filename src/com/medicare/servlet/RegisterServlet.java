package com.medicare.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.medicare.dao.UserDAO;
import com.medicare.exception.InvalidInputException;
import com.medicare.model.Patient;
import com.medicare.model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = req.getParameter("role"); // "PATIENT" or "BRANCH_ADMIN"
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (name == null || name.trim().isEmpty() ||
            email == null || !User.isValidEmail(email) ||
            password == null || !User.isValidPassword(password) ||
            !password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Validation error: Check that all required fields are filled and password has 6+ chars with uppercase, lowercase, number, and special character.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
                String branchIdStr = req.getParameter("branchId");
                String adminCode = req.getParameter("adminPasscode");

                if (!"MEDICARE_ADMIN_2026".equals(adminCode)) {
                    req.setAttribute("errorMessage", "Invalid Administrator Authorization Passcode.");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }

                int branchId = Integer.parseInt(branchIdStr);
                User admin = new User(0, email.trim().toLowerCase(), password, name.trim(), email.trim().toLowerCase(), "BRANCH_ADMIN", branchId);
                userDAO.registerBranchAdmin(admin, branchId);
                resp.sendRedirect(req.getContextPath() + "/login?registered=true&role=admin");
            } else {
                String phone = req.getParameter("phone");
                String ageStr = req.getParameter("age");
                String gender = req.getParameter("gender");
                String bloodGroup = req.getParameter("bloodGroup");
                String address = req.getParameter("address");

                if (phone == null || !Patient.isValidPhone(phone)) {
                    req.setAttribute("errorMessage", "Please provide a valid 10-digit Indian phone number starting with 6-9.");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }

                int age = 25;
                try { if (ageStr != null) age = Integer.parseInt(ageStr.trim()); } catch (NumberFormatException ignored) {}

                Patient patient = new Patient(0, email.trim().toLowerCase(), password, name.trim(), email.trim().toLowerCase(),
                                              phone.trim(), age, gender, bloodGroup, address != null ? address.trim() : "Bangalore");
                userDAO.registerPatient(patient);
                resp.sendRedirect(req.getContextPath() + "/login?registered=true&role=patient");
            }
        } catch (InvalidInputException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Unexpected error: " + e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }
}