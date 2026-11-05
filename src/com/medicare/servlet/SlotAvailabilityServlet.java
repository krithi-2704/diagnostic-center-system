package com.medicare.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.medicare.dao.AppointmentDAO;
import com.medicare.model.SlotAvailability;

@WebServlet("/api/slots")
public class SlotAvailabilityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String branchIdStr = req.getParameter("branchId");
        String dateStr = req.getParameter("date");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (branchIdStr == null || dateStr == null || branchIdStr.isEmpty() || dateStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Missing branchId or date parameter\"}");
            return;
        }

        try {
            int branchId = Integer.parseInt(branchIdStr);
            Date apptDate = Date.valueOf(dateStr);

            List<SlotAvailability> slots = appointmentDAO.getAvailableSlots(branchId, apptDate);
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < slots.size(); i++) {
                SlotAvailability s = slots.get(i);
                json.append("{")
                    .append("\"timeSlot\":\"").append(s.getTimeSlot()).append("\",")
                    .append("\"maxCapacity\":").append(s.getMaxCapacity()).append(",")
                    .append("\"currentBookings\":").append(s.getCurrentBookings()).append(",")
                    .append("\"available\":").append(s.isAvailable()).append(",")
                    .append("\"remaining\":").append(s.getRemainingSlots())
                    .append("}");
                if (i < slots.size() - 1) json.append(",");
            }
            json.append("]");

            out.write(json.toString());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Invalid date or branch ID format\"}");
        }
    }
}