package com.medicare.model;

import java.io.Serializable;

public class AppointmentItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int itemId;
    private int appointmentId;
    private Integer testId;
    private Integer packageId;
    private double itemPrice;
    private String itemName;

    public AppointmentItem() {}

    public AppointmentItem(int itemId, int appointmentId, Integer testId, Integer packageId, double itemPrice, String itemName) {
        this.itemId = itemId;
        this.appointmentId = appointmentId;
        this.testId = testId;
        this.packageId = packageId;
        this.itemPrice = itemPrice;
        this.itemName = itemName;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }
    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }
    public double getItemPrice() { return itemPrice; }
    public void setItemPrice(double itemPrice) { this.itemPrice = itemPrice; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}