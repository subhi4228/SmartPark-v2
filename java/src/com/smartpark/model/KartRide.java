package com.smartpark.model;

/**
 * Motorized go-kart circuit requiring higher minimum age for track safety.
 */
public class KartRide extends Ride {
    public KartRide(int id, String name, int capacity, int unitPrice, int minAge) {
        super(id, name, capacity, unitPrice, minAge);
    }

    // Time O(1), Space O(1)
    @Override
    public int calculateFare(int groupSize, int visitorAge) {
        int unit = calculateDiscountedUnit(getUnitPrice(), visitorAge);
        return unit * groupSize;
    }
}
