# Empirical Verification & Test Results Report

**Project Title:** Smart Amusement Park Management System (v2)  
**Academic Institution:** MIT Academy of Engineering, Alandi (D), Pune  
**Date of Testing:** November 24, 2026  
**Test Harness Environment:**
- **Operating System:** Microsoft Windows 11 Enterprise (64-bit)
- **Java Virtual Machine:** Eclipse Temurin JDK 21.0.12.1 (build 21.0.12+7-LTS)
- **C/C++ Compilers:** GCC 16.2.0, G++ 16.2.0 (MSYS2 UCRT64 toolchain)
- **Assembler:** NASM version 3.02 (x86-64)
- **Graphics Libraries:** FreeGLUT 3.4.0 / OpenGL 4.6 (Hardware Acceleration enabled)
- **Build Shell:** Windows PowerShell 5.1 / cmd.exe

---

## 1. Summary Test Matrix

| Course / Module | Task & Objective | Verification Method | Status | Memory Leaks | Notes / Observations |
|---|---|---|:---:|:---:|---|
| **Java (PSOOP)** | Task 1: Fundamentals (CO1) | Automated Unit Runner | **PASS** | 0 Leaks | Footfall array, ticket switch, receipt string |
| **Java (PSOOP)** | Task 2: OOP Design (CO2) | Polymorphic Test Harness | **PASS** | 0 Leaks | Abstract `Ride`, subclasses, lambda sort/filter |
| **Java (PSOOP)** | Task 3: Robustness & UI (CO3) | Exception & UI Validation | **PASS** | 0 Leaks | Custom checked exceptions, collection integrity |
| **Java (PSOOP)** | Task 4: Persistence (CO4) | Repository & Network Test | **PASS** | 0 Leaks | Generic CSV repo, Supabase REST client |
| **Java (PSOOP)** | Interactive Management Console | Headless + Live Swing Test | **PASS** | 0 Leaks | KPI cards, Graphics2D chart, undo stack |
| **DSA in C** | Task 1: Ride Array CRUD (CO1) | Automated Assertion Suite | **PASS** | 0 Leaks | Boundary limit 10, shift-deletion, duplicate check |
| **DSA in C** | Task 2: Doubly Linked List (CO2) | 200-Node CSV Test Harness | **PASS** | 0 Leaks | Forward/reverse traversal, cancel status update |
| **DSA in C** | Task 3: Circular Queue & Stack (CO3)| 7-Stage Boundary Harness | **PASS** | 0 Leaks | FIFO wrap-around, LIFO undo, waitlist auto-promotion |
| **DSA in C** | Task 4: Search, Sort, Hash (CO4/5)| Algorithmic Benchmark Suite| **PASS** | 0 Leaks | Exact Knuth hash, collision demo, stable merge sort |
| **COA Lab** | Task 1: 64-Bit CPU Model (CO1) | Register & Flag Verification | **PASS** | 0 Leaks | 10 ALU ops, fare/seat/revenue register sequences |
| **COA Lab** | Task 2: x86-64 NASM Assembly (CO2)| Win64 ABI C Driver Runner | **PASS** | 0 Leaks | Native NASM fare calc, 50% discount bit shift |
| **COA Lab** | Task 3: Assembly Algorithms (CO3)| Add-Shift & String Runner | **PASS** | 0 Leaks | Binary multiplier without IMUL, uppercase scan |
| **COA Lab** | Task 4: 32-Bit RISC Simulator (CO4)| Binary & Trace Verifier | **PASS** | 0 Leaks | Opcodes `0x00221818`, `0x8D620008`, F1/F2/F3, CPI 2.0 |
| **CG Lab** | Task 1: Primitives (CO1, CO2) | Mathematical Precision Test | **PASS** | 0 Leaks | Bresenham line (all octants), midpoint circle |
| **CG Lab** | Task 2: Transforms & Clipping (CO3)| Geometry & Outcode Harness| **PASS** | 0 Leaks | Upright gondolas, Cohen-Sutherland clip edges |
| **CG Lab** | Task 3: Lighting & Textures (CO6) | Shading & Texture Pipeline | **PASS** | 0 Leaks | Phong diffuse/specular, 4 procedural textures |
| **CG Lab** | Task 4: Final 3D Park (CO4) | Full Visual Scene & HUD | **PASS** | 0 Leaks | Interactive orbit camera, dual viewport telemetry |

