/**
 * Smart Amusement Park - Programming Lab (DSA in C)
 * Task 1 (CO1): Ride Catalogue Management using Array
 *
 * Entity Justification:
 * The ride catalogue consists of a small, fixed collection of attractions (4-10 items).
 * A fixed array provides direct O(1) index access, zero pointer overhead,
 * and maximum CPU cache locality.
 */

#include "../common/dsa_common.h"

static Ride g_rides[MAX_RIDES];
static int g_ride_count = 0;

// Search ride index by ID: Time O(n), Space O(1)
int array_find_index(int id) {
    for (int i = 0; i < g_ride_count; i++) {
        if (g_rides[i].id == id) return i;
    }
    return -1;
}

// Insert new ride: Time O(1) append, Space O(1)
bool array_insert_ride(int id, const char* name, int capacity, int price, int min_age) {
    if (g_ride_count >= MAX_RIDES) {
        printf("[ERROR] Array Full: Cannot insert ride %d. Max capacity reached (%d).\n", id, MAX_RIDES);
        return false;
    }
    if (array_find_index(id) != -1) {
        printf("[ERROR] Duplicate Key: Ride ID %d already exists.\n", id);
        return false;
    }
    Ride r;
    r.id = id;
    strncpy(r.name, name, sizeof(r.name) - 1);
    r.capacity = capacity;
    r.unit_price = price;
    r.min_age = min_age;
    r.booked_seats = 0;
    g_rides[g_ride_count++] = r;
    return true;
}

// Delete ride by ID with shifting: Time O(n), Space O(1)
bool array_delete_ride(int id) {
    int idx = array_find_index(id);
    if (idx == -1) {
        printf("[ERROR] Not Found: Cannot delete ride ID %d.\n", id);
        return false;
    }
    for (int i = idx; i < g_ride_count - 1; i++) {
        g_rides[i] = g_rides[i + 1];
    }
    g_ride_count--;
    return true;
}

// Update ride price and capacity: Time O(n), Space O(1)
bool array_update_ride(int id, int new_price, int new_capacity) {
    int idx = array_find_index(id);
    if (idx == -1) {
        printf("[ERROR] Not Found: Cannot update ride ID %d.\n", id);
        return false;
    }
    g_rides[idx].unit_price = new_price;
    g_rides[idx].capacity = new_capacity;
    return true;
}

// Display catalogue: Time O(n), Space O(1)
void array_display_rides(void) {
    printf("\n--- PARK RIDE CATALOGUE (Array Size: %d/%d) ---\n", g_ride_count, MAX_RIDES);
    printf("%-6s | %-16s | %-10s | %-12s | %-8s\n", "ID", "Name", "Capacity", "Price (Rs.)", "Min Age");
    printf("----------------------------------------------------------------\n");
    for (int i = 0; i < g_ride_count; i++) {
        printf("%-6d | %-16s | %-10d | %-12d | %-8d\n",
               g_rides[i].id, g_rides[i].name, g_rides[i].capacity,
               g_rides[i].unit_price, g_rides[i].min_age);
    }
    printf("----------------------------------------------------------------\n");
}

