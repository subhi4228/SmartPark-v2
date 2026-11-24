package com.smartpark.model;

import com.smartpark.util.CsvConvertible;
import java.time.Instant;

/**
 * Represents a visitor booking record.
 * Structure choice: Stored in a doubly linked list (LinkedList<Booking>)
 * because history grows dynamically with frequent append and status cancellation operations.
 */
public class Booking implements CsvConvertible, Comparable<Booking> {
    private final long bookingId;
    private final String visitorName;
    private final int age;
    private final int rideId;
    private final int groupSize;
    private final int unitPrice;
    private final int discountPct;
    private final int totalAmount;
    private BookingStatus status;
    private final String createdAt;

    public Booking(long bookingId, String visitorName, int age, int rideId,
                   int groupSize, int unitPrice, int discountPct, int totalAmount,
                   BookingStatus status, String createdAt) {
        this.bookingId = bookingId;
        this.visitorName = visitorName;
        this.age = age;
        this.rideId = rideId;
        this.groupSize = groupSize;
        this.unitPrice = unitPrice;
        this.discountPct = discountPct;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = (createdAt != null) ? createdAt : Instant.now().toString();
    }

    public long getBookingId() { return bookingId; }
    public String getVisitorName() { return visitorName; }
    public int getAge() { return age; }
    public int getRideId() { return rideId; }
    public int getGroupSize() { return groupSize; }
    public int getUnitPrice() { return unitPrice; }
    public int getDiscountPct() { return discountPct; }
    public int getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }

    // Time O(1), Space O(1)
    @Override
    public String toCsvRow() {
        return String.format("%d,%s,%d,%d,%d,%d,%d,%d,%s,%s",
                bookingId, visitorName.replace(",", " "), age, rideId,
                groupSize, unitPrice, discountPct, totalAmount, status.name(), createdAt);
    }

    // Time O(1), Space O(1)
    public static Booking fromCsvRow(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 10) return null;
        long bid = Long.parseLong(parts[0].trim());
        String name = parts[1].trim();
        int age = Integer.parseInt(parts[2].trim());
        int rid = Integer.parseInt(parts[3].trim());
        int grp = Integer.parseInt(parts[4].trim());
        int uprice = Integer.parseInt(parts[5].trim());
        int disc = Integer.parseInt(parts[6].trim());
        int total = Integer.parseInt(parts[7].trim());
        BookingStatus st = BookingStatus.valueOf(parts[8].trim().toUpperCase());
        String ts = parts[9].trim();
        return new Booking(bid, name, age, rid, grp, uprice, disc, total, st, ts);
    }

    @Override
    public int compareTo(Booking other) {
        return Long.compare(this.bookingId, other.bookingId);
    }
}
