package com.medicare.service;

import java.sql.Date;
import java.util.List;
import com.medicare.exception.BookingException;
import com.medicare.exception.InvalidInputException;
import com.medicare.model.Appointment;

public interface IAppointmentService {
    Appointment bookAppointment(Appointment appointment, String paymentMethod) throws BookingException, InvalidInputException;
    List<Appointment> getAppointmentsByPatientId(int patientId);
    Appointment getAppointmentById(int appointmentId);
    boolean cancelAppointment(int appointmentId, int patientId) throws BookingException;
    boolean rescheduleAppointment(int appointmentId, int patientId, Date newDate, String newSlot) throws BookingException;
}