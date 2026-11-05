package com.medicare.model;

import java.io.Serializable;

public class Package implements Serializable {
    private static final long serialVersionUID = 1L;
    private int packageId;
    private String packageCode;
    private String packageName;
    private String description;
    private double price;
    private double discountPercentage;
    private boolean isActive;

    public Package() {}

    public Package(int packageId, String packageCode, String packageName, String description, double price, double discountPercentage, boolean isActive) {
        this.packageId = packageId;
        this.packageCode = packageCode;
        this.packageName = packageName;
        this.description = description;
        this.price = price;
        this.discountPercentage = discountPercentage;
        this.isActive = isActive;
    }

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}