package com.smartpark.db;

import com.google.gson.*;
import com.smartpark.model.Booking;
import com.smartpark.model.BookingStatus;
import com.smartpark.util.FileRepository;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Resilient REST client for Supabase cloud persistence.
 * Implements offline queueing via FileRepository and auto-sync retry every 30s.
 */
public class SupabaseClient {
    private final String supabaseUrl;
    private final String supabaseKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private final FileRepository<Booking> offlineQueue;
    private boolean isOnline = false;

    public SupabaseClient(File configFile, File queueFile) {
        Properties props = loadConfig(configFile);
        this.supabaseUrl = props.getProperty("supabase.url", "").trim();
        this.supabaseKey = props.getProperty("supabase.key", "").trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
        this.offlineQueue = new FileRepository<>(queueFile);
    }

    private Properties loadConfig(File file) {
        Properties props = new Properties();
        if (file != null && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException ignored) {}
        }
        return props;
    }

    public boolean isConfigured() {
        return !supabaseUrl.isEmpty() && !supabaseKey.isEmpty() && !supabaseUrl.contains("your-project-id");
    }

    public boolean isOnline() { return isOnline; }

    // Health check: Time O(1), Space O(1)
    public boolean checkConnection() {
        if (!isConfigured()) {
            isOnline = false;
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/rides?select=ride_id&limit=1"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            isOnline = (response.statusCode() == 200);
        } catch (Exception e) {
            isOnline = false;
        }
        return isOnline;
    }

    // Book ticket via Postgres RPC with row lock: Time O(1), Space O(1)
    public boolean syncBooking(Booking booking) {
        if (!isOnline && !checkConnection()) {
            queueOffline(booking);
            return false;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("p_visitor_name", booking.getVisitorName());
            payload.put("p_age", booking.getAge());
            payload.put("p_ride_id", booking.getRideId());
            payload.put("p_group_size", booking.getGroupSize());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/rpc/book_ticket"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return true;
            } else {
                queueOffline(booking);
                return false;
            }
        } catch (Exception e) {
            isOnline = false;
            queueOffline(booking);
            return false;
        }
    }

    // Cancel booking via Postgres RPC: Time O(1), Space O(1)
    public boolean syncCancel(long bookingId) {
        if (!isOnline && !checkConnection()) return false;
        try {
            Map<String, Object> payload = Collections.singletonMap("p_booking_id", bookingId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/rpc/cancel_booking"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            isOnline = false;
            return false;
        }
    }

    private synchronized void queueOffline(Booking b) {
        try {
            offlineQueue.append(b);
        } catch (IOException ignored) {}
    }

    // Flush pending offline bookings when connection is restored
    public synchronized int flushPendingQueue() {
        if (!checkConnection()) return 0;
        try {
            List<String> lines = offlineQueue.readAllLines();
            if (lines.isEmpty()) return 0;
            List<Booking> remaining = new ArrayList<>();
            int synced = 0;
            for (String line : lines) {
                Booking b = Booking.fromCsvRow(line);
                if (b != null && !syncBooking(b)) {
                    remaining.add(b);
                } else {
                    synced++;
                }
            }
            offlineQueue.saveAll(remaining, "");
            return synced;
        } catch (IOException e) {
            return 0;
        }
    }
}
