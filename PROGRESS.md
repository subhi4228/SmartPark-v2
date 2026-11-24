# Smart Amusement Park Management System (v2) - Implementation Progress

## Project Metadata
- **Institution:** MIT Academy of Engineering, Alandi, Pune (SY B.Tech Semester III)
- **Courses:**
  - Programming Lab (DSA in C, `2310214L`)
  - Computer Organization & Architecture Lab (`2310221L`)
  - Computer Graphics Lab (`2310218L`)
  - Problem Solving using Object Oriented Programming (Java, `2310262L`)
- **Exam Reform Evaluation:** 75% Lab Implementation + 25% Report / AI Usage / Ethics
- **Final Status:** **100% COMPLETE & VERIFIED**

---

## 1. Single Structure Per Entity Audit Matrix (Section 3.2 Compliance)

| Entity | Canonical Structure | Java Implementation | C Implementation (DSA) | Multi-Structure Prohibited? | Status |
|---|---|---|---|:---:|:---:|
| **Rides Catalogue** (4 fixed rides) | **Array** | `Ride[] rides` | `Ride rides[MAX_RIDES]` | Verified (No linked list used for rides) | **COMPLIANT** |
| **Booking History** | **Doubly Linked List** | `LinkedList<Booking>` | Custom DLL (`head`, `tail` pointers) | Verified (No array resizing used for history) | **COMPLIANT** |
| **Full Ride Waitlist** | **FIFO Queue** | `ArrayDeque<Booking>` (queue) | Circular Queue (`front`, `rear`, `count`) | Verified (No priority queue or list) | **COMPLIANT** |
| **Undo System** | **LIFO Stack** | `ArrayDeque<Action>` (stack) | Array-based Stack (`top`, `items[]`) | Verified (No tree or arbitrary rewind) | **COMPLIANT** |
| **Booking ID Lookup** | **Hash Table** | `HashMap<Long, Booking>` | Open Addressing Linear Probing (`TOMBSTONE`) | Verified (No binary search tree) | **COMPLIANT** |
| **Ride Lookup by ID** | **Binary Search** | `Arrays.binarySearch` | Hand-written `binary_search_ride()` | Verified (Only on sorted ride array) | **COMPLIANT** |
| **Visitor Name Search** | **Linear Search** | Loop / stream over `LinkedList` | Linear list traversal `search_by_name()` | Verified (Only on linked list) | **COMPLIANT** |
| **History Sorting** | **Merge Sort** | `list.sort(Comparator)` | Hand-written stable `merge_sort_list()` | Verified (No quicksort or heap sort) | **COMPLIANT** |

*Audit Note: The `int[24]` hourly footfall array in Java Task 1 stores hourly footfall visitor counts (a separate entity from park rides), in full compliance with rule 3.2.*

---

## 2. Implementation Checklist

### Priority P0: Core Lab Requirements
- [x] Workspace skeleton & reference documents (`Exam_Reform_Guidelines.pdf`, `Project_Rubrics.pdf`, `cg_task2_screenshot.png`, `old_project/`).
- [x] Deterministic seed data: `data/rides.csv` and `data/sample_bookings.csv` (200 records).
- [x] Formal COA Task 4 technical specification: `reference/COA_CO4_Design_Notes.md`.
- [x] **Java OOP & Management Console**:
  - [x] `setup_libs.bat` & libraries (`flatlaf-3.5.4.jar`, `gson-2.11.0.jar`).
  - [x] Task 1 (CO1 Fundamentals): Footfall array, ticket switch, `StringBuilder`/`StringBuffer`.
  - [x] Task 2 (CO2 OOP Design): Abstract `Ride`, subclasses, `Bookable`/`Discountable`, lambdas.
  - [x] Task 3 (CO3 Robustness & UI): Checked exceptions, collection data structures.
  - [x] Task 4 (CO4 Persistence & Generics): `FileRepository<T>`, `Result<T>`, CSV export.
  - [x] Modern Swing Management Dashboard (1280x800): KPI cards, ride occupancy progress bars, custom `Graphics2D` revenue chart, live search/filter/sort, cancel & waitlist promotion, offline sync.
  - [x] Supabase Cloud Integration: `schema.sql` (stored procedures with `FOR UPDATE` row lock), `SupabaseClient` with offline queue.
  - [x] Build & run verification with `java/run.bat`.
