# Subject Mapping & Course Outcomes Matrix

**Project:** Smart Amusement Park Management System (v2)  
**Academic Context:** MIT Academy of Engineering, Alandi, Pune (SY B.Tech Semester III)

---

## 1. Single Structure Principle: Cross-Subject Alignment (Section 3.2 Audit)

The project strictly guarantees that each domain entity is implemented using **exactly one canonical data structure** across both Java and C modules:

| Entity | Canonical Data Structure | Java Implementation (`java/`) | C Implementation (`dsa/`) | Cross-Subject Justification |
|---|---|---|---|---|
| **Ride Catalogue** (4 fixed rides) | **Array** | `Ride[] rides` in `ParkService` | `Ride rides[MAX_RIDES]` in `task1_array` & `common` | Fixed small catalogue (4 items); provides $O(1)$ random indexing and optimal cache locality. |
| **Booking History** | **Doubly Linked List** | `LinkedList<Booking>` in `ParkService` | Custom Doubly Linked List with `head` & `tail` | Unbounded dynamic growth; allows $O(1)$ append at tail and $O(1)$ status unlinking without array shifting. |
| **Ride Waitlist** | **FIFO Queue** | `ArrayDeque<Booking>` as queue | Circular Queue with `front` and `rear` | Enforces first-come, first-served fair ticket promotion for full attractions. |
| **Undo System** | **LIFO Stack** | `ArrayDeque<Action>` as stack | Array-based Stack with `top` | Reverses last confirmed or cancelled operations in reverse chronological order ($O(1)$ push/pop). |
| **Booking ID Lookup** | **Hash Table** | `HashMap<Long, Booking>` | Open Addressing Hash Table (Linear Probing, Tombstones) | Instant $O(1)$ average cancellation & verification by booking ID. |
| **Ride Lookup by ID** | **Binary Search** | `Arrays.binarySearch` on `Ride[]` | Hand-written `binary_search_ride()` on `rides[]` | $O(\log n)$ lookup on ordered array. |
| **Visitor Name Search** | **Linear Search** | Loop/Stream over `LinkedList` | Linear traversal over DLL nodes | Linked lists lack random indexing ($O(n)$ search). |
| **History Sorting** | **Merge Sort** | `bookingHistory.sort(Comparator)` | Hand-written stable `dll_merge_sort()` | $O(n \log n)$ stable sort preserving chronological order of equal values. |

---

## 2. Comprehensive Course Outcomes (CO) Mapping

### 2.1 Problem Solving using OOP in Java (`2310262L`)
- **CO1 (OOP Fundamentals):** `Task1Fundamentals.java`  
  *Concepts:* Control statements, loops, hourly visitor counts in `int[24]` footfall array, ticket category `switch`, `StringBuilder` receipt formatting, thread-safe `StringBuffer` audit logging.
- **CO2 (Design & Inheritance):** `Task2Design.java`, `model/Ride.java`, `model/ThrillRide.java`, `model/FamilyRide.java`, `model/WaterRide.java`, `model/KartRide.java`  
  *Concepts:* Abstract base class, inheritance hierarchy, interfaces `Bookable` & `Discountable`, `super()` constructor chaining, polymorphic `calculateFare()`, Lambda predicates and comparators.
- **CO3 (Robustness & UI):** `Task3RobustnessUI.java`, `exceptions/*`, `ui/ParkDashboard.java`  
  *Concepts:* Custom checked exceptions (`RideFullException`, `AgeRestrictionException`, `InvalidVisitorException`), Swing GUI integration, collections (`LinkedList`, `ArrayDeque`, `HashMap`).
- **CO4 (Persistence & Generics):** `Task4PersistenceGenerics.java`, `util/FileRepository.java`, `util/Result.java`, `db/SupabaseClient.java`  
  *Concepts:* Generic repository `FileRepository<T extends CsvConvertible>`, try-with-resources I/O, `Result<T>` container, offline-fallback queueing and cloud synchronization.

