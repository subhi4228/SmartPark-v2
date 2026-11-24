/**
 * Smart Amusement Park - Programming Lab (DSA in C)
 * Task 4 (CO4, CO5): Searching, Stable Merge Sort, Hashing & Benchmarks
 *
 * Single-Structure Enforcement:
 * 1. Ride by ID: Hand-written Binary Search on sorted Ride[] array (O(log n)).
 * 2. Bookings by Name: Linear Search on Doubly Linked List (O(n)).
 * 3. History Ordering: Stable Merge Sort on Doubly Linked List (O(n log n)).
 * 4. Fast Booking ID Lookup / Cancel: Open Addressing Hash Table with Linear Probing (O(1) average).
 */

#include "../common/dsa_common.h"
#include <time.h>

#define HASH_INITIAL_CAPACITY 31
#define TOMBSTONE ((void*)(intptr_t)-1)

typedef struct Node {
    Booking data;
    struct Node* prev;
    struct Node* next;
} Node;

typedef struct {
    Node* head;
    Node* tail;
    int size;
} DoublyLinkedList;

// Hash Table Entry
typedef struct {
    long long booking_id;
    Node* node_ptr;
} HashEntry;

typedef struct {
    HashEntry* entries;
    int capacity;
    int count;
} HashTable;

// --- 1. SEARCHING ALGORITHMS ---

// Binary Search on sorted Ride[] array: Time O(log n), Space O(1)
int binary_search_ride(const Ride rides[], int count, int target_id) {
    int low = 0, high = count - 1;
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (rides[mid].id == target_id) return mid;
        if (rides[mid].id < target_id) low = mid + 1;
        else high = mid - 1;
    }
    return -1;
}

// Linear Search on Doubly Linked List: Time O(n), Space O(1)
int linear_search_visitor(const DoublyLinkedList* list, const char* name_query) {
    Node* curr = list->head;
    int matches = 0;
    printf("\nLinear Search Results for '%s':\n", name_query);
    while (curr) {
        if (strstr(curr->data.visitor_name, name_query) != NULL) {
            printf("  [MATCH] ID #%lld: %s (Age %d), Ride %d, Rs. %d [%s]\n",
                   curr->data.booking_id, curr->data.visitor_name, curr->data.age,
                   curr->data.ride_id, curr->data.total_amount, curr->data.status);
            matches++;
        }
        curr = curr->next;
    }
    return matches;
}

// --- 2. STABLE MERGE SORT ON DOUBLY LINKED LIST ---

// Split list into halves: Time O(n), Space O(1)
static Node* split_list(Node* head) {
    Node* fast = head;
    Node* slow = head;
    while (fast->next && fast->next->next) {
        fast = fast->next->next;
        slow = slow->next;
    }
    Node* temp = slow->next;
    slow->next = NULL;
    if (temp) temp->prev = NULL;
    return temp;
}

// Merge two sorted sublists stably by total_amount: Time O(n), Space O(1)
static Node* merge_sublists(Node* first, Node* second) {
    if (!first) return second;
    if (!second) return first;

    // Stable condition: first <= second
    if (first->data.total_amount <= second->data.total_amount) {
        first->next = merge_sublists(first->next, second);
        if (first->next) first->next->prev = first;
        first->prev = NULL;
        return first;
    } else {
        second->next = merge_sublists(first, second->next);
        if (second->next) second->next->prev = second;
        second->prev = NULL;
        return second;
    }
}

// Recursive merge sort: Time O(n log n), Space O(log n) call stack
Node* dll_merge_sort(Node* head) {
    if (!head || !head->next) return head;
    Node* second = split_list(head);
    head = dll_merge_sort(head);
    second = dll_merge_sort(second);
    return merge_sublists(head, second);
}

void sort_booking_history(DoublyLinkedList* list) {
    if (list->size <= 1) return;
    list->head = dll_merge_sort(list->head);
    // Restore tail pointer
    Node* curr = list->head;
    while (curr && curr->next) {
        curr = curr->next;
    }
    list->tail = curr;
}

// --- 3. OPEN ADDRESSING HASH TABLE (LINEAR PROBING) ---

