package com.smartpark.tasks;

import com.smartpark.model.*;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * Task 1 (CO1) - Programming Fundamentals in Java.
 * Demonstrates:
 * - 24-hour footfall count array (int[24])
 * - Fixed catalogue array (Ride[] rides)
 * - Switch statements for ticket category classification
 * - StringBuilder for single-threaded ticket receipt generation
 * - StringBuffer for thread-safe activity logging
 */
public class Task1Fundamentals {
    private final int[] hourlyFootfall = new int[24];
    private final StringBuffer sharedAuditLog = new StringBuffer();

    // Time O(1), Space O(1)
    public void recordVisitor(int hour, int count) {
        if (hour >= 0 && hour < 24) {
            hourlyFootfall[hour] += count;
            sharedAuditLog.append(String.format("[%02d:00] Logged %d visitors.\n", hour, count));
        }
    }

    // Time O(1), Space O(1)
    public String generateReceipt(String visitorName, int age, String rideName, int groupSize, int unitPrice, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        SMART AMUSEMENT PARK TICKET      \n");
        sb.append("========================================\n");
        sb.append("Visitor: ").append(visitorName).append(" (Age: ").append(age).append(")\n");
        sb.append("Ride: ").append(rideName).append("\n");
        sb.append("Visitors in Group: ").append(groupSize).append("\n");
        sb.append("Unit Price: Rs. ").append(unitPrice).append("\n");
        
        String category;
        switch (age < 12 ? 1 : (age >= 60 ? 2 : 3)) {
            case 1: category = "Child (50% Concession applied)"; break;
            case 2: category = "Senior Citizen"; break;
            default: category = "General Adult"; break;
        }
        sb.append("Category: ").append(category).append("\n");
        sb.append("Total Amount: Rs. ").append(total).append("\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    // Time O(n), Space O(1)
    public void printFootfallReport() {
        System.out.println("--- 24-HOUR FOOTFALL REPORT ---");
        int total = 0;
        for (int h = 0; h < 24; h++) {
            if (hourlyFootfall[h] > 0) {
                System.out.printf("%02d:00 - %02d:59 : %d visitors\n", h, h, hourlyFootfall[h]);
                total += hourlyFootfall[h];
            }
        }
        System.out.printf("Total Park Visitors Recorded: %d\n", total);
    }

    public static void main(String[] args) {
        System.out.println("Executing Task 1 (CO1) Fundamentals Demo...");
        Task1Fundamentals task = new Task1Fundamentals();
        task.recordVisitor(10, 15);
        task.recordVisitor(11, 42);
        task.recordVisitor(14, 38);
        task.printFootfallReport();

        String receipt = task.generateReceipt("Aarav Sharma", 10, "Ferris Wheel", 2, 100, 100);
        System.out.println(receipt);
        System.out.println("Shared Audit Log Output:\n" + task.sharedAuditLog);
    }
}