### 2.2 Programming Lab - DSA in C (`2310214L`)
- **CO1 (Array Applications):** `dsa/task1_array/task1_array.c`  
  *Concepts:* Array-based ride catalogue with insert, delete (with shifting), update, linear lookup, duplicate key rejection, and boundary capacity checks.
- **CO2 (Linked Lists):** `dsa/task2_linked_list/task2_linked_list.c`  
  *Concepts:* Doubly linked list with explicit head and tail pointers, $O(1)$ tail insertion, bidirectional traversal, revenue calculation, and zero memory leaks.
- **CO3 (Stack & Queue Scenarios):** `dsa/task3_stack_queue/task3_stack_queue.c`  
  *Concepts:* Circular waitlist queue (FIFO) and array-based undo stack (LIFO) with boundary test suite (empty queue/stack, full queue wrap-around, cancellation promotion).
- **CO4 & CO5 (Searching, Sorting & Hashing):** `dsa/task4_search_sort_hash/task4_search_sort_hash.c`  
  *Concepts:* Binary search on sorted array, linear search on linked list, stable merge sort on linked list, open-addressing hash table with linear probing and tombstone deletion, deliberate collision demonstration, and sorting benchmarks across $N \in \{100, 1000, 10000\}$.

### 2.3 Computer Organization & Architecture Lab (`2310221L`)
- **CO1 (Processor Modeling):** `coa/task1_cpu_model/cpu_model.c`  
  *Concepts:* 64-bit integer registers, ALU operations (ADD, SUB, MUL, CMP), and control flow simulation of park fare and seat checks.
- **CO2 (64-bit Assembly):** `coa/task2_asm64/park_calc64.asm`  
  *Concepts:* NASM x86-64 assembly implementing fare calculation (`calculate_fare_x64`) and seat availability verification (`check_seats_x64`) using comparison and conditional branches under Windows x64 ABI.
- **CO3 (Algorithmic & String Processing):** `coa/task3_algorithms/algo_string_proc64.asm`  
  *Concepts:* Binary add-and-shift multiplication algorithm (`add_and_shift_mult_x64`) and 64-bit string operations (`string_length_x64`, `string_to_upper_x64`).
- **CO4 (Processor Integration & Pipelining):** `coa/task4_integration/cpu_sim.c`, `simulator.html`, `diagrams/`  
  *Concepts:* 32-bit RISC processor simulator with assembler verifying `MUL R3,R1,R2` (`0x00221818`) and `LD R2,8(R11)` (`0x8D620008`), executing F1 (300), F2 (accept/reject), F3 (1300/8/23), hardwired and microprogrammed control stores, and 5-stage pipeline timing model (10 cycles, 1 stall bubble, forwarding, CPI=2.0, speed-up=2.5x).

### 2.4 Computer Graphics Lab (`2310218L`)
- **CO1 & CO2 (Primitives & Drawing Algorithms):** `cg/task1_primitives/main.cpp`  
  *Concepts:* OpenGL primitives, 2D park layout blueprint, custom hand-written Bresenham line algorithm, and Midpoint circle drawing algorithm.
- **CO3 & CO5 (Transformations & Clipping):** `cg/task2_transform_clip/main.cpp`  
  *Concepts:* Hierarchical 3D transformations (`glPushMatrix`, `glRotatef`, `glTranslatef`, `glScalef`), rotating Ferris wheel with upright gondolas, moving coaster car, hand-written Cohen-Sutherland line clipping algorithm, and dual `glViewport` with revenue chart.
- **CO6 (Texture Mapping & Lighting):** `cg/task3_texture_light/main.cpp`  
  *Concepts:* Procedural texture synthesis (grass, asphalt, water ripple, pavers), Phong illumination model (ambient, diffuse, specular, shininess), and interactive keyboard toggles (`L`, `T`, `P`).
- **CO4 (Final Integrated 3D Presentation):** `cg/task4_final_render/main.cpp`  
  *Concepts:* Perspective projection with aspect ratio preservation, interactive orbit camera (mouse drag / arrow keys), real-time animations of all 4 rides, and secondary HUD viewport.
