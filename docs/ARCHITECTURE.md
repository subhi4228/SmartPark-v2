# System Architecture & Cross-Subject Integration

This document outlines the multi-tiered architecture of the Smart Amusement Park Management System (v2) and visualizes the cross-course data flow unifying Java, C (DSA), COA, and Computer Graphics.

---

## 1. High-Level Architectural Flow

```mermaid
flowchart TD
    subgraph UI_Tier ["User Interface & Presentation Layer"]
        UI_Dash["Java Swing Dashboard\n(FlatLaf 1280x800 Console)"]
        CG_Win["Computer Graphics Window\n(OpenGL / FreeGLUT 3D Simulation)"]
        HTML_Sim["COA CPU Simulator\n(Offline Interactive HTML/JS)"]
    end

    subgraph Service_Tier ["Business & Algorithmic Processing"]
        Java_Svc["ParkService (Java)\n- Capacity Management\n- Concession Logic\n- Waitlist Promotion\n- Multi-Level Undo"]
        C_DSA["DSA Engine (C11)\n- Doubly Linked List History\n- Circular Queue Waitlist\n- Array Undo Stack\n- Open Addressing Hash Table"]
        COA_Core["COA Processor Simulator (C & NASM)\n- 32-bit RISC ALU\n- Hardwired & Microcode Control\n- 5-Stage Pipeline Engine"]
    end

    subgraph Persistence_Tier ["Shared Storage & Cloud Persistence"]
        Local_CSV["Local Data Files\n- data/rides.csv\n- data/sample_bookings.csv\n- data/bookings_export.csv"]
        Supabase_Cloud[("Supabase PostgreSQL\n- Transactional book_ticket() [FOR UPDATE]\n- cancel_booking()\n- RLS Security Policies")]
    end

    %% UI Connections
    UI_Dash -->|Invokes Operations| Java_Svc
    Java_Svc -->|Syncs Async / Queue| Supabase_Cloud
    Java_Svc -->|Offline Fallback & Export| Local_CSV
    
    %% Cross-Course Connections
    Local_CSV -.->|Loads Seed Catalog & Bookings| C_DSA
    Local_CSV -.->|Feeds Revenue Data| CG_Win
    C_DSA -.->|Translates Calculations to Instructions| COA_Core
    COA_Core -->|Drives Execution & CPI Visuals| HTML_Sim
```

---

## 2. Cross-Module Data Sharing Workflow

1. **Catalogue Definition:** `data/rides.csv` defines the canonical 4 rides (ID, Name, Capacity, Unit Price, Min Age). Both the Java Swing Dashboard and the C DSA Engine load this identical CSV at boot.
2. **Transaction Export:** The Java Dashboard features an "Export CSV" facility that writes `data/bookings_export.csv`.
3. **Graphics Visualization:** The Computer Graphics module reads the ride status and revenue totals to dynamically size the Cohen-Sutherland clipped bar chart in its secondary viewport.
4. **Processor-Level Simulation:** The COA simulator extracts critical operations (Fare Multiplication, Seat Verification, and Batch Revenue Accumulation) and demonstrates their cycle-accurate execution at the ALU and pipeline level.
