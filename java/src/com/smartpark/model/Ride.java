package com.smartpark.model;

/**
 * Canonical model for a park ride.
 * Structure choice: Kept in a fixed Ride[] array because the catalogue is small
 * and fixed (4 to 10 rides), providing O(1) index access and optimal cache locality.
 */
public abstract class Ride implements Bookable, Discountable, Comparable<Ride> {
    private final int id;
    private final String name;
    private final int capacity;
    private int unitPrice;
    private final int minAge;
    private int bookedSeats;

    public Ride(int id, String name, int capacity, int unitPrice, int minAge) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.unitPrice = unitPrice;
        this.minAge = minAge;
        this.bookedSeats = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public int getMinAge() { return minAge; }
    public int getBookedSeats() { return bookedSeats; }

    // Time O(1), Space O(1)
    @Override
    public int getAvailableSeats() {
        return Math.max(0, capacity - bookedSeats);
    }

    // Time O(1), Space O(1)
    @Override
    public boolean book(int groupSize) {
        if (getAvailableSeats() >= groupSize) {
            bookedSeats += groupSize;
            return true;
        }
        return false;
    }

    // Time O(1), Space O(1)
    @Override
    public void cancel(int groupSize) {
        bookedSeats = Math.max(0, bookedSeats - groupSize);
    }

    public void setBookedSeats(int seats) {
        this.bookedSeats = seats;
    }

    // Polymorphic fare calculation overridden by specific ride types if special pricing applies
    public abstract int calculateFare(int groupSize, int visitorAge);

    // Time O(1), Space O(1)
    public String describe() {
        return String.format("%s (ID: %d) - Cap: %d, Fare: Rs. %d, Min Age: %d",
                name, id, capacity, unitPrice, minAge);
    }

    @Override
    public int compareTo(Ride other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return name + " (Rs. " + unitPrice + ")";
    }
}
