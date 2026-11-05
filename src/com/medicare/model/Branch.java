package com.medicare.model;

import java.io.Serializable;

public class Branch implements Serializable {
    private static final long serialVersionUID = 1L;
    private int branchId;
    private String branchName;
    private String area;
    private String address;
    private String contactPhone;
    private String email;
    private boolean isActive;

    public Branch() {}

    public Branch(int branchId, String branchName, String area, String address, String contactPhone, String email, boolean isActive) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.area = area;
        this.address = address;
        this.contactPhone = contactPhone;
        this.email = email;
        this.isActive = isActive;
    }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}