/**
 * C Runner for Task 3 (Add-and-Shift Mult & String Ops).
 */

#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <assert.h>

extern uint64_t add_and_shift_mult_x64(uint64_t multiplicand, uint64_t multiplier);
extern uint64_t string_length_x64(const char* str);
extern void string_to_upper_x64(char* dest, const char* src);

int main(void) {
    printf("=====================================================================\n");
    printf("   SMART AMUSEMENT PARK - COA TASK 3: ADD-AND-SHIFT & STRING OPS     \n");
    printf("=====================================================================\n");

    // 1. Add-and-Shift Multiplication Algorithm Test
    uint64_t a = 120; // Go-Kart Price
    uint64_t b = 6;   // Group Size
    uint64_t prod = add_and_shift_mult_x64(a, b);
    printf("1. Add-and-Shift Binary Multiplication (%llu x %llu):\n", a, b);
    printf("   Hardware Algorithm Result: Rs. %llu (Expected: 720)\n", prod);
    assert(prod == 720);

    // Test with larger powers
    uint64_t prod2 = add_and_shift_mult_x64(250, 40);
    assert(prod2 == 10000);
    printf("   Secondary Multiplication (250 x 40): Rs. %llu (Expected: 10000) [PASS]\n", prod2);

    // 2. String Length Assembly Routine
    const char* visitor = "Aarav Sharma";
    uint64_t len = string_length_x64(visitor);
    printf("\n2. 64-bit String Length for '%s':\n", visitor);
    printf("   Calculated Length: %llu characters (Expected: %zu)\n", len, strlen(visitor));
    assert(len == strlen(visitor));

    // 3. String To-Upper Conversion Assembly Routine
    char upper_buf[64];
    string_to_upper_x64(upper_buf, visitor);
    printf("\n3. 64-bit String Uppercase Conversion:\n");
    printf("   Input:  '%s'\n", visitor);
    printf("   Output: '%s' (Expected: 'AARAV SHARMA')\n", upper_buf);
    assert(strcmp(upper_buf, "AARAV SHARMA") == 0);

    printf("\n[PASS] All COA Task 3 Arithmetic & String Operations Verified!\n");
    return 0;
}
