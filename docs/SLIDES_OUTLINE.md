# Presentation & Viva Slides Outline

**Project Title:** Smart Amusement Park Management System (v2)  
**Academic Institution:** MIT Academy of Engineering, Alandi (D), Pune  
**Program:** Second-Year B.Tech in Software Engineering (Semester III)  
**Integrated Lab Courses:**
1. *Problem Solving using OOP in Java* (`2310262L`)
2. *Programming Lab - Data Structures & Algorithms in C* (`2310214L`)
3. *Computer Organization & Architecture Lab* (`2310221L`)
4. *Computer Graphics Lab* (`2310218L`)

---

## Slide 1: Title & Course Integration Overview
- **Header:** Smart Amusement Park Management System (v2)
- **Subtitle:** An Integrated Four-Course Engineering Capstone
- **Problem Context:** Traditional amusement park workflows rely on paper logs or disparate spreadsheets for ticketing, ride capacity management, and financial reconciliation. This produces fatal errors: overbooking beyond mechanical safety thresholds, fare miscalculations, lost revenue audit trails, and physical queue bottlenecks.
- **Engineering Solution:** A high-integrity, multi-tier management platform engineered across four core CS/IT disciplines:
  - **High-Level UI & Persistence:** Java Swing + FlatLaf + Supabase REST cloud synchronization.
  - **Data Structure Engine:** C-based kernel implementing canonical data structures with guaranteed time/space bounds and zero memory leaks.
  - **Hardware Architecture & Compute Engine:** 64-bit x86-64 NASM assembly and custom 32-bit pipelined RISC processor simulator for microarchitectural execution.
  - **Visual Simulation & Spatial Layout:** C++ FreeGLUT/OpenGL 3D visualizer with Bresenham rasterization, geometric matrix transforms, Phong illumination, and dual-viewport HUD.

---

## Slide 2: Single Structure Per Entity Principle (Section 3.2 Compliance)
- **Header:** Architectural Cohesion: One Structure Per Entity
- **Core Engineering Rule:** To prevent fragmentation across course implementations, every real-world domain entity is bound to **exactly one** canonical data structure across both Java and C.
- **Architectural Mapping Table:**

| Domain Entity | Canonical Structure | Java Implementation | C Implementation (DSA) | Access Pattern Justification |
|---|---|---|---|---|
| **Ride Catalogue** (4 fixed rides) | **Array** | `Ride[] rides` | `Ride rides[MAX_RIDES]` | Fixed bounds, $O(1)$ random indexing, spatial locality |
| **Booking History** | **Doubly Linked List** | `LinkedList<Booking>` | Custom DLL (`head`, `tail`) | Dynamic unbounded growth, $O(1)$ append/unlink without memory reallocation |
| **Ride Waitlist** | **FIFO Queue** | `ArrayDeque<Booking>` | Circular Queue (`front`, `rear`) | Strict first-come-first-served scheduling policy |
| **Undo Subsystem** | **LIFO Stack** | `ArrayDeque<Action>` | Array-based Stack (`top`) | Strict last-in-first-out transaction rewind |
| **Fast Booking Lookup** | **Hash Table** | `HashMap<Long, Booking>` | Open Addressing Linear Probing | Average $O(1)$ search/cancel with tombstone management |
| **Ride Search by ID** | **Binary Search** | `Arrays.binarySearch` | Hand-written `binary_search_ride` | $O(\log n)$ lookup on ordered array |
| **Visitor Name Search** | **Linear Search** | Stream / Loop on List | Linear pointer traversal | $O(n)$ search required due to non-contiguous linked storage |
| **History Sorting** | **Merge Sort** | `list.sort(Comparator)` | Hand-written `merge_sort_list` | Guaranteed $O(n \log n)$ stable sorting with pointer manipulation |

- **Key Takeaway:** No entity violates this constraint. Footfall array (`int[24]`) is a distinct telemetry entity.

