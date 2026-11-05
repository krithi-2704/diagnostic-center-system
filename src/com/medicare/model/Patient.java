package com.medicare.model;

public class Patient extends User {
    private static final long serialVersionUID = 1L;
    private String phoneNumber;
    private int age;
    private String gender;
    private String bloodGroup;
    private String address;

    public Patient() {
        super();
        setRole("PATIENT");
    }

    public Patient(int userId, String username, String password, String fullName, String email,
                   String phoneNumber, int age, String gender, String bloodGroup, String address) {
        super(userId, username, password, fullName, email, "PATIENT", null);
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[6-9][0-9]{9}$");
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}