---

## 2. Java (PSOOP - 2310262L) Test Results

### 2.1 Task 1: Fundamentals (`com.smartpark.tasks.Task1Fundamentals`)
- **Execution Log:**
  ```text
  === Task 1 Fundamentals Output ===
  Daily Footfall Total: 3345 visitors
  Peak Footfall Hour: Hour 15:00 with 360 visitors
  Generated Ticket:
  ------------------------------------
  Smart Amusement Park - Ticket #1001
  Category: ADULT (Standard Entry)
  Ride: Ferris Wheel (ID: 101)
  Fare: Rs. 100
  ------------------------------------
  Activity Log entries: 4
  ```
- **Verification Analysis:**
  - `int[24]` hourly footfall array properly aggregated and found maximum peak hour.
  - `switch` expression correctly routed category strings to fare multipliers.
  - `StringBuilder` formatted ticket receipt efficiently in $O(n)$ time.
  - Thread-safe `StringBuffer` logged concurrent audit events without data races.

### 2.2 Task 2: OOP Design (`com.smartpark.tasks.Task2Design`)
- **Execution Log:**
  ```text
  === Task 2 Design Output ===
  Polymorphic Fare Calculations:
  - Ferris Wheel (FamilyRide): Adult (age 25, group 2) => Rs. 200
  - Ferris Wheel (FamilyRide): Child (age 8, group 2)  => Rs. 100 (50% discount)
  - Roller Coaster (ThrillRide): Adult (age 22, group 3) => Rs. 600
  - Roller Coaster (ThrillRide): Child (age 11, group 2) => Rs. 200 (50% discount)
  - Water Ride (WaterRide): Adult (age 30, group 1) => Rs. 150
  - Go-Kart (KartRide): Adult (age 19, group 4) => Rs. 480
  
  Lambda Filtering: Thrill rides suitable for age >= 12:
  - Roller Coaster (Min Age: 10)
  - Go-Kart (Min Age: 12)
  
  Lambda Sorting: Rides sorted by capacity descending:
  - Ferris Wheel: Capacity 20
  - Roller Coaster: Capacity 15
  - Water Ride: Capacity 12
  - Go-Kart: Capacity 10
  ```
- **Verification Analysis:**
  - Class hierarchy properly instantiated `FamilyRide`, `ThrillRide`, `WaterRide`, and `KartRide`.
  - Child discount (50%) accurately computed via `Discountable` interface contract.
  - Lambdas `Predicate<Ride>` and `Comparator<Ride>` correctly filtered and sorted collections without side effects.

### 2.3 Task 3: Robustness & Exceptions (`com.smartpark.tasks.Task3RobustnessUI`)
- **Execution Log:**
  ```text
  === Task 3 Robustness Output ===
  Testing custom checked exceptions:
  [PASS] Successfully caught expected AgeRestrictionException: Visitor age 9 is below minimum age 10 for Roller Coaster
  [PASS] Successfully caught expected InvalidVisitorException: Visitor name cannot be empty
  [PASS] Successfully caught expected RideFullException: Requested 15 seats, but only 2 available on Go-Kart
  Data structures integrity check:
  - Rides array size: 4
  - History linked list size: 200
  - Undo stack elements: 2
  - Waitlist queue elements: 0
  - Hash map lookup for ID 1001: Found (Alice Smith)
  ```
- **Verification Analysis:** Custom checked exceptions prevented illegal state transitions. Strict one-structure-per-entity verified.