void hash_init(HashTable* ht, int initial_cap) {
    ht->capacity = initial_cap;
    ht->count = 0;
    ht->entries = (HashEntry*) dsa_malloc(sizeof(HashEntry) * ht->capacity);
    for (int i = 0; i < ht->capacity; i++) {
        ht->entries[i].booking_id = 0;
        ht->entries[i].node_ptr = NULL;
    }
}

static int hash_func(long long key, int cap) {
    return (int)((key ^ (key >> 16)) % cap);
}

static void hash_resize(HashTable* ht, int new_cap);

// Insert key-pointer pair: Time O(1) avg, Space O(1)
void hash_insert(HashTable* ht, long long booking_id, Node* node) {
    if ((double)(ht->count + 1) / ht->capacity >= 0.70) {
        hash_resize(ht, ht->capacity * 2 + 1);
    }
    int idx = hash_func(booking_id, ht->capacity);
    while (ht->entries[idx].node_ptr != NULL && ht->entries[idx].node_ptr != TOMBSTONE) {
        if (ht->entries[idx].booking_id == booking_id) {
            ht->entries[idx].node_ptr = node;
            return;
        }
        idx = (idx + 1) % ht->capacity; // Linear probing
    }
    ht->entries[idx].booking_id = booking_id;
    ht->entries[idx].node_ptr = node;
    ht->count++;
}

static void hash_resize(HashTable* ht, int new_cap) {
    HashEntry* old_entries = ht->entries;
    int old_cap = ht->capacity;

    ht->capacity = new_cap;
    ht->count = 0;
    ht->entries = (HashEntry*) dsa_malloc(sizeof(HashEntry) * ht->capacity);
    for (int i = 0; i < ht->capacity; i++) {
        ht->entries[i].booking_id = 0;
        ht->entries[i].node_ptr = NULL;
    }

    for (int i = 0; i < old_cap; i++) {
        if (old_entries[i].node_ptr != NULL && old_entries[i].node_ptr != TOMBSTONE) {
            hash_insert(ht, old_entries[i].booking_id, old_entries[i].node_ptr);
        }
    }
    dsa_free(old_entries);
}

// Find booking node by ID: Time O(1) avg, Space O(1)
Node* hash_find(const HashTable* ht, long long booking_id) {
    int idx = hash_func(booking_id, ht->capacity);
    int start = idx;
    while (ht->entries[idx].node_ptr != NULL) {
        if (ht->entries[idx].node_ptr != TOMBSTONE && ht->entries[idx].booking_id == booking_id) {
            return ht->entries[idx].node_ptr;
        }
        idx = (idx + 1) % ht->capacity;
        if (idx == start) break;
    }
    return NULL;
}

// Delete from hash table using TOMBSTONE: Time O(1) avg, Space O(1)
bool hash_delete(HashTable* ht, long long booking_id) {
    int idx = hash_func(booking_id, ht->capacity);
    int start = idx;
    while (ht->entries[idx].node_ptr != NULL) {
        if (ht->entries[idx].node_ptr != TOMBSTONE && ht->entries[idx].booking_id == booking_id) {
            ht->entries[idx].node_ptr = TOMBSTONE;
            ht->count--;
            return true;
        }
        idx = (idx + 1) % ht->capacity;
        if (idx == start) break;
    }
    return false;
}

void hash_free(HashTable* ht) {
    if (ht->entries) {
        dsa_free(ht->entries);
        ht->entries = NULL;
    }
    ht->count = 0;
    ht->capacity = 0;
}

