package com.medicare.model;

import java.io.Serializable;

public class DiagnosticTest implements Serializable {
    private static final long serialVersionUID = 1L;
    private int testId;
    private String testCode;
    private String testName;
    private String category;
    private double price;
    private String preparationInstructions;
    private String reportTurnaroundTime;
    private String imageUrl;
    private boolean isActive;

    public DiagnosticTest() {}

    // 8-parameter constructor for Database / DAO mappings
    public DiagnosticTest(int testId, String testCode, String testName, String category, double price,
                          String preparationInstructions, String reportTurnaroundTime, boolean isActive) {
        this.testId = testId;
        this.testCode = testCode;
        this.testName = testName;
        this.category = category;
        this.price = price;
        this.preparationInstructions = preparationInstructions;
        this.reportTurnaroundTime = reportTurnaroundTime;
        this.imageUrl = "https://images.unsplash.com/photo-1579154204601-01588f351e67?auto=format&fit=crop&w=600&q=80";
        this.isActive = isActive;
    }

    // 9-parameter constructor with explicit imageUrl
    public DiagnosticTest(int testId, String testCode, String testName, String category, double price,
                          String preparationInstructions, String reportTurnaroundTime, String imageUrl, boolean isActive) {
        this.testId = testId;
        this.testCode = testCode;
        this.testName = testName;
        this.category = category;
        this.price = price;
        this.preparationInstructions = preparationInstructions;
        this.reportTurnaroundTime = reportTurnaroundTime;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getPreparationInstructions() { return preparationInstructions; }
    public void setPreparationInstructions(String preparationInstructions) { this.preparationInstructions = preparationInstructions; }
    public String getReportTurnaroundTime() { return reportTurnaroundTime; }
    public void setReportTurnaroundTime(String reportTurnaroundTime) { this.reportTurnaroundTime = reportTurnaroundTime; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}