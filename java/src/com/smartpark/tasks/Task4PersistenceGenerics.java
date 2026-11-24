package com.smartpark.tasks;

import com.smartpark.model.Booking;
import com.smartpark.model.BookingStatus;
import com.smartpark.util.FileRepository;
import com.smartpark.util.Result;
import java.io.File;
import java.time.Instant;
import java.util.List;

/**
 * Task 4 (CO4) - Persistence, Try-with-Resources and Generics.
 * Demonstrates:
 * - Generic FileRepository<T extends CsvConvertible>
 * - Generic Result<T> container
 * - Safe file I/O using try-with-resources
 */
public class Task4PersistenceGenerics {
    public static void main(String[] args) {
        System.out.println("Executing Task 4 (CO4) Persistence & Generics Demo...\n");
        File tempFile = new File("build/temp_test_bookings.csv");
        FileRepository<Booking> repo = new FileRepository<>(tempFile);

        Booking b1 = new Booking(9001, "Test Alpha", 24, 101, 2, 100, 0, 200, BookingStatus.CONFIRMED, Instant.now().toString());
        Booking b2 = new Booking(9002, "Test Beta", 9, 102, 3, 200, 50, 300, BookingStatus.CONFIRMED, Instant.now().toString());

        try {
            repo.append(b1);
            repo.append(b2);
            System.out.println("[PASS] Appended 2 bookings to " + tempFile.getPath());

            List<String> rows = repo.readAllLines();
            System.out.println("[PASS] Successfully read " + rows.size() + " lines from repository:");
            for (String r : rows) {
                System.out.println("  " + r);
            }

            // Generic Result Test
            Result<Booking> okRes = Result.ok(b1);
            Result<Booking> failRes = Result.fail("Network unreachable. Cached locally.");
            System.out.println("\nResult Container Test:");
            System.out.println("  OK Result: success=" + okRes.isSuccess() + ", visitor=" + okRes.getData().getVisitorName());
            System.out.println("  Fail Result: success=" + failRes.isSuccess() + ", error=" + failRes.getError());

            tempFile.delete(); // cleanup
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