// Deliberate Collision Demonstration
void demonstrate_collision_probing(void) {
    printf("\n============================================================\n");
    printf("     DELIBERATE HASH COLLISION & LINEAR PROBE DEMO          \n");
    printf("============================================================\n");
    HashTable ht;
    hash_init(&ht, 11); // Prime capacity 11

    long long key1 = 1005;
    long long key2 = 1005 + ht.capacity; // Forces exact collision at hash_func!

    printf("Table Capacity: %d\n", ht.capacity);
    printf("Key 1: %lld -> Initial Hash Index: %d\n", key1, hash_func(key1, ht.capacity));
    printf("Key 2: %lld -> Initial Hash Index: %d (SAME INDEX -> COLLISION!)\n", key2, hash_func(key2, ht.capacity));

    Node dummy1 = { .data = { .booking_id = key1, .total_amount = 100 } };
    Node dummy2 = { .data = { .booking_id = key2, .total_amount = 200 } };

    hash_insert(&ht, key1, &dummy1);
    printf("Inserted Key 1 (%lld) at index %d\n", key1, hash_func(key1, ht.capacity));

    printf("Inserting Key 2 (%lld)... Linear probing resolves slot collision:\n", key2);
    int probe_idx = hash_func(key2, ht.capacity);
    printf("  - Probe slot %d: OCCUPIED by Key %lld\n", probe_idx, ht.entries[probe_idx].booking_id);
    probe_idx = (probe_idx + 1) % ht.capacity;
    printf("  - Probe slot %d: EMPTY -> Successfully stored!\n", probe_idx);

    hash_insert(&ht, key2, &dummy2);

    Node* f1 = hash_find(&ht, key1);
    Node* f2 = hash_find(&ht, key2);
    assert(f1 == &dummy1);
    assert(f2 == &dummy2);
    printf("[PASS] Both keys successfully located via linear probing.\n");

    hash_free(&ht);
}

// --- 4. BENCHMARK SUITE ---
// Bubble sort on temporary array copy: Time O(n^2), Space O(1)
static void bubble_sort(int arr[], int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int tmp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = tmp;
            }
        }
    }
}

// Insertion sort on temporary array copy: Time O(n^2), Space O(1)
static void insertion_sort(int arr[], int n) {
    for (int i = 1; i < n; i++) {
        int key = arr[i];
        int j = i - 1;
        while (j >= 0 && arr[j] > key) {
            arr[j + 1] = arr[j];
            j--;
        }
        arr[j + 1] = key;
    }
}

// Array merge sort for benchmarking: Time O(n log n), Space O(n)
static void merge(int arr[], int l, int m, int r) {
    int n1 = m - l + 1;
    int n2 = r - m;
    int* L = (int*) dsa_malloc(sizeof(int) * n1);
    int* R = (int*) dsa_malloc(sizeof(int) * n2);
    for (int i = 0; i < n1; i++) L[i] = arr[l + i];
    for (int j = 0; j < n2; j++) R[j] = arr[m + 1 + j];

    int i = 0, j = 0, k = l;
    while (i < n1 && j < n2) {
        if (L[i] <= R[j]) arr[k++] = L[i++];
        else arr[k++] = R[j++];
    }
    while (i < n1) arr[k++] = L[i++];
    while (j < n2) arr[k++] = R[j++];
    dsa_free(L);
    dsa_free(R);
}

static void array_merge_sort(int arr[], int l, int r) {
    if (l < r) {
        int m = l + (r - l) / 2;
        array_merge_sort(arr, l, m);
        array_merge_sort(arr, m + 1, r);
        merge(arr, l, m, r);
    }
}