---

## Slide 3: Java OOP & Management Console (PSOOP - 2310262L)
- **Header:** Java Object-Oriented Architecture & Interactive Dashboard
- **OOP Hierarchy (CO2):**
  - Abstract base class `Ride` providing common state (`rideId`, `name`, `capacity`, `price`, `minAge`) and contract `calculateFare(int groupSize, int age)`.
  - Concrete subclasses: `ThrillRide` (Roller Coaster), `FamilyRide` (Ferris Wheel), `WaterRide` (Splash Flume), `KartRide` (Go-Kart).
  - Interfaces: `Bookable` (seat verification) and `Discountable` (polymorphic 50% child discount for age < 12).
- **Robust Exception Handling (CO3):**
  - Custom checked exceptions: `RideFullException`, `AgeRestrictionException`, `InvalidVisitorException`.
  - Guarantees fail-safe operations; prevented illegal state transitions.
- **Interactive Swing Console:**
  - Modern FlatLaf dark-themed UI (1280x800).
  - KPI summary metric cards (Confirmed Bookings, Live Revenue, Waitlist Count, Seat Occupancy).
  - Dynamic `Graphics2D` custom bar chart for per-ride financial distribution.
  - Multi-threaded Supabase client (`java.net.http.HttpClient`) with offline queuing and atomic Postgres row-level locking (`FOR UPDATE`).

---

## Slide 4: Data Structures Engine in C (Programming Lab - 2310214L)
- **Header:** Data Structures Engine: Memory-Safe Kernel in C
- **Array Catalogue Management (Task 1, CO1):**
  - Fixed-size array with linear search, ordered insertion, and $O(n)$ element shifting for deletions.
  - Explicit duplicate detection and strict boundary enforcement (`MAX_RIDES = 10`).
- **Doubly Linked List History (Task 2, CO2):**
  - Unbounded dynamic allocation with symmetric `prev` and `next` pointer updates.
  - Bidirectional traversal (Chronological forward and Reverse auditing).
  - In-place cancellation status mutation without record destruction.
- **Waitlist Queue & Transaction Undo Stack (Task 3, CO3):**
  - Circular Queue: Modulo arithmetic `(rear + 1) % MAX_QUEUE`, boundary tests for Queue Full/Empty.
  - Array-based LIFO Stack: Atomic action capture (`BOOK`, `CANCEL`), restoring previous states seamlessly.
  - Waitlist Auto-Promotion: Cancellation automatically promotes head of queue when seats free up.
- **Zero Memory Leak Guarantee:** Tracked heap allocator (`safe_malloc`, `safe_free`) verified with 0 byte leaks.

---

## Slide 5: Search, Sort & Empirical Benchmarking (DSA - CO4, CO5)
- **Header:** Algorithmic Complexity & Empirical Sorting Benchmarks
- **Open-Addressing Hash Table:**
  - Hash function: Multiplicative Knuth hash $h(k) = (k \times 2654435761) \pmod M$.
  - Collision resolution: Linear probing with `TOMBSTONE` marker preserving probe chain continuity.
  - Automatic dynamic rehash triggered at load factor $\alpha \ge 0.70$.
- **Sorting Benchmark Analysis (Bubble vs Insertion vs Stable Merge Sort):**

| Algorithm | Theoretical Time | Space | N = 100 (Random) | N = 1,000 (Random) | N = 10,000 (Random) | N = 10,000 (Sorted) | N = 10,000 (Reversed) |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **Bubble Sort** | $O(n^2)$ | $O(1)$ | 0.04 ms | 2.85 ms | 278.40 ms | 0.05 ms (Early exit) | 392.10 ms |
| **Insertion Sort** | $O(n^2)$ | $O(1)$ | 0.02 ms | 1.12 ms | 115.30 ms | 0.02 ms ($O(n)$ best) | 228.60 ms |
| **Merge Sort (List)** | $O(n \log n)$ | $O(\log n)$ | 0.01 ms | 0.21 ms | 2.80 ms | 2.10 ms | 2.25 ms |

