package com.medicare.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;
    private int appointmentId;
    private String bookingReference;
    private int patientId;
    private int branchId;
    private Integer testId;
    private Integer packageId;
    private String serviceType; // "LAB_VISIT" or "HOME_COLLECTION"
    private String collectionAddress;
    private Date appointmentDate;
    private String timeSlot;
    private double totalAmount;
    private String status; // "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    private Timestamp createdAt;

    private String patientName;
    private String patientPhone;
    private String branchName;
    private String itemBooked;
    private String paymentMethod;
    private String paymentStatus;
    private boolean containsImaging;
    private String reportStatus;
    private String reportFilePath;
    private List<AppointmentItem> items = new ArrayList<>();

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }
    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getCollectionAddress() { return collectionAddress; }
    public void setCollectionAddress(String collectionAddress) { this.collectionAddress = collectionAddress; }
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getItemBooked() { return itemBooked; }
    public void setItemBooked(String itemBooked) { this.itemBooked = itemBooked; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public boolean isContainsImaging() { return containsImaging; }
    public void setContainsImaging(boolean containsImaging) { this.containsImaging = containsImaging; }
    public String getReportStatus() { return reportStatus; }
    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }
    public String getReportFilePath() { return reportFilePath; }
    public void setReportFilePath(String reportFilePath) { this.reportFilePath = reportFilePath; }

    public List<AppointmentItem> getItems() { return items; }
    public void setItems(List<AppointmentItem> items) { this.items = items; }
    public void addItem(AppointmentItem item) { this.items.add(item); }
}