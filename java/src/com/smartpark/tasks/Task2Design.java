package com.smartpark.tasks;

import com.smartpark.model.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Task 2 (CO2) - Object-Oriented Design and Functional Interfaces.
 * Demonstrates:
 * - Abstract class Ride and subclasses (ThrillRide, FamilyRide, WaterRide, KartRide)
 * - Polymorphic fare calculation (override of calculateFare)
 * - Interfaces Bookable and Discountable
 * - Lambdas (Predicate for filtering, Comparator for sorting)
 */
public class Task2Design {
    public static void main(String[] args) {
        System.out.println("Executing Task 2 (CO2) OOP Design Demo...");

        Ride[] rides = new Ride[] {
            new FamilyRide(101, "Ferris Wheel", 20, 100, 5),
            new ThrillRide(102, "Roller Coaster", 15, 200, 10),
            new WaterRide(103, "Water Ride", 12, 150, 6),
            new KartRide(104, "Go-Kart", 10, 120, 12)
        };

        System.out.println("\n1. Polymorphic Ride Descriptions & Fares:");
        for (Ride r : rides) {
            int fareChild = r.calculateFare(2, 9);
            int fareAdult = r.calculateFare(2, 25);
            System.out.printf("%s -> 2 Children: Rs. %d | 2 Adults: Rs. %d\n",
                    r.describe(), fareChild, fareAdult);
        }

        // Functional Programming: Lambda Predicate for filtering rides accessible for an 8-year-old
        int visitorAge = 8;
        Predicate<Ride> ageFilter = ride -> visitorAge >= ride.getMinAge();
        System.out.printf("\n2. Lambda Filter: Rides accessible for Age %d:\n", visitorAge);
        Arrays.stream(rides)
                .filter(ageFilter)
                .forEach(r -> System.out.println("  [ACCESSIBLE] " + r.getName()));

        // Lambda Comparator: Sort rides by Unit Price descending
        System.out.println("\n3. Lambda Comparator: Rides ordered by Price (Descending):");
        List<Ride> rideList = new ArrayList<>(Arrays.asList(rides));
        rideList.sort((r1, r2) -> Integer.compare(r2.getUnitPrice(), r1.getUnitPrice()));
        rideList.forEach(r -> System.out.printf("  %s - Rs. %d\n", r.getName(), r.getUnitPrice()));
    }
}