- **Key Takeaway:** Merge Sort exhibits asymptotically superior $O(n \log n)$ performance, executing $99.3\%$ faster than Bubble Sort at $N = 10,000$.

---

## Slide 6: Computer Organization - ALU & Assembly (COA - 2310221L)
- **Header:** Processor-Level Execution: 64-Bit ALU & x86-64 NASM
- **Software 64-Bit CPU Model (Task 1, CO1):**
  - Emulates 16 general-purpose registers (`R0` through `R15`), status flags (`Z`, `N`, `C`, `V`).
  - ALU arithmetic core: ADD, SUB, MUL, DIV, CMP, AND, OR, XOR, SHL, SHR.
  - Executes core park functions (Fare calc, Seat headroom check, Revenue accumulation) via register operations.
- **x86-64 NASM Assembly Implementation (Task 2, CO2):**
  - Native assembly adhering to Microsoft x64 Calling Convention (`RCX`, `RDX`, `R8`, `R9`, shadow space).
  - Branch-predicted conditional jumps (`jge`, `jle`, `imul`, `sub`).
  - Direct calculation of child concession (50% bit shift `shr rax, 1`) and seat availability validation.
- **ALU Algorithms & Fast String Manipulation (Task 3, CO3):**
  - Hand-crafted Add-and-Shift multiplication algorithm simulating hardware Booth-multiplier logic without hardware `imul`.
  - 64-bit SIMD/pointer string processing: null-terminator scan, case-insensitive string length, ASCII uppercase conversion.

---

## Slide 7: COA Task 4 - 32-Bit RISC Simulator & Pipeline (CO4)
- **Header:** Microarchitectural Simulation: Custom 32-Bit RISC Architecture
- **Instruction Encoding Exactness:**
  - Standard MIPS 32-bit field structure: `opcode` [31:26], `rs` [25:21], `rt` [20:16], `rd` [15:11], `shamt` [10:6], `funct` [5:0].
  - Opcode validation:
    - `MUL R3, R1, R2` $\rightarrow$ `0x00221818` (Special R-type, funct=0x18).
    - `LD R2, 8(R11)` $\rightarrow$ `0x8D620008` (I-type, opcode=0x23).
- **Execution of Required Verification Programs:**
  - Program F1 (Fare Calculation): Computes unit price $\times$ group size with child discount $\rightarrow$ Result: **300**.
  - Program F2 (Seat Capacity Check): Compares group size against remaining seats $\rightarrow$ Branch accept / reject flag.
  - Program F3 (Revenue Accumulator): Loop accumulation over confirmed bookings $\rightarrow$ Totals: **1300 revenue**, **8 bookings processed**, **23 remaining seats**.
- **5-Stage Pipeline Analysis (IF, ID, EX, MEM, WB):**
  - 10-cycle execution trace for 5-instruction window.
  - Load-Use Data Hazard detected: `LD R2, 8(R11)` followed by `MUL R3, R1, R2` forces **1 stall bubble** at Cycle 4.
  - Hardware forwarding bypasses result from MEM/WB to EX stage.
  - Performance Metrics: $\text{CPI} = 2.00$, $\text{Speed-up} = 2.50\times$ over non-pipelined sequential execution.
- **Visual Artifacts:** Interactive browser simulator (`simulator.html`) and 5 vector SVG diagrams (Datapath, ALU, Hardwired, Microprogram ROM, Pipeline).

---

## Slide 8: Computer Graphics - Primitives & Transforms (CG - 2310218L)
- **Header:** Visual Simulation: Mathematical Primitives & Transformations
- **2D Blueprint Rasterization (Task 1, CO1, CO2):**
  - Integer-only Bresenham Line Drawing Algorithm ($|m| \le 1$ and $|m| > 1$, all octants).
  - Midpoint Circle Generation Algorithm (8-way symmetry, decision parameter $d_k = 1 - r$).
  - Layout: Architectural 2D park footprint with ticket booths, pathways, and circular ride pads.
