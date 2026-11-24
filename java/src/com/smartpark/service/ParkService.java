package com.smartpark.service;

import com.smartpark.exceptions.*;
import com.smartpark.model.*;
import com.smartpark.util.FileRepository;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Core business service for park operations.
 * Enforces single canonical data structure per entity (Section 3.2):
 * - Rides: Ride[] array (small, fixed size 4 to 10, O(1) index access)
 * - Booking History: LinkedList<Booking> (unbounded growth, O(1) append/unlink)
 * - Waitlist: ArrayDeque<Booking> as FIFO queue
 * - Undo: ArrayDeque<Action> as LIFO stack
 * - Fast ID Lookup: HashMap<Long, Booking> (O(1) average lookup/cancel)
 */
public class ParkService {
    private Ride[] rides;
    private final LinkedList<Booking> bookingHistory;
    private final ArrayDeque<Booking> waitlistQueue;
    private final ArrayDeque<Action> undoStack;
    private final HashMap<Long, Booking> bookingIndex;
    private final Set<Long> boardedBookings;
    private final List<StaffMember> staffList;
    private long nextBookingId = 1201;

    public static final int MAX_RIDES = 10;

    public ParkService() {
        this.rides = new Ride[] {
            new FamilyRide(101, "Ferris Wheel", 20, 100, 5),
            new ThrillRide(102, "Roller Coaster", 15, 200, 10),
            new WaterRide(103, "Water Ride", 12, 150, 6),
            new KartRide(104, "Go-Kart", 10, 120, 12)
        };
        Arrays.sort(this.rides); // Pre-sorted by ID for binary search
        this.bookingHistory = new LinkedList<>();
        this.waitlistQueue = new ArrayDeque<>();
        this.undoStack = new ArrayDeque<>();
        this.bookingIndex = new HashMap<>();
        this.boardedBookings = new HashSet<>();
        this.staffList = new ArrayList<>();
        initDefaultStaff();
    }

    private void initDefaultStaff() {
        staffList.add(new StaffMember("OP-101", "Rajesh Kumar", "Ferris Wheel Operator", "09:00 - 15:00", "Certified Level 1 (Mechanical)", "ON DUTY"));
        staffList.add(new StaffMember("OP-102", "Amit Patel", "Roller Coaster Operator", "09:00 - 15:00", "Certified High-G Restraints", "ON DUTY"));
        staffList.add(new StaffMember("OP-103", "Snehal Mane", "Water Ride Operator", "09:00 - 15:00", "Aquatic Safety & Pump Ops", "ON DUTY"));
        staffList.add(new StaffMember("OP-104", "Vikram Singh", "Go-Kart Track Marshal", "09:00 - 15:00", "Track Safety & Governor Lead", "ON DUTY"));
        staffList.add(new StaffMember("OP-105", "Pooja Sharma", "Ticketing & Concourse", "09:00 - 17:00", "Customer Ops & Cash Audit", "ON DUTY"));
        staffList.add(new StaffMember("SUP-01", "Dr. S. Kulkarni", "Park Operations Supervisor", "08:30 - 20:30", "Chief Safety Engineer", "ON DUTY"));
    }

    public Ride[] getRides() { return rides; }
    public LinkedList<Booking> getBookingHistory() { return bookingHistory; }
    public ArrayDeque<Booking> getWaitlistQueue() { return waitlistQueue; }
    public List<StaffMember> getStaffList() { return staffList; }

