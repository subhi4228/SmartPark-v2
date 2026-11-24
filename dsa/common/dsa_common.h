#ifndef DSA_COMMON_H
#define DSA_COMMON_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <assert.h>

#define MAX_RIDES 10
#define DEFAULT_CAPACITY 100

// Ride Catalogue Entity
// Structure choice: Fixed array because catalogue is small (4-10) and fixed in advance.
typedef struct {
    int id;
    char name[50];
    int capacity;
    int unit_price;
    int min_age;
    int booked_seats;
} Ride;

// Booking History Entity
// Structure choice: Doubly linked list for unbounded history, O(1) append, and O(1) node unlinking.
typedef struct Booking {
    long long booking_id;
    char visitor_name[64];
    int age;
    int ride_id;
    int group_size;
    int unit_price;
    int discount_pct;
    int total_amount;
    char status[16];   // "CONFIRMED", "WAITLISTED", "CANCELLED"
    char created_at[32];
} Booking;

// Memory leak tracking helpers
void* dsa_malloc(size_t size);
void dsa_free(void* ptr);
int dsa_get_active_allocations(void);

// Shared CSV parsing helper
int dsa_load_rides(const char* filepath, Ride rides[], int max_rides);
int dsa_load_sample_bookings(const char* filepath, Booking bookings[], int max_bookings);

#endif // DSA_COMMON_H