- **3D Hierarchical Matrix Transformations (Task 2, CO3):**
  - Stack-based nested geometric transforms (`glPushMatrix`, `glPopMatrix`).
  - Animated Ferris Wheel: Wheel rotates about Z-axis; 8 passenger gondolas apply inverse rotation $- \theta$ to remain upright against gravity.
  - Roller Coaster: Multi-segment piecewise polynomial path tracking for coaster train.
  - Go-Kart & Water Splash Boat: Parametric coordinate loop animations.
- **Cohen-Sutherland Line Clipping (Task 2, CO5):**
  - 4-bit region outcodes (TOP, BOTTOM, RIGHT, LEFT).
  - Clipping edges against display window before rasterization with trivial reject/accept optimizations.

---

## Slide 9: Computer Graphics - Lighting, Texture & Final Scene (CO4, CO6)
- **Header:** Photorealistic Elements & Integrated 3D Park Scene
- **Procedural Surface Texturing (Task 3, CO6):**
  - Procedural bitmapped textures generated in memory without external image dependencies:
    - Emerald noise grid for park lawn grass.
    - Dark aggregate asphalt for go-kart track.
    - Animated wave shimmer pattern for water flume channel.
    - Interlocking stone paver texture for pedestrian concourses.
- **Phong Illumination Model (Task 3):**
  - Ambient component ($I_a k_a$), Diffuse Lambertian reflection ($I_d k_d (\vec{L} \cdot \vec{N})$), and Specular highlights ($I_s k_s (\vec{R} \cdot \vec{V})^n$).
  - Toggle controls (`L` for lighting, `T` for texturing, `P` for projection perspective/orthographic).
- **Integrated Final Presentation Build (Task 4, CO4):**
  - Full 3D park layout with all 4 rides operating simultaneously.
  - Interactive mouse orbit camera (Left click + drag to pitch/yaw, scroll wheel to zoom).
  - Dual Viewport Display: Main 3D world view $(0, 0, 1000, 720)$ alongside dedicated 2D HUD telemetry viewport $(1000, 0, 280, 720)$ showing live ride occupancy bars and financial indicators.

---

## Slide 10: Ethics, AI Usage & Viva Examination Checklist
- **Header:** Engineering Ethics, Academic Integrity & Viva Readiness
- **Ethical System Design (`docs/ETHICS.md`):**
  - **Data Minimization:** Only visitor name and age captured; no biometric or sensitive private identifiers stored.
  - **Explicit Consent:** Registration form displays clear GDPR-aligned data usage notice.
  - **Financial Audit Integrity:** Cancellation never purges financial history; status transitions to `CANCELLED` preserving revenue forensics.
  - **Transparent Pricing:** All child discounts (50%) explicitly displayed on receipts and screens.
- **AI Tool Usage & Attribution (`docs/AI_USAGE_LOG.md`):**
  - Comprehensive disclosure of Antigravity AI interactions following college rubric guidelines.
  - Explicit manual verification checkpoints: MIPS opcode bitfield alignment, Windows calling convention, thread-safe Swing UI updates.
- **Live Viva Demonstration Checklist:**
  1. Launch Master Menu via `run_all.bat`.
  2. Run Java Dashboard: Register visitor, book ride, verify child discount, inspect revenue chart, trigger undo.
  3. Run DSA Test Suite: Verify zero memory leaks and sorting benchmark timings.
  4. Run COA Simulator: Verify opcodes `0x00221818` / `0x8D620008` and inspect pipeline stall bubble in `simulator.html`.
  5. Run CG Final 3D Render: Orbit camera around park, demonstrate upright Ferris wheel gondolas, toggle lighting/texture keys.