- [x] **Programming Lab (DSA in C)**:
  - [x] `dsa/common/`: Shared structs (`Ride`, `Booking`), CSV parser/loader, safe memory tracker.
  - [x] Task 1 (CO1 Array): Ride catalogue CRUD, boundary validations.
  - [x] Task 2 (CO2 Doubly Linked List): History node management, head/tail pointers, forward/reverse traversal, revenue sum.
  - [x] Task 3 (CO3 Stack & Queue): Circular waitlist queue, array-based undo stack, 7/7 boundary test suite.
  - [x] Task 4 (CO4/CO5 Search, Sort, Hash): Binary search, linear search, stable merge sort, linear probing hash table with collision demo.
  - [x] Sorting benchmark suite (Bubble vs Insertion vs Merge sort for N=100, 1000, 10000 across sorted, reversed, random data).
  - [x] Build & run verification for all C tasks with `run.bat` (0 memory leaks).
- [x] **COA Lab Task 4 Integration Simulator (CO4)**:
  - [x] `cpu_sim.c`: 32-bit RISC CPU simulator in C with assembler, single-step/run modes, verifying `0x00221818` and `0x8D620008`, executing F1 (300), F2 (accept/reject), F3 (1300/8/23).
  - [x] `simulator.html`: Interactive single-page visual demo (datapath, ALU, register file, memory, cycle signals, microprogram ROM, 5-stage pipeline diagram with 10 cycles, 1 stall bubble, forwarding, CPI=2.0, speed-up=2.5).
  - [x] SVG diagrams in `diagrams/`: datapath, ALU, hardwired control, microcode store, pipeline timing.

### Priority P1: COA Tasks 1-3 & Computer Graphics
- [x] **COA Tasks 1-3**:
  - [x] Task 1 (CO1): 64-bit CPU model in C (`cpu_model.c`, `run.bat`).
  - [x] Task 2 (CO2): 64-bit NASM x86-64 assembly program for fare calculation & seat checks (`park_calc64.asm`, C runner, `run.bat`).
  - [x] Task 3 (CO3): Add-and-shift multiplication & 64-bit string operations in NASM x86-64 with C driver (`run.bat`).
- [x] **Computer Graphics Lab (C++ with FreeGLUT / OpenGL)**:
  - [x] Task 1 (CO1, CO2): 2D blueprint layout, Bresenham line and midpoint circle algorithms, dashboard frame/buttons, `run.bat`.
  - [x] Task 2 (CO3, CO5): 3D transformations (Ferris wheel, coaster car, kart), Cohen-Sutherland line clipping, secondary `glViewport` for revenue chart, `run.bat`.
  - [x] Task 3 (CO6): Procedural textures (grass, asphalt, water, pavers), Phong lighting model, toggle keys (`L`, `T`, `P`), `run.bat`.
  - [x] Task 4 (CO4): Integrated final render, perspective projection, interactive orbit camera, HUD overlay, dual viewport demo build, `run.bat`.

### Priority P2: Documentation, Packaging & Master Automation
- [x] `docs/SUBJECT_MAPPING.md`
- [x] `docs/COMPLEXITY.md`
- [x] `docs/ETHICS.md`
- [x] `docs/AI_USAGE_LOG.md`
- [x] `docs/ARCHITECTURE.md` (with Mermaid flowchart)
- [x] `docs/SLIDES_OUTLINE.md`
- [x] `docs/TEST_RESULTS.md`
- [x] `README.md` (complete project guide and evaluation guide)
- [x] `run_all.bat` (master interactive runner)
- [x] `package_zip.bat` generating clean `SmartPark_v2.zip`.

---

## 3. Definition of Done Final Sign-Off

1. **No Mixed Data Structures:** Certified 100% compliant with Section 3.2.
2. **Build & Execution:** All modules compile and run cleanly under Windows 11 with MSYS2 UCRT64 GCC/G++, NASM, FreeGLUT, and JDK 21.
3. **Memory Safety:** C/C++ memory allocator reports 0 bytes leaked across all test executions.
4. **Machine Hex Signatures:** COA Task 4 verified `MUL R3, R1, R2` = `0x00221818` and `LD R2, 8(R11)` = `0x8D620008`.
5. **No Tracked Secrets:** `supabase.properties` is omitted; `supabase.properties.example` is supplied.
6. **Packaging:** `SmartPark_v2.zip` contains all source code, assets, docs, and test runners, strictly excluding intermediate binaries.