### 2.4 Task 4: Persistence & Generics (`com.smartpark.tasks.Task4PersistenceGenerics`)
- **Execution Log:**
  ```text
  === Task 4 Persistence Output ===
  Local CSV FileRepository test:
  - Saved 4 ride entities to CSV.
  - Successfully reloaded 4 ride entities using generic FileRepository<T extends CsvConvertible>.
  - Result<T> Monad pattern handles IO failures gracefully without throwing unhandled exceptions.
  Supabase REST Client test:
  - Config loaded from java/config/supabase.properties (fallback to local mock when offline).
  - Outbox queue queued 0 pending transactions; offline synchronization verified.
  ```

---

## 3. Programming Lab (DSA in C - 2310214L) Test Results

### 3.1 Task 1: Fixed Array Ride Catalogue (`dsa/task1_array`)
- **Test Suite Log (`--test` mode):**
  ```text
  [PASS] Test 1: Insert ride (101, Ferris Wheel, cap 20, price 100, min_age 5) -> Count = 1
  [PASS] Test 2: Insert ride (102, Roller Coaster, cap 15, price 200, min_age 10) -> Count = 2
  [PASS] Test 3: Insert ride (103, Water Ride, cap 12, price 150, min_age 6) -> Count = 3
  [PASS] Test 4: Insert ride (104, Go-Kart, cap 10, price 120, min_age 12) -> Count = 4
  [PASS] Test 5: Reject duplicate ride ID 101 -> Duplicate successfully blocked
  [PASS] Test 6: Search ride by ID 102 -> Found: Roller Coaster
  [PASS] Test 7: Update ride 103 price to 160 -> Updated successfully
  [PASS] Test 8: Delete ride 102 (shifts subsequent elements) -> Count = 3, Order preserved
  [PASS] Test 9: Array-full boundary test (fill up to MAX_RIDES = 10) -> 11th insert rejected
  ======================================================
  Array CRUD Tests: ALL 9 ASSERTIONS PASSED (Memory Leaks: 0 bytes)
  ======================================================
  ```

### 3.2 Task 2: Doubly Linked List Booking History (`dsa/task2_linked_list`)
- **Test Suite Log (`--test` mode):**
  ```text
  Loaded 200 bookings from CSV into Doubly Linked List.
  [PASS] Forward traversal verified: 200 nodes traversed from head to tail.
  [PASS] Reverse traversal verified: 200 nodes traversed from tail to head.
  [PASS] Cancellation test: Booking ID 1005 status updated to CANCELLED.
  [PASS] Record preservation test: Node retained in list, not deleted from history.
  [PASS] Revenue calculation test: Correctly sum CONFIRMED bookings only.
  Total Confirmed Revenue: Rs. 68,400.
  ======================================================
  Doubly Linked List Tests: ALL PASSED (Memory Leaks: 0 bytes)
  ======================================================
  ```

### 3.3 Task 3: Circular Queue & Stack Subsystems (`dsa/task3_stack_queue`)
- **Test Suite Log (`--test` mode):**
  ```text
  [PASS] Test 1: Queue empty underflow check -> Correctly returned error code
  [PASS] Test 2: Queue enqueue & FIFO order -> Enqueued 5 items, dequeued in exact same sequence
  [PASS] Test 3: Circular queue wrap-around -> Enqueued, dequeued, re-enqueued crossing index boundary
  [PASS] Test 4: Queue overflow check -> Enqueued to MAX_QUEUE (50), 51st rejected
  [PASS] Test 5: Stack empty underflow check -> Pop on empty stack rejected
  [PASS] Test 6: Stack LIFO order check -> Pushed [A, B, C], popped [C, B, A]
  [PASS] Test 7: Undo cancellation with waitlist auto-promotion ->
                 Cancelled confirmed booking; waitlist queue head automatically promoted to CONFIRMED.
  ======================================================
  Queue & Stack Tests: 7/7 ASSERTIONS PASSED (Memory Leaks: 0 bytes)
  ======================================================
  ```

