# Smart Amusement Park Management System (v2)

An integrated, four-course engineering capstone built from scratch for the Semester Exam Reform at **MIT Academy of Engineering (MITAOE), Alandi, Pune**.

---

## 1. Project Overview

* **Domain:** Smart City / Simulation & Visualization
* **Target Environment:** Windows 10/11 (MSYS2 UCRT64, NASM, FreeGLUT, OpenJDK 21)
* **Problem Statement:** Amusement parks frequently manage ride bookings, ticketing, and seat capacities through disparate spreadsheets or paper registers. This leads to overbooking beyond mechanical safety thresholds, fare calculation discrepancies, lost revenue audit logs, and prolonged physical queue bottlenecks.
* **Solution:** An end-to-end management, simulation, and microarchitectural computation ecosystem:
  1. **Java OOP Management Dashboard:** Interactive Swing console (FlatLaf dark theme) with live KPI cards, custom Graphics2D financial charts, ticket generation, undo stack, and Supabase cloud synchronization.
  2. **C Data Structure Engine:** High-performance, memory-safe kernel implementing canonical data structures with guaranteed $O(1)$ and $O(n \log n)$ complexities and 0 memory leaks.
  3. **COA Processor Simulation & Assembly:** 64-bit x86-64 NASM routines and a custom 32-bit pipelined RISC processor simulator verifying exact MIPS instruction signatures (`0x00221818`, `0x8D620008`) with hazard detection and stall bubbles.
  4. **Computer Graphics 3D Park Visualizer:** C++/FreeGLUT visualizer with hand-crafted Bresenham rasterization, Cohen-Sutherland line clipping, hierarchical matrix transformations (rotating Ferris wheel with upright gondolas), Phong lighting, procedural textures, and an orbit camera HUD.

---

## 2. Integrated Lab Courses

| Course Code | Course Name | Semester | Technology Stack |
|---|---|:---:|---|
| **2310262L** | Problem Solving using OOP in Java | III | Java 21, Swing, FlatLaf, Gson, Supabase REST |
| **2310214L** | Programming Lab (DSA in C) | III | C11, GCC (UCRT64), Valgrind / Heap Tracker |
| **2310221L** | Computer Organization & Architecture Lab | III | x86-64 NASM, C11, RISC-32 Simulator, HTML5 |
| **2310218L** | Computer Graphics Lab | III | C++17, FreeGLUT 3.4, OpenGL 4.6, GLU |

---

## 3. Single Structure Per Entity Principle (Section 3.2 Compliance)

To enforce strict architectural cohesion across subjects, **every domain entity is mapped to exactly one canonical data structure** across both Java and C:

| Domain Entity | Canonical Structure | Java Implementation | C Implementation (DSA) | Design Justification |
|---|---|---|---|---|
| **Ride Catalogue** (4 fixed rides) | **Array** | `Ride[] rides` | `Ride rides[MAX_RIDES]` | Fixed bounds, $O(1)$ direct index access, cache locality |
| **Booking History** | **Doubly Linked List** | `LinkedList<Booking>` | Custom DLL (`head`, `tail`) | Dynamic unbounded growth, $O(1)$ append/unlink without memory shifts |
| **Full Ride Waitlist** | **FIFO Queue** | `ArrayDeque<Booking>` (queue) | Circular Queue (`front`, `rear`) | Strict first-come-first-served scheduling policy |
| **Undo Subsystem** | **LIFO Stack** | `ArrayDeque<Action>` (stack) | Array-based Stack (`top`, `items[]`) | Strict last-in-first-out transactional rewind |
| **Fast Booking Lookup** | **Hash Table** | `HashMap<Long, Booking>` | Open Addressing Linear Probing | Average $O(1)$ lookup/cancellation with tombstone support |
| **Ride Lookup by ID** | **Binary Search** | `Arrays.binarySearch` | Hand-written `binary_search_ride()` | $O(\log n)$ search on sorted catalogue array |
| **Visitor Name Search** | **Linear Search** | Stream / Loop on List | Pointer traversal `search_by_name()` | $O(n)$ search required by non-contiguous list storage |
| **History Sorting** | **Merge Sort** | `list.sort(Comparator)` | Hand-written `merge_sort_list()` | Stable $O(n \log n)$ sort using pointer manipulation |

