package com.medicare.service;

import java.util.List;
import com.medicare.exception.BranchUnauthorizedException;
import com.medicare.model.Report;

public interface IReportService {
    List<Report> getReportsForPatient(int patientId);
    Report getReportById(int reportId, int patientId);
    boolean updateReportStatus(int reportId, String status, String filePath, int adminId, int authenticatedBranchId) throws BranchUnauthorizedException;
}