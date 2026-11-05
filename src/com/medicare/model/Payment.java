package com.medicare.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    private int paymentId;
    private String transactionId;
    private int appointmentId;
    private String paymentMethod; // "UPI", "CARD", "NET_BANKING"
    private double amountPaid;
    private String paymentStatus; // "SUCCESS", "FAILED", "PENDING"
    private Timestamp paymentDate;

    public Payment() {}

    public Payment(int paymentId, String transactionId, int appointmentId, String paymentMethod, double amountPaid, String paymentStatus, Timestamp paymentDate) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.appointmentId = appointmentId;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
}