package com.smartpark.model;

/**
 * Encapsulates a reversible operation for LIFO undo stack.
 */
public class Action {
    public enum Type {
        BOOK_CONFIRMED,
        BOOK_WAITLISTED,
        CANCEL_BOOKING
    }

    private final Type type;
    private final Booking booking;
    private final Booking promotedBooking;

    public Action(Type type, Booking booking, Booking promotedBooking) {
        this.type = type;
        this.booking = booking;
        this.promotedBooking = promotedBooking;
    }

    public Type getType() { return type; }
    public Booking getBooking() { return booking; }
    public Booking getPromotedBooking() { return promotedBooking; }
}
