package com.smartpark.exceptions;

public class AgeRestrictionException extends Exception {
    private final int visitorAge;
    private final int minAge;

    public AgeRestrictionException(int visitorAge, int minAge) {
        super(String.format("Visitor age %d is below the minimum required age of %d for this ride.",
                visitorAge, minAge));
        this.visitorAge = visitorAge;
        this.minAge = minAge;
    }

    public int getVisitorAge() { return visitorAge; }
    public int getMinAge() { return minAge; }
}
