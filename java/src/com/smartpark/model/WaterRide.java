package com.smartpark.model;

/**
 * Splash water flume ride requiring moderate age restrictions.
 */
public class WaterRide extends Ride {
    public WaterRide(int id, String name, int capacity, int unitPrice, int minAge) {
        super(id, name, capacity, unitPrice, minAge);
    }

    // Time O(1), Space O(1)
    @Override
    public int calculateFare(int groupSize, int visitorAge) {
        int unit = calculateDiscountedUnit(getUnitPrice(), visitorAge);
        return unit * groupSize;
    }
}
