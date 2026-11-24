package com.smartpark.model;

/**
 * Family-friendly attraction with gentle dynamics and low age barrier.
 */
public class FamilyRide extends Ride {
    public FamilyRide(int id, String name, int capacity, int unitPrice, int minAge) {
        super(id, name, capacity, unitPrice, minAge);
    }

    // Time O(1), Space O(1)
    @Override
    public int calculateFare(int groupSize, int visitorAge) {
        int unit = calculateDiscountedUnit(getUnitPrice(), visitorAge);
        return unit * groupSize;
    }
}