    // Binary search on sorted Ride[] array: Time O(log n), Space O(1)
    public Ride findRideById(int rideId) {
        int low = 0, high = rides.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (rides[mid].getId() == rideId) return rides[mid];
            if (rides[mid].getId() < rideId) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // Dynamic calculation from active bookings: Time O(m), Space O(1)
    public int getAvailableSeats(int rideId) {
        Ride ride = findRideById(rideId);
        if (ride == null) return 0;
        int confirmed = 0;
        for (Booking b : bookingHistory) {
            if (b.getRideId() == rideId && b.getStatus() == BookingStatus.CONFIRMED) {
                // If the group has already completed/boarded the ride, seats may or may not be freed
                // Following standard rule: active confirmed bookings reserve seats
                confirmed += b.getGroupSize();
            }
        }
        return Math.max(0, ride.getCapacity() - confirmed);
    }

    // Book tickets: Time O(m) to check capacity + O(1) insert, Space O(1)
    public synchronized Booking bookTicket(String visitorName, int age, int rideId, int groupSize)
            throws InvalidVisitorException, AgeRestrictionException, RideFullException {
        validateVisitor(visitorName, age, groupSize);
        Ride ride = findRideById(rideId);
        if (ride == null) throw new InvalidVisitorException("Invalid ride selected.");
        if (age < ride.getMinAge()) throw new AgeRestrictionException(age, ride.getMinAge());

        int available = getAvailableSeats(rideId);
        if (groupSize > available) throw new RideFullException(rideId, groupSize, available);

        int unitPrice = ride.getUnitPrice();
        int discountPct = (age < Discountable.CHILD_AGE_LIMIT) ? Discountable.CHILD_DISCOUNT_PERCENT : 0;
        int total = ride.calculateFare(groupSize, age);

        Booking booking = new Booking(nextBookingId++, visitorName.trim(), age, rideId,
                groupSize, unitPrice, discountPct, total, BookingStatus.CONFIRMED, Instant.now().toString());

        bookingHistory.addLast(booking);
        bookingIndex.put(booking.getBookingId(), booking);
        undoStack.push(new Action(Action.Type.BOOK_CONFIRMED, booking, null));
        return booking;
    }

    // Add to waitlist: Time O(1), Space O(1)
    public synchronized Booking addToWaitlist(String visitorName, int age, int rideId, int groupSize)
            throws InvalidVisitorException, AgeRestrictionException {
        validateVisitor(visitorName, age, groupSize);
        Ride ride = findRideById(rideId);
        if (ride == null) throw new InvalidVisitorException("Invalid ride selected.");
        if (age < ride.getMinAge()) throw new AgeRestrictionException(age, ride.getMinAge());

        int unitPrice = ride.getUnitPrice();
        int discountPct = (age < Discountable.CHILD_AGE_LIMIT) ? Discountable.CHILD_DISCOUNT_PERCENT : 0;
        int total = ride.calculateFare(groupSize, age);

        Booking booking = new Booking(nextBookingId++, visitorName.trim(), age, rideId,
                groupSize, unitPrice, discountPct, total, BookingStatus.WAITLISTED, Instant.now().toString());

        bookingHistory.addLast(booking);
        bookingIndex.put(booking.getBookingId(), booking);
        waitlistQueue.offerLast(booking);
        undoStack.push(new Action(Action.Type.BOOK_WAITLISTED, booking, null));
        return booking;
    }

    // Cancel booking and promote waitlist: Time O(w), Space O(1)
    public synchronized Booking cancelBooking(long bookingId) {
        Booking booking = bookingIndex.get(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            return null;
        }
        booking.setStatus(BookingStatus.CANCELLED);
        boardedBookings.remove(bookingId);

        // Check if any waitlisted booking for this ride can now be promoted (FIFO)
        Booking promoted = null;
        int avail = getAvailableSeats(booking.getRideId());
        for (Iterator<Booking> it = waitlistQueue.iterator(); it.hasNext(); ) {
            Booking w = it.next();
            if (w.getRideId() == booking.getRideId() && w.getGroupSize() <= avail) {
                w.setStatus(BookingStatus.CONFIRMED);
                it.remove();
                promoted = w;
                break;
            }
        }
        undoStack.push(new Action(Action.Type.CANCEL_BOOKING, booking, promoted));
        return booking;
    }

    // Undo last operation: Time O(1), Space O(1)
    public synchronized Action undoLastAction() {
        if (undoStack.isEmpty()) return null;
        Action action = undoStack.pop();
        Booking b = action.getBooking();
        switch (action.getType()) {
            case BOOK_CONFIRMED:
                b.setStatus(BookingStatus.CANCELLED);
                boardedBookings.remove(b.getBookingId());
                break;
            case BOOK_WAITLISTED:
                b.setStatus(BookingStatus.CANCELLED);
                waitlistQueue.remove(b);
                boardedBookings.remove(b.getBookingId());
                break;
            case CANCEL_BOOKING:
                b.setStatus(BookingStatus.CONFIRMED);
                if (action.getPromotedBooking() != null) {
                    action.getPromotedBooking().setStatus(BookingStatus.WAITLISTED);
                    waitlistQueue.offerFirst(action.getPromotedBooking());
                }
                break;
        }
        return action;
    }

    // ==========================================
    // RIDE CATALOGUE DYNAMIC MANAGEMENT
    // Array Ride[] preserved for Section 3.2
    // ==========================================

    public synchronized boolean addRide(Ride newRide) {
        if (newRide == null || rides.length >= MAX_RIDES) return false;
        if (findRideById(newRide.getId()) != null) return false;

        Ride[] updated = Arrays.copyOf(rides, rides.length + 1);
        updated[rides.length] = newRide;
        Arrays.sort(updated);
        this.rides = updated;
        return true;
    }

    public synchronized boolean removeRide(int rideId) {
        if (rides.length <= 1) return false;
        int targetIdx = -1;
        for (int i = 0; i < rides.length; i++) {
            if (rides[i].getId() == rideId) {
                targetIdx = i;
                break;
            }
        }
        if (targetIdx == -1) return false;

        Ride[] updated = new Ride[rides.length - 1];
        int idx = 0;
        for (int i = 0; i < rides.length; i++) {
            if (i != targetIdx) {
                updated[idx++] = rides[i];
            }
        }
        Arrays.sort(updated);
        this.rides = updated;
        return true;
    }

    public synchronized boolean updateRidePrice(int rideId, int newPrice) {
        if (newPrice <= 0) return false;
        Ride r = findRideById(rideId);
        if (r != null) {
            r.setUnitPrice(newPrice);
            return true;
        }
        return false;
    }

    public int getBookingCountForRide(int rideId) {
        int count = 0;
        for (Booking b : bookingHistory) {
            if (b.getRideId() == rideId) count++;
        }
        return count;
    }

    public long getRevenueForRide(int rideId) {
        long rev = 0;
        for (Booking b : bookingHistory) {
            if (b.getRideId() == rideId && b.getStatus() == BookingStatus.CONFIRMED) {
                rev += b.getTotalAmount();
            }
        }
        return rev;
    }

    // ==========================================
    // VISITOR BOARDING QUEUE & SEQUENCE
    // Sequential Boarding Order per Attraction
    // ==========================================

    public List<Booking> getBoardingQueueForRide(int rideId) {
        List<Booking> queue = new ArrayList<>();
        for (Booking b : bookingHistory) {
            if (b.getRideId() == rideId && b.getStatus() == BookingStatus.CONFIRMED) {
                queue.add(b);
            }
        }
        return queue;
    }

    public synchronized boolean markBoarded(long bookingId) {
        return boardedBookings.add(bookingId);
    }

    public synchronized boolean unmarkBoarded(long bookingId) {
        return boardedBookings.remove(bookingId);
    }

    public synchronized boolean isBoarded(long bookingId) {
        return boardedBookings.contains(bookingId);
    }

    // ==========================================
    // STAFF MANAGEMENT METHODS
    // ==========================================

    public synchronized void addStaff(StaffMember s) {
        if (s != null) staffList.add(s);
    }

    public synchronized boolean removeStaff(String staffId) {
        return staffList.removeIf(s -> s.getId().equalsIgnoreCase(staffId));
    }

    public synchronized boolean updateStaff(String staffId, String name, String work, String shift, String cert, String status) {
        for (StaffMember s : staffList) {
            if (s.getId().equalsIgnoreCase(staffId)) {
                if (name != null) s.setName(name);
                if (work != null) s.setAssignedWork(work);
                if (shift != null) s.setShift(shift);
                if (cert != null) s.setCertification(cert);
                if (status != null) s.setStatus(status);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean toggleStaffStatus(String staffId) {
        for (StaffMember s : staffList) {
            if (s.getId().equalsIgnoreCase(staffId)) {
                String cur = s.getStatus();
                if ("ON DUTY".equalsIgnoreCase(cur)) s.setStatus("ON BREAK");
                else if ("ON BREAK".equalsIgnoreCase(cur)) s.setStatus("OFF DUTY");
                else s.setStatus("ON DUTY");
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // SEARCH, SORT, CSV & AGGREGATE HELPERS
    // ==========================================

    public List<Booking> searchBookingsByVisitor(String query) {
        List<Booking> matches = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Booking b : bookingHistory) {
            if (b.getVisitorName().toLowerCase().contains(q) ||
                String.valueOf(b.getBookingId()).equals(q)) {
                matches.add(b);
            }
        }
        return matches;
    }

    public void sortHistoryByAmount(boolean ascending) {
        bookingHistory.sort((a, b) -> ascending ?
                Integer.compare(a.getTotalAmount(), b.getTotalAmount()) :
                Integer.compare(b.getTotalAmount(), a.getTotalAmount()));
    }

    public void sortHistoryByTime(boolean ascending) {
        bookingHistory.sort((a, b) -> ascending ?
                a.getCreatedAt().compareTo(b.getCreatedAt()) :
                b.getCreatedAt().compareTo(a.getCreatedAt()));
    }

    public void loadFromCsv(File csvFile) throws IOException {
        FileRepository<Booking> repo = new FileRepository<>(csvFile);
        List<String> lines = repo.readAllLines();
        for (int i = 1; i < lines.size(); i++) {
            Booking b = Booking.fromCsvRow(lines.get(i));
            if (b != null) {
                bookingHistory.addLast(b);
                bookingIndex.put(b.getBookingId(), b);
                if (b.getStatus() == BookingStatus.WAITLISTED) {
                    waitlistQueue.offerLast(b);
                }
                if (b.getBookingId() >= nextBookingId) {
                    nextBookingId = b.getBookingId() + 1;
                }
            }
        }
    }

    public void exportBookingsCsv(File dest) throws IOException {
        FileRepository<Booking> repo = new FileRepository<>(dest);
        String header = "booking_id,visitor_name,age,ride_id,group_size,unit_price,discount_pct,total_amount,status,created_at";
        repo.saveAll(bookingHistory, header);
    }

    public long getTotalRevenue() {
        long sum = 0;
        for (Booking b : bookingHistory) {
            if (b.getStatus() == BookingStatus.CONFIRMED) sum += b.getTotalAmount();
        }
        return sum;
    }

    public int getTotalConfirmedVisitors() {
        int visitors = 0;
        for (Booking b : bookingHistory) {
            if (b.getStatus() == BookingStatus.CONFIRMED) visitors += b.getGroupSize();
        }
        return visitors;
    }

    public int getCancelledCount() {
        int c = 0;
        for (Booking b : bookingHistory) {
            if (b.getStatus() == BookingStatus.CANCELLED) c++;
        }
        return c;
    }

    private void validateVisitor(String name, int age, int groupSize) throws InvalidVisitorException {
        if (name == null || name.trim().isEmpty() || !name.matches("^[a-zA-Z\\s]+$")) {
            throw new InvalidVisitorException("Visitor name must contain letters only.");
        }
        if (age < 1 || age > 100) {
            throw new InvalidVisitorException("Visitor age must be between 1 and 100.");
        }
        if (groupSize < 1 || groupSize > 10) {
            throw new InvalidVisitorException("Group size must be between 1 and 10.");
        }
    }
}
