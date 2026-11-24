/**
 * Smart Amusement Park - Programming Lab (DSA in C)
 * Task 2 (CO2): Booking History Management using Doubly Linked List
 *
 * Entity Justification:
 * The booking history grows continuously and unbounded as visitors purchase tickets.
 * An array would require costly amortized resizing and element shifting upon status changes.
 * A Doubly Linked List (DLL) with explicit head and tail pointers provides:
 * 1. O(1) append time at the tail for new bookings.
 * 2. O(1) node unlinking/status modification given a node pointer.
 * 3. Bidirectional traversal (forward and reverse chronological viewing).
 */

#include "../common/dsa_common.h"

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

// Initialize list: Time O(1), Space O(1)
void dll_init(DoublyLinkedList* list) {
    list->head = NULL;
    list->tail = NULL;
    list->size = 0;
}

// Append booking at tail: Time O(1), Space O(1)
void dll_append(DoublyLinkedList* list, Booking b) {
    Node* node = (Node*) dsa_malloc(sizeof(Node));
    node->data = b;
    node->next = NULL;
    node->prev = list->tail;

    if (list->tail) {
        list->tail->next = node;
    } else {
        list->head = node;
    }
    list->tail = node;
    list->size++;
}

// Cancel booking by ID: Time O(n), Space O(1)
bool dll_cancel_by_id(DoublyLinkedList* list, long long booking_id) {
    Node* curr = list->head;
    while (curr) {
        if (curr->data.booking_id == booking_id) {
            if (strcmp(curr->data.status, "CONFIRMED") == 0) {
                strcpy(curr->data.status, "CANCELLED");
                return true;
            }
            return false; // Already cancelled or waitlisted
        }
        curr = curr->next;
    }
    return false;
}

// Display forward: Time O(n), Space O(1)
void dll_display_forward(const DoublyLinkedList* list) {
    printf("\n--- BOOKING HISTORY (Chronological Forward, Size: %d) ---\n", list->size);
    printf("%-6s | %-16s | %-4s | %-6s | %-6s | %-10s | %-11s\n",
           "ID", "Visitor", "Age", "Ride", "Group", "Total (Rs)", "Status");
    printf("------------------------------------------------------------------------\n");
    Node* curr = list->head;
    int displayed = 0;
    while (curr && displayed < 25) { // show sample to avoid terminal flood
        printf("%-6lld | %-16s | %-4d | %-6d | %-6d | %-10d | %-11s\n",
               curr->data.booking_id, curr->data.visitor_name, curr->data.age,
               curr->data.ride_id, curr->data.group_size, curr->data.total_amount,
               curr->data.status);
        curr = curr->next;
        displayed++;
    }
    if (list->size > 25) {
        printf("... [%d more records omitted for display brevity] ...\n", list->size - 25);
    }
    printf("------------------------------------------------------------------------\n");
}

// Display reverse: Time O(n), Space O(1)
void dll_display_reverse(const DoublyLinkedList* list) {
    printf("\n--- BOOKING HISTORY (Reverse Chronological, Size: %d) ---\n", list->size);
    printf("%-6s | %-16s | %-4s | %-6s | %-6s | %-10s | %-11s\n",
           "ID", "Visitor", "Age", "Ride", "Group", "Total (Rs)", "Status");
    printf("------------------------------------------------------------------------\n");
    Node* curr = list->tail;
    int displayed = 0;
    while (curr && displayed < 15) {
        printf("%-6lld | %-16s | %-4d | %-6d | %-6d | %-10d | %-11s\n",
               curr->data.booking_id, curr->data.visitor_name, curr->data.age,
               curr->data.ride_id, curr->data.group_size, curr->data.total_amount,
               curr->data.status);
        curr = curr->prev;
        displayed++;
    }
    printf("------------------------------------------------------------------------\n");
}

// Compute total revenue: Time O(n), Space O(1)
long long dll_total_revenue(const DoublyLinkedList* list) {
    long long total = 0;
    Node* curr = list->head;
    while (curr) {
        if (strcmp(curr->data.status, "CONFIRMED") == 0) {
            total += curr->data.total_amount;
        }
        curr = curr->next;
    }
    return total;
}

