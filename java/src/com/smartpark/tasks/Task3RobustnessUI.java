package com.smartpark.tasks;

import com.smartpark.exceptions.*;
import com.smartpark.model.Booking;
import com.smartpark.service.ParkService;

/**
 * Task 3 (CO3) - Robustness, Custom Checked Exceptions and Data Structures.
 * Demonstrates:
 * - Custom checked exceptions: RideFullException, AgeRestrictionException, InvalidVisitorException
 * - Strict single-structure data flow: Ride[], LinkedList, ArrayDeque queue/stack, HashMap
 */
public class Task3RobustnessUI {
    public static void main(String[] args) {
        System.out.println("Executing Task 3 (CO3) Robustness & Exception Handling Demo...\n");
        ParkService service = new ParkService();

        // 1. Valid Booking
        try {
            Booking b = service.bookTicket("Aarav Sharma", 14, 101, 4);
            System.out.println("[PASS] Booked successfully: ID " + b.getBookingId() + " for " + b.getVisitorName());
        } catch (Exception e) {
            System.out.println("[FAIL] " + e.getMessage());
        }

        // 2. Age Restriction Violation
        try {
            System.out.println("\nAttempting booking for Age 7 on Go-Kart (Min age 12)...");
            service.bookTicket("Chintu Patel", 7, 104, 2);
            System.out.println("[FAIL] Should have thrown AgeRestrictionException");
        } catch (AgeRestrictionException e) {
            System.out.println("[PASS] Caught expected AgeRestrictionException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[FAIL] Unexpected error: " + e.getMessage());
        }

        // 3. Invalid Name Validation
        try {
            System.out.println("\nAttempting booking with invalid numerical name 'User123'...");
            service.bookTicket("User123", 22, 101, 2);
            System.out.println("[FAIL] Should have thrown InvalidVisitorException");
        } catch (InvalidVisitorException e) {
            System.out.println("[PASS] Caught expected InvalidVisitorException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[FAIL] Unexpected error: " + e.getMessage());
        }

        // 4. Ride Full & Waitlist
        try {
            System.out.println("\nAttempting booking exceeding ride capacity...");
            service.bookTicket("Big Group", 25, 104, 10); // fills 10 of 10 seats
            System.out.println("Ride 104 capacity filled. Available seats: " + service.getAvailableSeats(104));
            service.bookTicket("Extra Visitor", 25, 104, 2); // should fail
            System.out.println("[FAIL] Should have thrown RideFullException");
        } catch (RideFullException e) {
            System.out.println("[PASS] Caught expected RideFullException: " + e.getMessage());
            try {
                Booking w = service.addToWaitlist("Extra Visitor", 25, 104, 2);
                System.out.println("[PASS] Successfully added to waitlist queue: ID " + w.getBookingId());
            } catch (Exception ex) {
                System.out.println("[FAIL] Waitlist error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("[FAIL] Unexpected error: " + e.getMessage());
        }
    }
}
