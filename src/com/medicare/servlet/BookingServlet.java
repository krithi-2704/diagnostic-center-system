package com.medicare.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.medicare.dao.AppointmentDAO;
import com.medicare.exception.BookingException;
import com.medicare.model.Appointment;
import com.medicare.model.AppointmentItem;
import com.medicare.model.User;
import com.medicare.util.EmailService;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BookingServlet.class.getName());
    private static final String CURRENT_USER = "currentUser";
    private static final String HOME_COLLECTION = "HOME_COLLECTION";
    private static final String PAY_AT_CENTER = "PAY_AT_CENTER";
    private static final Pattern CART_DELIMITER = Pattern.compile("::");

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(CURRENT_USER) == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=cart");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(CURRENT_USER) == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=cart");
            return;
        }

        User currentUser = (User) session.getAttribute(CURRENT_USER);
        int patientId = currentUser.getUserId();
        String patientEmail = currentUser.getEmail();
        String patientName = currentUser.getFullName();

        String branchIdStr = req.getParameter("branchId");
        String serviceType = req.getParameter("serviceType");
        String collectionAddress = req.getParameter("collectionAddress");
        String apptDateStr = req.getParameter("appointmentDate");
        String timeSlot = req.getParameter("timeSlot");
        String paymentMethod = req.getParameter("paymentMethod");
        String[] cartItems = req.getParameterValues("cartItem");

        try {
            if (branchIdStr == null || branchIdStr.trim().isEmpty()) {
                throw new BookingException("Please select a diagnostic center branch.");
            }
            if (apptDateStr == null || apptDateStr.trim().isEmpty()) {
                throw new BookingException("Please select an appointment date.");
            }
            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                throw new BookingException("Please select a preferred time slot.");
            }
            if (HOME_COLLECTION.equalsIgnoreCase(serviceType) && 
                (collectionAddress == null || collectionAddress.trim().isEmpty())) {
                throw new BookingException("Please provide a home collection address.");
            }

            int branchId = Integer.parseInt(branchIdStr.trim());
            Date apptDate = Date.valueOf(apptDateStr.trim());

            LocalDate selected = apptDate.toLocalDate();
            LocalDate today = LocalDate.now();
            if (selected.isBefore(today)) {
                throw new BookingException("Appointment date cannot be in the past.");
            }
            if (selected.isAfter(today.plusDays(14))) {
                throw new BookingException("Appointments are currently opened only for the next 14 days.");
            }

            double totalAmount = 0.0;
            List<AppointmentItem> items = new ArrayList<>();
            StringBuilder itemNamesSummary = new StringBuilder();

            if (cartItems != null && cartItems.length > 0) {
                for (String cartItem : cartItems) {
                    if (cartItem == null || cartItem.trim().isEmpty()) continue;
                    String[] parts = CART_DELIMITER.split(cartItem);
                    if (parts.length >= 4) {
                        String type = parts[0].trim();
                        int id = Integer.parseInt(parts[1].trim());
                        double price = Double.parseDouble(parts[2].trim());
                        String name = parts[3].trim();

                        AppointmentItem item = new AppointmentItem();
                        if ("PACKAGE".equalsIgnoreCase(type)) {
                            item.setPackageId(id);
                        } else {
                            item.setTestId(id);
                        }
                        item.setItemPrice(price);
                        item.setItemName(name);
                        items.add(item);
                        totalAmount += price;

                        if (itemNamesSummary.length() > 0) itemNamesSummary.append(", ");
                        itemNamesSummary.append(name);
                    }
                }
            }

            if (items.isEmpty()) {
                throw new BookingException("Your cart is empty. Please select at least one test or package.");
            }

            if (HOME_COLLECTION.equalsIgnoreCase(serviceType)) {
                totalAmount += 150.00;
            }

            Appointment appt = new Appointment();
            appt.setPatientId(patientId);
            appt.setBranchId(branchId);
            appt.setServiceType(serviceType != null ? serviceType : "LAB_VISIT");
            appt.setCollectionAddress(HOME_COLLECTION.equalsIgnoreCase(serviceType) ? collectionAddress.trim() : "MediCare Branch Visit");
            appt.setAppointmentDate(apptDate);
            appt.setTimeSlot(timeSlot.trim());
            appt.setTotalAmount(totalAmount);
            appt.setPaymentMethod(paymentMethod != null ? paymentMethod : PAY_AT_CENTER);
            appt.setItemBooked(itemNamesSummary.toString());

            // 1. Transactionally insert into Microsoft SQL Server
            Appointment booked = appointmentDAO.createCartBooking(appt, items, paymentMethod);
            if (booked == null) {
                throw new BookingException("Failed to generate appointment booking. Please try again.");
            }
            booked.setBranchName(getBranchNameById(branchId));
            booked.setItemBooked(itemNamesSummary.toString());
            booked.setPaymentMethod(paymentMethod != null ? paymentMethod : PAY_AT_CENTER);

            LOGGER.log(Level.INFO, "Booking created successfully in DB with Ref: {0}", booked.getBookingReference());

            // 2. Dispatch Live Confirmation Email (non-blocking)
            sendConfirmationEmailSafely(patientEmail, patientName, booked);

            // 3. Redirect to dashboard
            resp.sendRedirect(req.getContextPath() + "/dashboard?bookedRef=" + booked.getBookingReference());
        } catch (BookingException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Booking validation error: {0}", e.getMessage());
            String msg = (e.getMessage() != null && !e.getMessage().trim().isEmpty()) ? e.getMessage() : "Booking request was invalid.";
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=" + URLEncoder.encode(msg, "UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected booking error: " + e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=" + URLEncoder.encode("Booking failed due to an unexpected error.", "UTF-8"));
        }
    }

    private void sendConfirmationEmailSafely(String patientEmail, String patientName, Appointment booked) {
        try {
            LOGGER.log(Level.INFO, "Triggering EmailService for patient email: {0}", patientEmail);
            EmailService.sendBookingConfirmation(patientEmail, patientName, booked);
        } catch (Exception mailEx) {
            LOGGER.log(Level.WARNING, "Failed to dispatch confirmation email: {0}", mailEx.getMessage());
        }
    }

    private String getBranchNameById(int id) {
        switch (id) {
            case 1: return "MediCare - Shivajinagar";
            case 2: return "MediCare - Whitefield";
            case 3: return "MediCare - MG Road";
            case 4: return "MediCare - Kamanahalli";
            default: return "MediCare Bangalore Center";
        }
    }
}