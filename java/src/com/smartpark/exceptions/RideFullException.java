package com.smartpark.exceptions;

public class RideFullException extends Exception {
    private final int rideId;
    private final int requested;
    private final int available;

    public RideFullException(int rideId, int requested, int available) {
        super(String.format("Ride %d is full. Requested %d seats, but only %d available.",
                rideId, requested, available));
        this.rideId = rideId;
        this.requested = requested;
        this.available = available;
    }

    public int getRideId() { return rideId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
