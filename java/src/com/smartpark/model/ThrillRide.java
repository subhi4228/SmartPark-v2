package com.smartpark.model;

/**
 * High-speed roller coaster attraction with strict height and age safety limits.
 */
public class ThrillRide extends Ride {
    public ThrillRide(int id, String name, int capacity, int unitPrice, int minAge) {
        super(id, name, capacity, unitPrice, minAge);
    }

    // Time O(1), Space O(1)
    @Override
    public int calculateFare(int groupSize, int visitorAge) {
        int unit = calculateDiscountedUnit(getUnitPrice(), visitorAge);
        return unit * groupSize;
    }
}
