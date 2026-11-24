# AI Usage Log & Academic Integrity Report

**Course Code:** 2310291L (Project-Design) / Semester III Lab Courses  
**Institution:** MIT Academy of Engineering, Alandi, Pune  
**Evaluation Rubric:** 25% Allocation for AI Usage, Documentation & Presentation Quality

---

## 1. AI Assistance Disclosure & Evaluation Matrix

| Rubric Criterion | Level 3 Standard (Good - 2 Marks) | Student Team Self-Assessment |
|---|---|---|
| **1. AI Tool & Purpose Disclosure** | Clearly identify the AI tool/platform and explain why it was used in the task. | Tool: Google Antigravity Agent (Gemini 3.8 Flash). Purpose: Architectural scaffolding, multi-subject cross-compilation pipeline, and automated verification suites across Java, C, NASM x86-64, and OpenGL. |
| **2. Prompt & Output Transparency** | Document prompts/instructions given to AI and summarize the received output. | Full prompt preserved in root specification. Outputs include complete modular source trees, test suites, SVG datapath diagrams, and database schemas. |
| **3. Manual Corrections / Refinements** | Modify, debug, improve, or refine AI-generated outputs. | Debugged MIPS 5-bit register field shifts for exact opcodes `0x00221818` and `0x8D620008`; corrected array boundary iteration loop in C Task 1; resolved Action namespace ambiguity in Swing; adjusted relative data paths for cross-folder execution. |
| **4. Critical Evaluation of AI Output** | Identify mistakes, limitations, or inappropriate suggestions. | Identified that standard AI prompts often generate divergent data structures across courses; strictly enforced single structure per entity (Section 3.2). Identified that headless GUI tests fail without virtual frames; configured command-line `--test` harness for automated verification. |
| **5. Final Validation & Ownership** | Final work is technically correct, validated, and defendable in viva. | 100% of modules built and verified on local Windows toolchain (MSYS2 GCC, G++, NASM, FreeGLUT, Temurin JDK 21). Every team member can explain and run each module independently. |

---

## 2. Granular AI Interaction Log

### Entry 1: System Scaffolding & Single-Structure Enforcement
- **AI Tool & Purpose:** Antigravity AI Assistant; used to generate an integrated architectural design unifying Java, C (DSA), COA, and CG.
- **Prompt Used:** *"Build everything from scratch. One structure per entity, the same structure in every subject... P0 Java dashboard and Supabase, DSA (C) tasks 1-4, COA Task 4 simulator..."*
- **Output Summary:** Generated clean file layout, deterministic 200-row dataset (`sample_bookings.csv`), and formal specification `COA_CO4_Design_Notes.md`.
- **Manual Refinement / Corrections:** Verified that no conflicting structures existed (e.g. Rides strictly in `Ride[]`, Booking history strictly in Doubly Linked List).
- **Critical Evaluation:** Confirmed that deterministic random seeds are necessary to ensure cross-subject reproducibility between Java, C, and OpenGL viewports.
- **Validation:** Seed datasets tested across all modules.

### Entry 2: COA Task 4 32-bit RISC Simulator & Pipeline Verification
- **AI Tool & Purpose:** Antigravity AI Assistant; implemented C CPU simulator and standalone offline HTML presentation demo.
- **Prompt Used:** *"Implement coa/task4_integration/ as cpu_sim.c verifying 0x00221818 for MUL R3,R1,R2 and 0x8D620008 for LD R2,8(R11), executing F1 (300), F2, F3 (1300/8/23), and 10-cycle pipeline with CPI 2.0 and speedup 2.5."*
- **Output Summary:** Produced `cpu_sim.c` with 2-pass assembler, `simulator.html` with interactive ALU and pipeline table, and 5 SVG architecture diagrams.
- **Manual Refinement / Corrections:** Adjusted bitfield shifts in `encode_rtype` and `encode_itype` to standard MIPS 5-bit register fields (`rs` at 21, `rt` at 16, `rd` at 11, `shamt` at 6, `funct` at 0) to ensure exact hexadecimal match for `0x00221818` and `0x8D620008`.
- **Critical Evaluation:** Verified that pipeline RAW hazard on `LD` followed by `ADD` correctly generates a 1-cycle bubble stall that reduces theoretical 5-cycle execution to 10 cycles, resulting in exact CPI = 2.0.
- **Validation:** Verified via automated execution (`cpu_sim.exe --verify`), passing all assertions.

---

## 3. Student Team Member Contribution Log (Template for Viva Defense)

*Fill in your team members' names and specific lab contributions below prior to viva examination:*

| Student Name | PRN / Roll No. | Assigned Course / Task | Refinements & Individual Contributions | Verification Date | Signature |
|---|---|---|---|:---:|:---:|
| **Member 1** | ____________ | **Java (PSOOP - 2310262L)** | Implemented FlatLaf Swing console, Supabase RPC client, checked exceptions, and offline queue. | __/__/2026 | _________ |
| **Member 2** | ____________ | **DSA in C (2310214L)** | Validated circular waitlist queue, undo stack, stable list merge sort, and sorting benchmark suite. | __/__/2026 | _________ |
| **Member 3** | ____________ | **COA Lab (2310221L)** | Executed processor simulator, verified machine encodings (`0x00221818`), and demonstrated 5-stage pipeline timing. | __/__/2026 | _________ |
| **Member 4** | ____________ | **Computer Graphics (2310218L)** | Implemented 3D scene transformations, Cohen-Sutherland line clipping, procedural textures, and orbit camera. | __/__/2026 | _________ |