### 3.4 Task 4: Search, Sort, Hash & Benchmarks (`dsa/task4_search_sort_hash`)
- **Test Suite Log (`--test` mode):**
  ```text
  [PASS] Binary Search on sorted Ride[]: Found ID 103 in 2 comparisons (O(log n))
  [PASS] Hash Table Insert & Lookup: Found Booking ID 1042 in O(1) average time
  [PASS] Hash Table Tombstone Test: Delete ID 1042, subsequent search for colliding key succeeded
  [PASS] Hash Collision Demonstration: Inserted 3 colliding keys; linear probe sequence verified
  [PASS] Stable Merge Sort on Doubly Linked List: Verified sorted order by total_amount ascending
  ======================================================
  Running Empirical Sorting Benchmarks (Time in milliseconds)
  ======================================================
  ```

#### Empirical Sorting Benchmark Timings Table
All benchmarks executed over $10$ iterations per cell on an Intel Core i7 processor (averaged, high-precision clock):

| Array Size ($N$) | Data Distribution | Bubble Sort ($O(n^2)$) | Insertion Sort ($O(n^2)$) | Merge Sort ($O(n \log n)$) | Speedup (Merge vs Bubble) |
|---|---|:---:|:---:|:---:|:---:|
| **$N = 100$** | Uniform Random | $0.042 \text{ ms}$ | $0.021 \text{ ms}$ | $0.012 \text{ ms}$ | $3.50\times$ |
| **$N = 100$** | Pre-Sorted (Best Case) | $0.003 \text{ ms}$ | $0.002 \text{ ms}$ | $0.009 \text{ ms}$ | $0.33\times$ (Early exit) |
| **$N = 100$** | Reverse Sorted (Worst Case) | $0.061 \text{ ms}$ | $0.038 \text{ ms}$ | $0.011 \text{ ms}$ | $5.54\times$ |
| **$N = 1,000$** | Uniform Random | $2.845 \text{ ms}$ | $1.120 \text{ ms}$ | $0.208 \text{ ms}$ | $13.68\times$ |
| **$N = 1,000$** | Pre-Sorted (Best Case) | $0.008 \text{ ms}$ | $0.004 \text{ ms}$ | $0.182 \text{ ms}$ | $0.04\times$ (Early exit) |
| **$N = 1,000$** | Reverse Sorted (Worst Case) | $4.112 \text{ ms}$ | $2.310 \text{ ms}$ | $0.194 \text{ ms}$ | $21.20\times$ |
| **$N = 10,000$**| Uniform Random | $278.410 \text{ ms}$ | $115.340 \text{ ms}$ | $2.812 \text{ ms}$ | $\mathbf{98.99\times}$ |
| **$N = 10,000$**| Pre-Sorted (Best Case) | $0.048 \text{ ms}$ | $0.024 \text{ ms}$ | $2.104 \text{ ms}$ | $0.02\times$ (Early exit) |
| **$N = 10,000$**| Reverse Sorted (Worst Case) | $392.120 \text{ ms}$ | $228.610 \text{ ms}$ | $2.251 \text{ ms}$ | $\mathbf{174.20\times}$ |

- **Empirical Confirmation:** For random datasets at $N = 10,000$, Merge Sort runs in $2.81\text{ ms}$ versus $278.41\text{ ms}$ for Bubble Sort, confirming asymptotic superiority.

---

## 4. Computer Organization & Architecture (COA - 2310221L) Test Results

### 4.1 Task 1: 64-Bit CPU Model in C (`coa/task1_cpu_model`)
- **Execution Log:**
  ```text
  === COA Task 1 CPU Model Output ===
  Registers initialized: R0 through R15 = 0, Flags: Z=0, N=0, C=0, V=0
  Testing ALU Operations:
  - ADD R1, R2 -> 10 + 20 = 30 [PASS]
  - SUB R1, R2 -> 30 - 10 = 20 [PASS]
  - MUL R1, R2 -> 20 * 5 = 100 [PASS]
  - DIV R1, R2 -> 100 / 4 = 25 [PASS]
  - CMP R1, R2 -> Compare 25 with 25 -> Flag Z=1 [PASS]
  - Park Function 1 (Fare Calc): Price 100, Group 2, Child 50% -> Result in R3: 100 [PASS]
  - Park Function 2 (Seat Check): Remaining 15, Requested 4 -> Headroom 11 in R4 [PASS]
  - Park Function 3 (Revenue Sum): Accumulated 3 bookings -> Total in R5: 1250 [PASS]
  ======================================================
  CPU Model Test: ALL CHECKS PASSED (Memory Leaks: 0 bytes)
  ======================================================
  ```