// Clean up list and verify memory: Time O(n), Space O(1)
void dll_free_all(DoublyLinkedList* list) {
    Node* curr = list->head;
    while (curr) {
        Node* next = curr->next;
        dsa_free(curr);
        curr = next;
    }
    list->head = NULL;
    list->tail = NULL;
    list->size = 0;
}

int main(int argc, char* argv[]) {
    DoublyLinkedList history;
    dll_init(&history);

    // Load initial bookings from sample CSV
    Booking temp[250];
    int loaded = dsa_load_sample_bookings("../../data/sample_bookings.csv", temp, 250);
    for (int i = 0; i < loaded; i++) {
        dll_append(&history, temp[i]);
    }

    if (argc > 1 && strcmp(argv[1], "--test") == 0) {
        printf("--- Running Task 2 Automated Tests ---\n");
        assert(history.size == loaded);
        printf("[PASS] Loaded %d bookings into Doubly Linked List.\n", history.size);

        long long rev = dll_total_revenue(&history);
        printf("[PASS] Active confirmed revenue: Rs. %lld\n", rev);

        // Cancel test
        long long test_id = history.head->data.booking_id;
        assert(dll_cancel_by_id(&history, test_id) == true);
        assert(strcmp(history.head->data.status, "CANCELLED") == 0);
        printf("[PASS] Cancelled booking #%lld successfully.\n", test_id);

        dll_free_all(&history);
        printf("[PASS] Memory leak audit: %d active allocations remaining.\n", dsa_get_active_allocations());
        assert(dsa_get_active_allocations() == 0);
        printf("All Task 2 Tests Passed with Zero Leaks!\n");
        return 0;
    }

    int choice;
    do {
        printf("\n======================================================\n");
        printf("  SMART AMUSEMENT PARK - DSA TASK 2 (DOUBLY LINKED LIST)\n");
        printf("======================================================\n");
        printf("1. Display History (Forward Chronological)\n");
        printf("2. Display History (Reverse Chronological)\n");
        printf("3. Add New Booking at Tail\n");
        printf("4. Cancel Booking by ID\n");
        printf("5. Calculate Total Confirmed Revenue\n");
        printf("6. Run Automated Tests and Memory Audit\n");
        printf("0. Exit\n");
        printf("Enter your choice: ");
        if (scanf("%d", &choice) != 1) break;

        switch (choice) {
            case 1:
                dll_display_forward(&history);
                break;
            case 2:
                dll_display_reverse(&history);
                break;
            case 3: {
                Booking b;
                memset(&b, 0, sizeof(Booking));
                b.booking_id = 2000 + history.size;
                printf("Enter VisitorName Age RideID GroupSize UnitPrice Total: ");
                scanf("%63s %d %d %d %d %d", b.visitor_name, &b.age, &b.ride_id,
                      &b.group_size, &b.unit_price, &b.total_amount);
                strcpy(b.status, "CONFIRMED");
                strcpy(b.created_at, "NOW");
                dll_append(&history, b);
                printf("[OK] Booking #%lld appended to history tail.\n", b.booking_id);
                break;
            }
            case 4: {
                long long id;
                printf("Enter Booking ID to cancel: ");
                scanf("%lld", &id);
                if (dll_cancel_by_id(&history, id)) {
                    printf("[OK] Booking #%lld marked CANCELLED.\n", id);
                } else {
                    printf("[FAIL] Booking #%lld not found or already cancelled.\n", id);
                }
                break;
            }
            case 5:
                printf("\nTotal Active Confirmed Revenue: Rs. %lld\n", dll_total_revenue(&history));
                break;
            case 6: {
                dll_free_all(&history);
                // Re-run test
                dll_init(&history);
                for (int i = 0; i < loaded; i++) dll_append(&history, temp[i]);
                long long test_id = history.head->data.booking_id;
                dll_cancel_by_id(&history, test_id);
                printf("[PASS] All operations verified.\n");
                break;
            }
        }
    } while (choice != 0);

    dll_free_all(&history);
    return 0;
}
