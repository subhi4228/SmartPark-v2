/**
 * Smart Amusement Park - Computer Organization & Architecture Lab (COA)
 * Task 1 (CO1): 64-bit Processor-Level Simulation of Park Business Logic
 *
 * Implements:
 * - 64-bit register set
 * - ALU operations (ADD, SUB, MUL, CMP)
 * - Control flow logic executing:
 *   1. Fare calculation (unit_price * group_size)
 *   2. Seat capacity verification (capacity - booked >= requested)
 *   3. Revenue accumulation
 */

#include <stdio.h>
#include <stdint.h>
#include <stdbool.h>
#include <assert.h>

typedef struct {
    uint64_t r[8]; // R0 through R7
    bool zero_flag;
    bool carry_flag;
} CPU64;

// Time O(1), Space O(1)
void cpu64_init(CPU64* cpu) {
    for (int i = 0; i < 8; i++) cpu->r[i] = 0;
    cpu->zero_flag = false;
    cpu->carry_flag = false;
}

// 64-bit ALU Execution: Time O(1), Space O(1)
uint64_t cpu64_alu(CPU64* cpu, int op, uint64_t a, uint64_t b) {
    uint64_t res = 0;
    switch (op) {
        case 0: res = a + b; break; // ADD
        case 1: res = a - b; break; // SUB
        case 2: res = a * b; break; // MUL
        case 3: // CMP
            res = a - b;
            cpu->zero_flag = (a == b);
            cpu->carry_flag = (a < b);
            return res;
    }
    cpu->zero_flag = (res == 0);
    return res;
}

// 1. Processor-level Fare Calculation: Time O(1), Space O(1)
uint64_t calc_fare(CPU64* cpu, uint64_t unit_price, uint64_t group_size, uint64_t age) {
    cpu->r[1] = unit_price;
    cpu->r[2] = group_size;
    cpu->r[3] = cpu64_alu(cpu, 2, cpu->r[1], cpu->r[2]); // MUL
    if (age < 12) {
        cpu->r[3] = cpu->r[3] / 2; // Child 50% discount
    }
    return cpu->r[3];
}

// 2. Processor-level Seat Verification: Time O(1), Space O(1)
bool verify_seats(CPU64* cpu, uint64_t capacity, uint64_t booked, uint64_t requested) {
    cpu->r[1] = capacity;
    cpu->r[2] = booked;
    cpu->r[4] = cpu64_alu(cpu, 1, cpu->r[1], cpu->r[2]); // Available = Cap - Booked
    cpu64_alu(cpu, 3, cpu->r[4], requested);              // CMP Avail with Requested
    return !cpu->carry_flag;                              // True if Avail >= Requested
}

int main(int argc, char* argv[]) {
    printf("===============================================================\n");
    printf("   SMART AMUSEMENT PARK - COA TASK 1: 64-BIT CPU MODEL (C)     \n");
    printf("===============================================================\n");

    CPU64 cpu;
    cpu64_init(&cpu);

    // Test 1: Fare Calculation
    uint64_t adult_fare = calc_fare(&cpu, 150, 4, 25);
    uint64_t child_fare = calc_fare(&cpu, 150, 4, 10);
    printf("Operation 1: Fare Calculation\n");
    printf("  4 Adults on Water Ride (Rs. 150): Rs. %llu (Expected: 600)\n", adult_fare);
    printf("  4 Children on Water Ride (50%% off): Rs. %llu (Expected: 300)\n", child_fare);
    assert(adult_fare == 600);
    assert(child_fare == 300);

    // Test 2: Seat Verification
    bool accept = verify_seats(&cpu, 20, 16, 4);
    bool reject = verify_seats(&cpu, 20, 18, 4);
    printf("\nOperation 2: Seat Check\n");
    printf("  Cap 20, Booked 16, Request 4 -> %s (Expected: ACCEPT)\n", accept ? "ACCEPT" : "REJECT");
    printf("  Cap 20, Booked 18, Request 4 -> %s (Expected: REJECT)\n", reject ? "ACCEPT" : "REJECT");
    assert(accept == true);
    assert(reject == false);

    // Test 3: Revenue Accumulation
    uint64_t rev_total = 0;
    rev_total = cpu64_alu(&cpu, 0, rev_total, adult_fare);
    rev_total = cpu64_alu(&cpu, 0, rev_total, child_fare);
    printf("\nOperation 3: Revenue Accumulation\n");
    printf("  Total Revenue: Rs. %llu (Expected: 900)\n", rev_total);
    assert(rev_total == 900);

    printf("\n[PASS] All COA Task 1 64-bit CPU Model Operations Verified!\n");
    return 0;
}
