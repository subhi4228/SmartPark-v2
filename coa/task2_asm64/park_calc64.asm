; =============================================================================
; Smart Amusement Park - Computer Organization & Architecture Lab (COA)
; Task 2 (CO2): 64-bit Assembly Computations (NASM x86-64, Windows ABI)
;
; Implements:
; 1. calculate_fare_x64(unit_price, group_size)
;    Input:  RCX = unit_price, RDX = group_size
;    Output: RAX = total_fare (RCX * RDX)
; 2. check_seats_x64(capacity, booked, requested)
;    Input:  RCX = capacity, RDX = booked, R8 = requested
;    Output: RAX = 1 (Accept) if (capacity - booked >= requested), else 0 (Reject)
; =============================================================================

default rel

global calculate_fare_x64
global check_seats_x64

section .text

; uint64_t calculate_fare_x64(uint64_t unit_price, uint64_t group_size)
calculate_fare_x64:
    mov rax, rcx        ; Load unit_price into RAX
    imul rax, rdx       ; RAX = unit_price * group_size
    ret

; uint64_t check_seats_x64(uint64_t capacity, uint64_t booked, uint64_t requested)
check_seats_x64:
    sub rcx, rdx        ; RCX = available_seats (capacity - booked)
    cmp rcx, r8         ; Compare available seats with requested seats
    jge .seats_available; If available >= requested, jump to accept

    ; Reject: return 0
    xor rax, rax
    ret

.seats_available:
    ; Accept: return 1
    mov rax, 1
    ret
