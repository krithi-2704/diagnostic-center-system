package com.medicare.service;

import java.util.List;
import com.medicare.exception.BranchUnauthorizedException;
import com.medicare.model.Appointment;
import com.medicare.model.Patient;

public interface IBranchAdminService {
    List<Appointment> getBranchAppointments(int authenticatedBranchId) throws BranchUnauthorizedException;
    boolean updateAppointmentStatus(int appointmentId, String newStatus, int authenticatedBranchId) throws BranchUnauthorizedException;
    List<Patient> getBranchPatients(int authenticatedBranchId) throws BranchUnauthorizedException;
    int[] getBranchDashboardStatistics(int authenticatedBranchId) throws BranchUnauthorizedException;
}