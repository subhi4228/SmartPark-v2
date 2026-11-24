package com.smartpark.model;

/**
 * Business contract for pricing discounts across park attractions.
 * Why: Centralizes discount policies (such as child concession) in one interface.
 */
public interface Discountable {
    int CHILD_AGE_LIMIT = 12;
    int CHILD_DISCOUNT_PERCENT = 50;

    // Time O(1), Space O(1)
    default int calculateDiscountedUnit(int unitPrice, int age) {
        if (age < CHILD_AGE_LIMIT) {
            return (unitPrice * (100 - CHILD_DISCOUNT_PERCENT)) / 100;
        }
        return unitPrice;
    }
}
