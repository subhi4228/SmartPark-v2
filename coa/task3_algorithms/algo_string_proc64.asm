; =============================================================================
; Smart Amusement Park - Computer Organization & Architecture Lab (COA)
; Task 3 (CO3): Add-and-Shift Multiplication & 64-bit String Processing
;
; Implements:
; 1. add_and_shift_mult_x64(multiplicand, multiplier)
;    Input:  RCX = multiplicand, RDX = multiplier
;    Output: RAX = product calculated via binary add-and-shift algorithm
; 2. string_length_x64(str)
;    Input:  RCX = pointer to null-terminated ASCII string
;    Output: RAX = character count
; 3. string_to_upper_x64(dest, src)
;    Input:  RCX = dest buffer, RDX = source string
;    Output: None (dest populated with uppercase ASCII characters)
; =============================================================================

default rel

global add_and_shift_mult_x64
global string_length_x64
global string_to_upper_x64

section .text

; uint64_t add_and_shift_mult_x64(uint64_t multiplicand, uint64_t multiplier)
add_and_shift_mult_x64:
    xor rax, rax        ; Product accumulator RAX = 0

.mult_loop:
    test rdx, rdx       ; Check if multiplier is 0
    jz .mult_done       ; Exit loop if no remaining bits

    test rdx, 1         ; Test least significant bit (LSB)
    jz .skip_add
    add rax, rcx        ; Add shifted multiplicand if bit is 1

.skip_add:
    shl rcx, 1          ; Multiplicand << 1
    shr rdx, 1          ; Multiplier >> 1
    jmp .mult_loop

.mult_done:
    ret

; uint64_t string_length_x64(const char* str)
string_length_x64:
    xor rax, rax        ; Counter RAX = 0

.len_loop:
    cmp byte [rcx + rax], 0
    je .len_done
    inc rax
    jmp .len_loop

.len_done:
    ret

; void string_to_upper_x64(char* dest, const char* src)
string_to_upper_x64:
    xor r8, r8          ; Index R8 = 0

.upper_loop:
    mov al, byte [rdx + r8]
    test al, al
    jz .upper_done

    ; Check if 'a' <= al <= 'z'
    cmp al, 'a'
    jl .store_char
    cmp al, 'z'
    jg .store_char
    sub al, 32          ; Convert to uppercase

.store_char:
    mov byte [rcx + r8], al
    inc r8
    jmp .upper_loop

.upper_done:
    mov byte [rcx + r8], 0 ; Null terminate
    ret