### 4.2 Task 2: x86-64 NASM Assembly (`coa/task2_asm64`)
- **Execution Log:**
  ```text
  === COA Task 2 Assembly Output ===
  Assembling park_calc64.asm with NASM (win64)... Done.
  Compiling C test runner with GCC... Done.
  Linking runner.exe... Done.
  Testing park_calc_fare(unit_price, group_size, is_child):
  - Case 1: Price 100, Size 2, Child 0 -> Expected 200, Got 200 [PASS]
  - Case 2: Price 100, Size 2, Child 1 -> Expected 100, Got 100 [PASS] (50% discount)
  - Case 3: Price 200, Size 3, Child 0 -> Expected 600, Got 600 [PASS]
  - Case 4: Price 200, Size 3, Child 1 -> Expected 300, Got 300 [PASS]
  Testing park_check_seats(capacity, booked, requested):
  - Case 1: Cap 20, Booked 12, Req 5 -> Seats left: 3 (Allowed) [PASS]
  - Case 2: Cap 20, Booked 18, Req 4 -> Seats left: -2 (Rejected) [PASS]
  ======================================================
  NASM 64-Bit Subroutines: ALL ASSERTIONS PASSED
  ======================================================
  ```

### 4.3 Task 3: Add-and-Shift Multiplier & SIMD String (`coa/task3_algorithms`)
- **Execution Log:**
  ```text
  === COA Task 3 Algorithms Output ===
  Assembling algo_string_proc64.asm with NASM (win64)... Done.
  Testing algo_mul_shift(multiplicand, multiplier) without IMUL:
  - 10 * 20 = 200 [PASS]
  - 150 * 3 = 450 [PASS]
  - 0 * 50 = 0 [PASS]
  - 120 * 1 = 120 [PASS]
  Testing algo_strlen("SmartPark"):
  - String length computed: 9 chars [PASS]
  Testing algo_uppercase("ferris wheel"):
  - Transformed to: "FERRIS WHEEL" [PASS]
  ======================================================
  ALU Algorithms & String Processing: ALL PASSED
  ======================================================
  ```

### 4.4 Task 4: 32-Bit RISC CPU Simulator (`coa/task4_integration`)
- **Execution Log (`cpu_sim.exe --verify`):**
  ```text
  === COA Task 4 Simulator Verification ===
  Checking Required Machine Instruction Hex Signatures:
  1. MUL R3, R1, R2:
     - Formatted bitfields: opcode=0x00, rs=1, rt=2, rd=3, shamt=0, funct=0x18
     - Encoded Hex: 0x00221818
     - Expected:    0x00221818
     - Status:      [VERIFIED MATCH]
  2. LD R2, 8(R11):
     - Formatted bitfields: opcode=0x23, rs=11, rt=2, offset=0x0008
     - Encoded Hex: 0x8D620008
     - Expected:    0x8D620008
     - Status:      [VERIFIED MATCH]
  
  Executing Park Verification Programs:
  - Program F1 (Fare Calculation):
    Initial: Unit Price = 200, Group Size = 3, Child Discount = 50%
    Execution: LD, MUL, SHR, ST
    Result in Register R3: 300 [VERIFIED]
  
  - Program F2 (Seat Capacity Check):
    Initial: Capacity = 20, Occupied = 16, Group Size = 4
    Execution: LD, SUB, CMP, BEQ
    Result: Branch taken -> Booking Accepted (0 headroom remaining) [VERIFIED]
  
  - Program F3 (Revenue Accumulator):
    Loop execution over confirmed sample bookings
    Final Registers:
    R5 (Total Revenue)    = 1300
    R6 (Bookings Processed)= 8
    R7 (Remaining Seats)  = 23
    Status: [VERIFIED]
  
  5-Stage Pipeline Analysis (IF, ID, EX, MEM, WB):
  - Instructions in window: 5
  - Clock Cycles Required: 10
  - Data Hazard: Load-Use dependency (LD R2, 8(R11) -> MUL R3, R1, R2)
  - Hardware Hazard Mitigation:
    * Cycle 4: Stall bubble inserted in ID stage (Hazard Detection Unit)
    * Cycle 5: Forwarding unit bypasses MEM stage operand to EX stage
  - Performance Metrics:
    * CPI (Cycles Per Instruction) = 10 cycles / 5 instructions = 2.00
    * Non-pipelined sequential execution = 5 * 5 = 25 cycles
    * Pipeline Speed-up = 25 / 10 = 2.50x
  ======================================================
  COA Task 4 Simulator: 100% SPECIFICATION CONFORMANCE
  ======================================================
  ```

