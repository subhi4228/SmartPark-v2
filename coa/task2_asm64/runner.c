/**
 * C Runner for Task 2 (NASM x86-64 Assembly).
 */

#include <stdio.h>
#include <stdint.h>
#include <assert.h>

// External assembly routines
extern uint64_t calculate_fare_x64(uint64_t unit_price, uint64_t group_size);
extern uint64_t check_seats_x64(uint64_t capacity, uint64_t booked, uint64_t requested);

int main(void) {
    printf("===============================================================\n");
    printf("   SMART AMUSEMENT PARK - COA TASK 2: NASM x86-64 ASSEMBLY    \n");
    printf("===============================================================\n");

    // 1. Test calculate_fare_x64
    uint64_t price = 200;  // Roller Coaster
    uint64_t group = 4;
    uint64_t fare = calculate_fare_x64(price, group);
    printf("1. calculate_fare_x64(%llu, %llu):\n", price, group);
    printf("   Result: Rs. %llu (Expected: 800)\n", fare);
    assert(fare == 800);

    // 2. Test check_seats_x64
    uint64_t cap = 15;     // Roller Coaster Capacity
    uint64_t booked = 12;  // Currently Booked -> 3 Available
    uint64_t reqA = 3;
    uint64_t reqB = 4;

    uint64_t resA = check_seats_x64(cap, booked, reqA);
    uint64_t resB = check_seats_x64(cap, booked, reqB);

    printf("\n2. check_seats_x64:\n");
    printf("   Test A (Cap %llu, Booked %llu, Req %llu): %s (Expected: 1 ACCEPT)\n",
           cap, booked, reqA, resA == 1 ? "ACCEPT" : "REJECT");
    printf("   Test B (Cap %llu, Booked %llu, Req %llu): %s (Expected: 0 REJECT)\n",
           cap, booked, reqB, resB == 1 ? "ACCEPT" : "REJECT");

    assert(resA == 1);
    assert(resB == 0);

    printf("\n[PASS] All NASM x86-64 Operations Verified Successfully!\n");
    return 0;
}
