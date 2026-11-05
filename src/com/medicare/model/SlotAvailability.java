package com.medicare.model;

import java.io.Serializable;

public class SlotAvailability implements Serializable {
    private static final long serialVersionUID = 1L;
    private String timeSlot;
    private int maxCapacity;
    private int currentBookings;
    private boolean available;

    public SlotAvailability() {}

    public SlotAvailability(String timeSlot, int maxCapacity, int currentBookings) {
        this.timeSlot = timeSlot;
        this.maxCapacity = maxCapacity;
        this.currentBookings = currentBookings;
        this.available = (currentBookings < maxCapacity);
    }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getCurrentBookings() { return currentBookings; }
    public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }
    public boolean isAvailable() { return (currentBookings < maxCapacity); }
    public void setAvailable(boolean available) { this.available = available; }
    public int getRemainingSlots() { return Math.max(0, maxCapacity - currentBookings); }
}