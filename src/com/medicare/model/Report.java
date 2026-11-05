package com.medicare.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    private int reportId;
    private int appointmentId;
    private String reportStatus; // "PENDING", "PROCESSING", "AVAILABLE"
    private String reportFilePath;
    private Integer uploadedByAdminId;
    private Timestamp uploadedAt;

    private String bookingReference;
    private String patientName;
    private String testName;

    public Report() {}

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getReportStatus() { return reportStatus; }
    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }
    public String getReportFilePath() { return reportFilePath; }
    public void setReportFilePath(String reportFilePath) { this.reportFilePath = reportFilePath; }
    public Integer getUploadedByAdminId() { return uploadedByAdminId; }
    public void setUploadedByAdminId(Integer uploadedByAdminId) { this.uploadedByAdminId = uploadedByAdminId; }
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
}