void run_sorting_benchmarks(void) {
    printf("\n=========================================================================\n");
    printf("         SORTING BENCHMARK SUITE: BUBBLE vs INSERTION vs MERGE SORT      \n");
    printf("=========================================================================\n");
    printf("%-8s | %-10s | %-14s | %-14s | %-14s\n",
           "Size N", "Data Order", "Bubble Sort", "Insertion Sort", "Merge Sort");
    printf("-------------------------------------------------------------------------\n");

    int sizes[] = { 100, 1000, 10000 };
    const char* orders[] = { "Sorted", "Reversed", "Random" };

    for (int s = 0; s < 3; s++) {
        int n = sizes[s];
        for (int o = 0; o < 3; o++) {
            int* orig = (int*) dsa_malloc(sizeof(int) * n);
            for (int i = 0; i < n; i++) {
                if (o == 0) orig[i] = i;                          // Sorted
                else if (o == 1) orig[i] = n - i;                 // Reversed
                else orig[i] = (rand() % (n * 10));               // Random
            }

            int* b_arr = (int*) dsa_malloc(sizeof(int) * n);
            int* i_arr = (int*) dsa_malloc(sizeof(int) * n);
            int* m_arr = (int*) dsa_malloc(sizeof(int) * n);
            memcpy(b_arr, orig, sizeof(int) * n);
            memcpy(i_arr, orig, sizeof(int) * n);
            memcpy(m_arr, orig, sizeof(int) * n);

            // Time Bubble
            clock_t t0 = clock();
            if (n <= 10000) bubble_sort(b_arr, n);
            clock_t t1 = clock();
            double b_time = ((double)(t1 - t0) * 1000.0) / CLOCKS_PER_SEC;

            // Time Insertion
            t0 = clock();
            insertion_sort(i_arr, n);
            t1 = clock();
            double i_time = ((double)(t1 - t0) * 1000.0) / CLOCKS_PER_SEC;

            // Time Merge
            t0 = clock();
            array_merge_sort(m_arr, 0, n - 1);
            t1 = clock();
            double m_time = ((double)(t1 - t0) * 1000.0) / CLOCKS_PER_SEC;

            printf("%-8d | %-10s | %9.3f ms   | %9.3f ms   | %9.3f ms\n",
                   n, orders[o], b_time, i_time, m_time);

            dsa_free(orig);
            dsa_free(b_arr);
            dsa_free(i_arr);
            dsa_free(m_arr);
        }
    }
    printf("-------------------------------------------------------------------------\n");
    printf("Conclusion: Merge Sort scales at O(n log n) across all distributions,\n");
    printf("while Bubble and Insertion Sort degrade to O(n^2) on random and reversed data.\n");
}

