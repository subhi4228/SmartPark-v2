#include "dsa_common.h"

static int g_active_allocations = 0;

// Time O(1), Space O(1)
void* dsa_malloc(size_t size) {
    void* ptr = malloc(size);
    if (ptr) {
        g_active_allocations++;
    }
    return ptr;
}

// Time O(1), Space O(1)
void dsa_free(void* ptr) {
    if (ptr) {
        g_active_allocations--;
        free(ptr);
    }
}

// Time O(1), Space O(1)
int dsa_get_active_allocations(void) {
    return g_active_allocations;
}

// Helper to open file searching standard relative and project paths: Time O(1), Space O(1)
static FILE* open_data_file(const char* filename) {
    char buf[512];
    const char* prefixes[] = {
        "",
        "data/",
        "../data/",
        "../../data/",
        "../../../data/",
        "C:/Users/SNEHA/.gemini/antigravity/scratch/SmartPark_v2/data/",
        "C:\\Users\\SNEHA\\.gemini\\antigravity\\scratch\\SmartPark_v2\\data\\"
    };
    for (size_t i = 0; i < sizeof(prefixes) / sizeof(prefixes[0]); i++) {
        snprintf(buf, sizeof(buf), "%s%s", prefixes[i], filename);
        FILE* fp = fopen(buf, "r");
        if (fp) return fp;
    }
    return NULL;
}

// Time O(n), Space O(1)
int dsa_load_rides(const char* filepath, Ride rides[], int max_rides) {
    const char* fname = (strstr(filepath, "rides") != NULL) ? "rides.csv" : filepath;
    FILE* fp = open_data_file(fname);
    if (!fp) {
        // Fallback to fixed 4 rides if file not accessible
        Ride default_rides[4] = {
            { .id = 101, .name = "Ferris Wheel", .capacity = 20, .unit_price = 100, .min_age = 5, .booked_seats = 0 },
            { .id = 102, .name = "Roller Coaster", .capacity = 15, .unit_price = 200, .min_age = 10, .booked_seats = 0 },
            { .id = 103, .name = "Water Ride", .capacity = 12, .unit_price = 150, .min_age = 6, .booked_seats = 0 },
            { .id = 104, .name = "Go-Kart", .capacity = 10, .unit_price = 120, .min_age = 12, .booked_seats = 0 }
        };
        for (int i = 0; i < 4 && i < max_rides; i++) {
            rides[i] = default_rides[i];
        }
        return 4;
    }

    char line[256];
    int count = 0;
    if (fgets(line, sizeof(line), fp) == NULL) {
        fclose(fp);
        return 0;
    }

    while (fgets(line, sizeof(line), fp) && count < max_rides) {
        Ride r;
        memset(&r, 0, sizeof(Ride));
        if (sscanf(line, "%d,%49[^,],%d,%d,%d",
                   &r.id, r.name, &r.capacity, &r.unit_price, &r.min_age) == 5) {
            r.booked_seats = 0;
            rides[count++] = r;
        }
    }
    fclose(fp);
    return count;
}

// Time O(n), Space O(1)
int dsa_load_sample_bookings(const char* filepath, Booking bookings[], int max_bookings) {
    const char* fname = (strstr(filepath, "bookings") != NULL) ? "sample_bookings.csv" : filepath;
    FILE* fp = open_data_file(fname);
    if (!fp) return 0;

    char line[512];
    int count = 0;
    if (fgets(line, sizeof(line), fp) == NULL) {
        fclose(fp);
        return 0;
    }

    while (fgets(line, sizeof(line), fp) && count < max_bookings) {
        Booking b;
        memset(&b, 0, sizeof(Booking));
        if (sscanf(line, "%lld,%63[^,],%d,%d,%d,%d,%d,%d,%15[^,],%31s",
                   &b.booking_id, b.visitor_name, &b.age, &b.ride_id,
                   &b.group_size, &b.unit_price, &b.discount_pct,
                   &b.total_amount, b.status, b.created_at) == 10) {
            bookings[count++] = b;
        }
    }
    fclose(fp);
    return count;
}