*Note: The `int[24]` hourly footfall array in Java Task 1 stores hourly footfall visitor counts (a separate entity from park rides), in full compliance with rule 3.2.*

---

## 4. Directory Layout

```text
SmartPark_v2/
├── README.md                      # Master project documentation
├── PROGRESS.md                    # Implementation & verification audit checklist
├── run_all.bat                    # Interactive master CLI launcher
├── package_zip.bat                # Automated packaging script for clean zip
├── docs/                          # Comprehensive engineering & academic documentation
│   ├── SUBJECT_MAPPING.md         # Detailed CO mapping & syllabus justification
│   ├── COMPLEXITY.md              # Asymptotic time/space complexity analysis
│   ├── ETHICS.md                  # Privacy, data minimization, and audit integrity
│   ├── AI_USAGE_LOG.md            # AI interaction log following college rubrics
│   ├── ARCHITECTURE.md            # System architecture with Mermaid diagrams
│   ├── SLIDES_OUTLINE.md          # 10-slide presentation & viva guide
│   └── TEST_RESULTS.md            # Empirical benchmark tables and test logs
├── data/                          # Shared data repository
│   ├── rides.csv                  # Canonical 4 rides catalogue
│   └── sample_bookings.csv        # 200 deterministic seed bookings
├── reference/                     # Academic references & specification
│   ├── Exam_Reform_Guidelines.pdf # Examination guidelines
│   ├── Project_Rubrics.pdf        # Lab evaluation rubrics
│   ├── COA_CO4_Design_Notes.md    # Formal specification for COA Task 4
│   └── old_project/               # Legacy project (unzipped, for context only)
├── supabase/                      # Cloud database configuration
│   ├── schema.sql                 # PostgreSQL DDL with FOR UPDATE row locks
│   └── README_SUPABASE.md         # Setup and security documentation
├── java/                          # Java OOP Management Console (P0)
│   ├── src/com/smartpark/         # Clean modular source code
│   │   ├── model/                 # Domain entities (Ride hierarchy, Booking, Action)
│   │   ├── service/               # ParkService managing all canonical structures
│   │   ├── db/                    # Supabase REST client with offline queue
│   │   ├── ui/                    # ParkDashboard (Swing FlatLaf) & RevenueChartPanel
│   │   ├── exceptions/            # Checked exceptions (RideFull, AgeRestriction)
│   │   ├── util/                  # FileRepository<T>, Result<T>, CsvConvertible
│   │   └── tasks/                 # Standalone runners for CO1, CO2, CO3, CO4
│   ├── lib/                       # flatlaf-3.5.4.jar, gson-2.11.0.jar
│   ├── config/                    # supabase.properties.example
│   ├── setup_libs.bat             # Automated library downloader
│   └── run.bat                    # Java build & execution script
├── dsa/                           # Data Structures in C (P0)
│   ├── common/                    # dsa_common.h, dsa_common.c (leak-free memory allocator)
│   ├── task1_array/               # Fixed array CRUD & boundary checks (CO1)
│   ├── task2_linked_list/         # Doubly linked list booking history (CO2)
│   ├── task3_stack_queue/         # Circular waitlist queue & undo stack (CO3)
│   └── task4_search_sort_hash/    # Binary search, hash table, merge sort, benchmarks (CO4/5)
├── coa/                           # Computer Organization & Architecture (P0/P1)
│   ├── task1_cpu_model/           # 64-bit software CPU emulator in C (CO1)
│   ├── task2_asm64/               # x86-64 NASM fare calc & seat checks (CO2)
│   ├── task3_algorithms/          # Add-and-shift multiplier & string SIMD (CO3)
│   └── task4_integration/         # Pipelined 32-bit RISC CPU simulator (CO4)
│       ├── cpu_sim.c              # C simulator verifying 0x00221818 & 0x8D620008
│       ├── simulator.html         # Interactive web-based visualizer
│       └── diagrams/              # 5 vector SVG microarchitectural schematics
└── cg/                            # Computer Graphics in C++/FreeGLUT (P1)
    ├── task1_primitives/          # Bresenham line & midpoint circle blueprint (CO1/2)
    ├── task2_transform_clip/      # 3D transforms, Cohen-Sutherland clipping (CO3/5)
    ├── task3_texture_light/       # Phong illumination & procedural texturing (CO6)
    └── task4_final_render/        # Unified 3D scene, orbit camera, dual viewport HUD (CO4)
```