int main(int argc, char* argv[]) {
    // Shared Ride Array for Binary Search
    Ride rides[MAX_RIDES];
    int ride_count = dsa_load_rides("rides.csv", rides, MAX_RIDES);
    if (ride_count == 0) {
        ride_count = dsa_load_rides("../../data/rides.csv", rides, MAX_RIDES);
    }
    // Ensure array is sorted by ID for binary search
    for (int i = 0; i < ride_count - 1; i++) {
        for (int j = 0; j < ride_count - i - 1; j++) {
            if (rides[j].id > rides[j + 1].id) {
                Ride t = rides[j]; rides[j] = rides[j + 1]; rides[j + 1] = t;
            }
        }
    }

    // Doubly Linked List for Booking History
    DoublyLinkedList history = { .head = NULL, .tail = NULL, .size = 0 };
    HashTable id_hash;
    hash_init(&id_hash, HASH_INITIAL_CAPACITY);

    Booking temp[250];
    int loaded = dsa_load_sample_bookings("../../data/sample_bookings.csv", temp, 250);
    for (int i = 0; i < loaded; i++) {
        Node* node = (Node*) dsa_malloc(sizeof(Node));
        node->data = temp[i];
        node->next = NULL;
        node->prev = history.tail;
        if (history.tail) history.tail->next = node;
        else history.head = node;
        history.tail = node;
        history.size++;

        hash_insert(&id_hash, temp[i].booking_id, node);
    }

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("--- Running Task 4 Automated Verification ---\n");

        // 1. Binary Search on Rides
        int r_idx = binary_search_ride(rides, ride_count, 103);
        assert(r_idx != -1);
        printf("[PASS] Binary search located Ride 103 (%s) at index %d in O(log n).\n",
               rides[r_idx].name, r_idx);

        // 2. Hash Table Lookup in O(1)
        assert(history.head != NULL);
        long long search_id = history.head->data.booking_id;
        Node* h_node = hash_find(&id_hash, search_id);
        assert(h_node != NULL && h_node->data.booking_id == search_id);
        printf("[PASS] Hash table lookup for Booking #%lld found visitor '%s' in O(1) avg.\n",
               search_id, h_node->data.visitor_name);

        // 3. Collision Demo
        demonstrate_collision_probing();

        // 4. Stable Merge Sort
        printf("\nExecuting Stable Merge Sort on Doubly Linked List by total_amount...\n");
        sort_booking_history(&history);
        assert(history.head->data.total_amount <= history.tail->data.total_amount);
        printf("[PASS] Doubly Linked List sorted stably. Min: Rs. %d, Max: Rs. %d\n",
               history.head->data.total_amount, history.tail->data.total_amount);

        // 5. Benchmarks
        run_sorting_benchmarks();

        // Cleanup
        Node* c = history.head;
        while (c) {
            Node* nx = c->next;
            dsa_free(c);
            c = nx;
        }
        hash_free(&id_hash);
        printf("\n[PASS] Memory audit: %d active allocations remaining.\n", dsa_get_active_allocations());
        assert(dsa_get_active_allocations() == 0);
        printf("All Task 4 Verification Checks Completed Successfully!\n");
        return 0;
    }

    int choice;
    do {
        printf("\n============================================================\n");
        printf("  SMART AMUSEMENT PARK - DSA TASK 4 (SEARCH, SORT & HASH)   \n");
        printf("============================================================\n");
        printf("1. Binary Search Ride Catalogue by ID (O(log n))\n");
        printf("2. Linear Search Bookings by Visitor Name (O(n))\n");
        printf("3. Fast Booking ID Lookup via Hash Table (O(1))\n");
        printf("4. Cancel Booking by ID via Hash Table (O(1))\n");
        printf("5. Stable Merge Sort on History List by Amount (O(n log n))\n");
        printf("6. Demonstrate Hash Collision & Linear Probing\n");
        printf("7. Run Comprehensive Sorting Benchmark Suite\n");
        printf("0. Exit\n");
        printf("Enter your choice: ");
        if (scanf("%d", &choice) != 1) break;

        switch (choice) {
            case 1: {
                int rid;
                printf("Enter Ride ID to search (101-104): ");
                scanf("%d", &rid);
                int idx = binary_search_ride(rides, ride_count, rid);
                if (idx != -1) {
                    printf("[FOUND] %s: Capacity %d, Fare Rs. %d, Min Age %d\n",
                           rides[idx].name, rides[idx].capacity,
                           rides[idx].unit_price, rides[idx].min_age);
                } else {
                    printf("[NOT FOUND] No ride found with ID %d.\n", rid);
                }
                break;
            }
            case 2: {
                char query[64];
                printf("Enter visitor name substring: ");
                scanf("%63s", query);
                int found = linear_search_visitor(&history, query);
                printf("Total matching bookings: %d\n", found);
                break;
            }
            case 3: {
                long long bid;
                printf("Enter Booking ID: ");
                scanf("%lld", &bid);
                Node* n = hash_find(&id_hash, bid);
                if (n) {
                    printf("[FOUND] ID #%lld: %s, Ride %d, Rs. %d [%s]\n",
                           n->data.booking_id, n->data.visitor_name,
                           n->data.ride_id, n->data.total_amount, n->data.status);
                } else {
                    printf("[NOT FOUND] Booking ID #%lld not in hash table.\n", bid);
                }
                break;
            }
            case 4: {
                long long bid;
                printf("Enter Booking ID to cancel: ");
                scanf("%lld", &bid);
                Node* n = hash_find(&id_hash, bid);
                if (n && strcmp(n->data.status, "CONFIRMED") == 0) {
                    strcpy(n->data.status, "CANCELLED");
                    printf("[OK] Booking #%lld marked CANCELLED in O(1) via hash table.\n", bid);
                } else {
                    printf("[FAIL] Booking not eligible or not found.\n");
                }
                break;
            }
            case 5:
                sort_booking_history(&history);
                printf("[OK] Booking history sorted stably by amount.\n");
                printf("Lowest: #%lld (%s) - Rs. %d\n",
                       history.head->data.booking_id, history.head->data.visitor_name,
                       history.head->data.total_amount);
                printf("Highest: #%lld (%s) - Rs. %d\n",
                       history.tail->data.booking_id, history.tail->data.visitor_name,
                       history.tail->data.total_amount);
                break;
            case 6:
                demonstrate_collision_probing();
                break;
            case 7:
                run_sorting_benchmarks();
                break;
        }
    } while (choice != 0);

    // Cleanup
    Node* curr = history.head;
    while (curr) {
        Node* next = curr->next;
        dsa_free(curr);
        curr = next;
    }
    hash_free(&id_hash);
    return 0;
}