void run_automated_tests(void) {
    printf("--- Running Task 1 Automated Boundary Tests ---\n");
    g_ride_count = 0;

    // Normal Inserts
    assert(array_insert_ride(101, "Ferris Wheel", 20, 100, 5) == true);
    assert(array_insert_ride(102, "Roller Coaster", 15, 200, 10) == true);
    assert(array_insert_ride(103, "Water Ride", 12, 150, 6) == true);
    assert(array_insert_ride(104, "Go-Kart", 10, 120, 12) == true);

    // Boundary Test 1: Duplicate ID
    printf("[TEST 1] Duplicate insertion validation: ");
    bool dup = array_insert_ride(101, "Duplicate Ride", 20, 100, 5);
    assert(dup == false);
    printf("PASSED\n");

    // Boundary Test 2: Search Non-existent
    printf("[TEST 2] Search non-existent ID: ");
    assert(array_find_index(999) == -1);
    printf("PASSED\n");

    // Boundary Test 3: Delete & Verify Shift
    printf("[TEST 3] Delete ride ID 102 and shift check: ");
    assert(array_delete_ride(102) == true);
    assert(array_find_index(102) == -1);
    assert(g_ride_count == 3);
    printf("PASSED\n");

    // Boundary Test 4: Fill Array to Limit
    printf("[TEST 4] Fill array to boundary limit (%d): \n", MAX_RIDES);
    int needed = MAX_RIDES - g_ride_count;
    for (int i = 0; i < needed; i++) {
        array_insert_ride(105 + i, "Test Attraction", 10, 50, 8);
    }
    // Now attempting 11th insert should fail
    bool over = array_insert_ride(999, "Overflow Ride", 10, 50, 8);
    assert(over == false);
    printf("PASSED: Array limit enforced successfully.\n");
    printf("\nAll Task 1 Boundary Tests Passed Successfully!\n");
}

int main(int argc, char* argv[]) {
    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        run_automated_tests();
        return 0;
    }

    // Load default seed rides from data/rides.csv if available
    g_ride_count = dsa_load_rides("../../data/rides.csv", g_rides, MAX_RIDES);
    if (g_ride_count == 0) {
        // Fallback default catalog
        array_insert_ride(101, "Ferris Wheel", 20, 100, 5);
        array_insert_ride(102, "Roller Coaster", 15, 200, 10);
        array_insert_ride(103, "Water Ride", 12, 150, 6);
        array_insert_ride(104, "Go-Kart", 10, 120, 12);
    }

    int choice;
    do {
        printf("\n==================================================\n");
        printf("  SMART AMUSEMENT PARK - DSA TASK 1 (ARRAY MENU)  \n");
        printf("==================================================\n");
        printf("1. Display All Rides\n");
        printf("2. Insert New Ride\n");
        printf("3. Update Ride Price/Capacity\n");
        printf("4. Delete Ride by ID\n");
        printf("5. Search Ride by ID\n");
        printf("6. Run Automated Boundary Test Suite\n");
        printf("0. Exit\n");
        printf("Enter your choice: ");
        if (scanf("%d", &choice) != 1) break;

        switch (choice) {
            case 1:
                array_display_rides();
                break;
            case 2: {
                int id, cap, price, age;
                char name[50];
                printf("Enter ID Name Capacity Price MinAge: ");
                scanf("%d %49s %d %d %d", &id, name, &cap, &price, &age);
                if (array_insert_ride(id, name, cap, price, age)) {
                    printf("[OK] Ride %d inserted successfully.\n", id);
                }
                break;
            }
            case 3: {
                int id, p, c;
                printf("Enter ID NewPrice NewCapacity: ");
                scanf("%d %d %d", &id, &p, &c);
                if (array_update_ride(id, p, c)) {
                    printf("[OK] Ride %d updated.\n", id);
                }
                break;
            }
            case 4: {
                int id;
                printf("Enter ID to delete: ");
                scanf("%d", &id);
                if (array_delete_ride(id)) {
                    printf("[OK] Ride %d deleted.\n", id);
                }
                break;
            }
            case 5: {
                int id;
                printf("Enter ID to search: ");
                scanf("%d", &id);
                int idx = array_find_index(id);
                if (idx != -1) {
                    printf("[FOUND] %s: Capacity %d, Price Rs. %d, Min Age %d\n",
                           g_rides[idx].name, g_rides[idx].capacity,
                           g_rides[idx].unit_price, g_rides[idx].min_age);
                } else {
                    printf("[NOT FOUND] No ride with ID %d.\n", id);
                }
                break;
            }
            case 6:
                run_automated_tests();
                break;
        }
    } while (choice != 0);

    return 0;
}