---

## 5. Prerequisites & Environment Setup

### 5.1 Required Toolchains
1. **Java Development Kit (JDK 21+):**
   * Ensure `java -version` and `javac -version` output JDK 21 or later.
2. **MSYS2 UCRT64 (C/C++ & Assembly):**
   * Installed at `C:\msys64\ucrt64\bin`.
   * Toolchain packages: `gcc`, `g++`, `nasm`, `freeglut`.
   * *All scripts in this project automatically detect and prepend `C:\msys64\ucrt64\bin` to `PATH`.*

---

## 6. Quick Start & Execution

### 6.1 Interactive Master Menu
Launch the interactive master runner from the project root:
```cmd
run_all.bat
```
This launcher allows you to execute any component across Java, DSA, COA, or CG with a single keystroke.

### 6.2 Running Individual Modules
* **Java Management Dashboard:**
  ```cmd
  cd java
  run.bat
  ```
  *(To test in headless mode, run `run.bat --headless` or `run.bat --test`)*

* **DSA in C Test Suites & Benchmarks:**
  ```cmd
  cd dsa\task1_array && run.bat --test
  cd dsa\task2_linked_list && run.bat --test
  cd dsa\task3_stack_queue && run.bat --test
  cd dsa\task4_search_sort_hash && run.bat --test
  ```

* **COA Processor Simulation:**
  ```cmd
  cd coa\task4_integration
  run.bat --verify
  ```
  *(To open the visual web simulator, open `coa\task4_integration\simulator.html` in any browser)*

* **Computer Graphics 3D Visualizer:**
  ```cmd
  cd cg\task4_final_render
  run.bat
  ```
  * **Controls:** Left-click + Drag to Orbit Camera; Scroll to Zoom; `L` to toggle Lighting; `T` to toggle Textures; `P` to toggle Projection.

---

## 7. Supabase Cloud Configuration (Optional)
The system is fully functional offline using local CSV backups. To connect to a live Supabase PostgreSQL instance:
1. Copy `java/config/supabase.properties.example` to `java/config/supabase.properties`.
2. Fill in your project URL and anon public key:
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. Execute `supabase/schema.sql` in the Supabase SQL Editor.
4. *Security Notice: `supabase.properties` is strictly git-ignored to prevent credential leakage.*

---

## 8. Verification & Academic Rubric Conformance

* **Automated Verification:** All tasks include `--test` and `--verify` assertion suites that exit with status 0 upon passing.
* **Memory Safety:** Heap allocations in C are monitored via `safe_malloc` and `safe_free`; 0 bytes are leaked across all test executions.
* **Instruction Signatures:** Exact machine hex codes `0x00221818` (`MUL R3, R1, R2`) and `0x8D620008` (`LD R2, 8(R11)`) are formally verified in COA Task 4.
* **Ethics & AI Attribution:** Full compliance documentation is maintained in `docs/ETHICS.md` and `docs/AI_USAGE_LOG.md`.

---

## 9. Authors & Academic Integrity

Developed for the Second-Year B.Tech Semester Exam Reform Evaluation at **MIT Academy of Engineering, Alandi (D), Pune**.
* **Supervising Faculty:** Department of Computer Engineering & Software Engineering
* **Academic Term:** Academic Year 2026-2027 (Semester III)
