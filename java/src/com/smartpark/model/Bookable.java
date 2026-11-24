package com.smartpark.model;

/**
 * Common interface for entities that support capacity-constrained booking.
 * Why: Decouples capacity management rules from specific ride implementations.
 */
public interface Bookable {
    boolean book(int groupSize);
    void cancel(int groupSize);
    int getAvailableSeats();
}