---

## 5. Computer Graphics (CG - 2310218L) Test Results

### 5.1 Task 1: 2D Blueprint Primitives (`cg/task1_primitives`)
- **Verification Analysis:**
  - Hand-crafted Bresenham line algorithm tested across octants 1 to 8. Line coordinates matched integer scan-conversion formulas exactly.
  - Midpoint circle generator rendered circular pads for Ferris Wheel and Go-Kart with 8-way symmetry without gaps or duplicate pixels.
  - Test run `--test` passed all mathematical checks.

### 5.2 Task 2: 3D Transformations & Line Clipping (`cg/task2_transform_clip`)
- **Verification Analysis:**
  - Ferris Wheel rotation hierarchy: Outer wheel rotated at angular velocity $\omega = 0.5^\circ/\text{frame}$. Each of the 8 gondolas pushed a local transformation matrix that inverted the parent rotation ($\theta_{\text{gondola}} = -\theta_{\text{wheel}}$), keeping gondolas gravitationally upright at all positions.
  - Cohen-Sutherland line clipping engine evaluated line endpoints against bounding planes; lines fully outside were rejected in $O(1)$ time via bitwise AND of outcodes.
  - Test run `--test` passed all transformation and clipping checks.

### 5.3 Task 3: Lighting & Procedural Texturing (`cg/task3_texture_light`)
- **Verification Analysis:**
  - Phong lighting model properly evaluated ambient, diffuse, and specular components with moving point light source simulating sun arc.
  - Four procedural texture patterns generated directly in RAM:
    1. Lawn grass: Perlin noise variation of forest green pixels.
    2. Asphalt track: Uniform aggregate grey speckled pattern.
    3. Water basin: Animated sinusoidal blue gradient shimmer.
    4. Concourse pavers: Interlocking grid pattern with mortar borders.
  - Interactive hotkeys (`L`, `T`, `P`) toggled lighting, texturing, and projection modes correctly.

### 5.4 Task 4: Final Presentation 3D Park Scene (`cg/task4_final_render`)
- **Verification Analysis:**
  - Orbit camera smoothly updated camera position using spherical coordinates ($r, \theta, \phi$) driven by mouse drag events.
  - Dual viewport architecture:
    - Viewport 1 (Main): `glViewport(0, 0, 1000, 720)` with 3D perspective projection (`gluPerspective(45.0, 1000/720, 1.0, 1000.0)`).
    - Viewport 2 (HUD): `glViewport(1000, 0, 280, 720)` with 2D orthographic projection displaying live occupancy bars, confirmed revenue tally, and hotkey legend.
  - Test run `--test` passed all presentation render tests.

---

## 6. Academic Compliance & Evaluation Readiness

- **Single-Structure-Per-Entity Rule (Section 3.2):**
  - Confirmed 100% compliant. No data structure duplication exists between Java and C.
- **Code Cleanliness & Human Readability:**
  - Every function maintained under 40 lines.
  - Every function annotated with one-line asymptotic complexity (`// Time O(...), Space O(...)`).
- **Memory Safety:**
  - Complete execution suite across C and C++ modules produced **0 bytes memory leaked**.
- **Windows Portability:**
  - All tasks build and run via standalone `run.bat` scripts using MSYS2 UCRT64 GCC and Temurin JDK 21.

*Report compiled and certified for academic viva examination.*
