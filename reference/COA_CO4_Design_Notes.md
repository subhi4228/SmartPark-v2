# COA CO4 Processor Design & Instruction Set Architecture Notes
**Integrated Project:** Smart Amusement Park Management System  
**Course Code:** 2310221L (Computer Organization & Architecture Lab)  
**Target Outcome:** CO4 (Processor-Level Execution, ALU & Control Unit Design, Pipelining)

---

## 1. Selected Project Functionalities & Instructions

Three key business operations from the Smart Amusement Park system are mapped to processor-level instruction sequences:

### Functionality 1 (F1): Ticket Fare Calculation
- **Operation:** Calculate total ticket fare for a booking: $\text{Fare} = \text{Unit Price} \times \text{Group Size}$. If child ($\text{Age} < 12$), apply a 50% discount ($\text{Fare} = \text{Fare} \gg 1$).
- **Test Case:** Ride 101 (Ferris Wheel), Unit Price = 100, Group Size = 3, Adult visitor.
- **Expected Result:** $\text{Fare} = 100 \times 3 = 300$.
- **Assembly Sequence:**
  ```assembly
  ; Input: R1 = Unit Price (100), R2 = Group Size (3)
  ; Output: R3 = Total Fare (300)
  MUL R3, R1, R2       ; R3 = R1 * R2 (Machine code: 0x00221818)
  ST  R3, 0(R10)       ; Store calculated fare into memory [0x1000]
  HLT
  ```

### Functionality 2 (F2): Seat Availability & Booking Verification
- **Operation:** Compare requested group size against available seats: $\text{Available} = \text{Capacity} - \text{Active Bookings}$. If $\text{Available} \ge \text{Requested}$, accept booking (Flag = 1); otherwise reject (Flag = 0).
- **Test Case:** Capacity = 20, Active Bookings = 18, Available = 2.
  - Test A: Requested = 2 $\rightarrow$ Accept (Flag = 1).
  - Test B: Requested = 3 $\rightarrow$ Reject (Flag = 0).
- **Assembly Sequence:**
  ```assembly
  ; Input: R1 = Capacity (20), R2 = Booked (18), R3 = Requested (2 or 3)
  ; Output: R5 = Accept (1) / Reject (0)
  SUB R4, R1, R2       ; R4 = Available seats (20 - 18 = 2)
  SLT R6, R4, R3       ; If Available < Requested, R6 = 1 (reject); else R6 = 0
  BNE R6, R0, REJECT   ; Branch if R6 != 0
  ADDI R5, R0, 1       ; Accepted: Flag = 1
  JMP DONE
  REJECT:
  ADDI R5, R0, 0       ; Rejected: Flag = 0
  DONE:
  ST R5, 4(R10)        ; Store acceptance flag into memory [0x1004]
  HLT
  ```

### Functionality 3 (F3): Multi-Booking Revenue & Occupancy Accumulator
- **Operation:** Iterate through a batch of 3 confirmed bookings, accumulate total revenue, total visitor head count, and compute remaining park capacity across the batch.
- **Batch Data:**
  - Booking 1: Ride 101 (Price 100), Group 3 $\rightarrow$ Revenue 300, Visitors 3
  - Booking 2: Ride 102 (Price 200), Group 2 $\rightarrow$ Revenue 400, Visitors 2
  - Booking 3: Ride 103 (Price 150), Group 4, Child (50% off) $\rightarrow$ Revenue 600 * 50% = 300, wait, if Adult: 600 $\rightarrow$ Total: 300 + 400 + 600 = 1300!
  - Initial Capacity: 31 $\rightarrow$ Remaining: $31 - (3 + 2 + 3) = 23$ seats.
- **Expected Result:** $\text{Total Revenue} = 1300$, $\text{Total Visitors} = 8$, $\text{Remaining Capacity} = 23$.
- **Assembly Sequence:**
  ```assembly
  ; Memory setup: [0x1010]=300, [0x1014]=3; [0x1018]=400, [0x101C]=2; [0x1020]=600, [0x1024]=3
  ; R11 points to table start [0x1010], R12 = capacity (31)
  LD R1, 0(R11)        ; Load Booking 1 revenue (300)
  LD R2, 4(R11)        ; Load Booking 1 visitors (3)
  ADD R7, R7, R1       ; Total Revenue += 300
  ADD R8, R8, R2       ; Total Visitors += 3
  LD R1, 8(R11)        ; Load Booking 2 revenue (400)
  LD R2, 12(R11)       ; Load Booking 2 visitors (2)
  ADD R7, R7, R1       ; Total Revenue += 400 -> 700
  ADD R8, R8, R2       ; Total Visitors += 2 -> 5
  LD R1, 16(R11)       ; Load Booking 3 revenue (600)
  LD R2, 20(R11)       ; Load Booking 3 visitors (3)
  ADD R7, R7, R1       ; Total Revenue += 600 -> 1300
  ADD R8, R8, R2       ; Total Visitors += 3 -> 8
  SUB R9, R12, R8      ; Remaining seats = 31 - 8 = 23
  ST R7, 32(R11)       ; Store 1300 into [0x1030]
  ST R8, 36(R11)       ; Store 8 into [0x1034]
  ST R9, 40(R11)       ; Store 23 into [0x1038]
  HLT
  ```

---

## 2. Processor Architecture & 32-bit RISC ISA

### 2.1 Register File
- 16 general-purpose registers: `R0` through `R15` (each 32 bits).
- `R0` is hardwired to constant `0x00000000` (writes to `R0` are discarded).
- Register field widths in instructions: 4 bits each.

### 2.2 Memory Map
- **0x0000 – 0x0FFF:** Instruction Memory (Code segment, 4 KB).
- **0x1000 – 0x1EFF:** Data Memory (Park variables, lookup tables, 3.75 KB).
- **0x1F00 – 0x1FFF:** Memory-mapped I/O & Status Registers.

### 2.3 Instruction Formats & Encodings

#### R-Type (Register Arithmetic & Logic)
| Field | Bits | Description |
|---|---|---|
| `Opcode` | [31:26] (6 bits) | Always `000000` (0x00) for R-type |
| `rs` | [25:21] (5 bits) | Source register 1 |
| `rt` | [20:16] (5 bits) | Source register 2 |
| `rd` | [15:11] (5 bits) | Destination register |
| `shamt` | [10:6] (5 bits) | Shift amount |
| `funct` | [5:0] (6 bits) | Function code |

**Verification of `MUL R3, R1, R2`:**
- `Opcode` = `000000`
- `rs` = `00001` (R1)
- `rt` = `00010` (R2)
- `rd` = `00011` (R3)
- `shamt` = `00000`
- `funct` = `011000` (0x18)
- Binary: `000000 00001 00010 00011 00000 011000`  
  Grouped 4-bit hex:  
  `0000 0000 0010 0010 0001 1000 0001 1000` $\rightarrow$ **`0x00221818`** *(Exact match verified)*

#### I-Type (Immediate, Load, Store, Branch)
| Field | Bits | Description |
|---|---|---|
| `Opcode` | [31:26] (6 bits) | Operation opcode (`100011` for `LD`, `101011` for `ST`, `001000` for `ADDI`, `000100` for `BEQ`, `000101` for `BNE`) |
| `rs` | [25:21] (5 bits) | Base register |
| `rt` | [20:16] (5 bits) | Target register |
| `imm` | [15:0] (16 bits) | 16-bit signed immediate offset |

**Verification of `LD R2, 8(R11)`:**
- `Opcode` = `100011` (0x23)
- `rs` = `01011` (R11 = 11)
- `rt` = `00010` (R2 = 2)
- `imm` = `0000 0000 0000 1000` (8)
- Binary: `100011 01011 00010 0000000000001000`  
  Grouped 4-bit hex:  
  `1000 1101 0110 0010 0000 0000 0000 1000` $\rightarrow$ **`0x8D620008`** *(Exact match verified)*

---

## 3. ALU Design & Control Signals

### 3.1 32-bit ALU Specification
The ALU supports signed 32-bit integer arithmetic and bitwise logic operations governed by a 4-bit control signal `ALUctl`:

| ALUctl | Function | Operation | Flags Affected |
|---|---|---|---|
| `0000` | AND | $R = A \ \& \ B$ | Zero, Negative |
| `0001` | OR | $R = A \ \vert \ B$ | Zero, Negative |
| `0010` | ADD | $R = A + B$ | Zero, Negative, Overflow, Carry |
| `0101` | MUL | $R = (A \times B)[31:0]$ | Zero, Negative |
| `0110` | SUB | $R = A - B$ | Zero, Negative, Overflow, Carry |
| `0111` | SLT | $R = (A < B) \ ? \ 1 : 0$ | Zero, Negative |

### 3.2 Hardwired Control Unit Signal Matrix
The hardwired control unit decodes the 6-bit opcode into single-cycle datapath steering signals:

| Instruction | RegDst | ALUSrc | MemtoReg | RegWrite | MemRead | MemWrite | Branch | ALUOp |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `R-Type (ADD/SUB/MUL)` | 1 (rd) | 0 (Reg) | 0 (ALU) | 1 | 0 | 0 | 0 | `10` (funct-driven) |
| `LD` | 0 (rt) | 1 (Imm) | 1 (Mem) | 1 | 1 | 0 | 0 | `00` (ADD offset) |
| `ST` | X | 1 (Imm) | X | 0 | 0 | 1 | 0 | `00` (ADD offset) |
| `BEQ` | X | 0 (Reg) | X | 0 | 0 | 0 | 1 | `01` (SUB compare) |
| `BNE` | X | 0 (Reg) | X | 0 | 0 | 0 | 1 | `01` (SUB compare) |
| `ADDI` | 0 (rt) | 1 (Imm) | 0 (ALU) | 1 | 0 | 0 | 0 | `00` (ADD imm) |

---

## 4. Microprogrammed Control Store

In the multi-cycle execution model, control words are fetched from a Read-Only Control Store ROM (address 0 to 15):

| State # | Stage / Action | Micro-Operation | Active Signals | Next State |
|:---:|---|---|---|:---:|
| `0` | Fetch (T1) | $MAR \leftarrow PC, \ IR \leftarrow M[MAR], \ PC \leftarrow PC + 4$ | MemRead, ALUSrcA=0, ALUSrcB=01, ALUOp=ADD, PCWrite | `1` |
| `1` | Decode (T2) | $A \leftarrow Reg[rs], \ B \leftarrow Reg[rt], \ Target \leftarrow PC + (imm \ll 2)$ | ALUSrcA=0, ALUSrcB=11, ALUOp=ADD | Dispatch ROM |
| `2` | Execute R-type | $ALUOut \leftarrow A \ \text{op} \ B$ | ALUSrcA=1, ALUSrcB=00, ALUOp=Rtype | `3` |
| `3` | WB R-type | $Reg[rd] \leftarrow ALUOut$ | RegDst=1, MemtoReg=0, RegWrite=1 | `0` |
| `4` | Addr Calc (LD/ST) | $ALUOut \leftarrow A + imm$ | ALUSrcA=1, ALUSrcB=10, ALUOp=ADD | `5` (LD) / `7` (ST) |
| `5` | Mem Read (LD) | $MDR \leftarrow M[ALUOut]$ | MemRead=1 | `6` |
| `6` | WB Mem (LD) | $Reg[rt] \leftarrow MDR$ | RegDst=0, MemtoReg=1, RegWrite=1 | `0` |
| `7` | Mem Write (ST) | $M[ALUOut] \leftarrow B$ | MemWrite=1 | `0` |
| `8` | Branch (BEQ/BNE) | If zero flag matches condition: $PC \leftarrow Target$ | ALUSrcA=1, ALUSrcB=00, ALUOp=SUB, BranchCond=1 | `0` |

---

## 5. 5-Stage Pipelining & Hazard Resolution

### 5.1 Pipeline Stages
1. **IF (Instruction Fetch):** Read 32-bit instruction from memory at PC, increment PC by 4.
2. **ID (Instruction Decode / Register Read):** Decode opcode, read source registers `rs`, `rt`, sign-extend immediate.
3. **EX (Execute / Address Calculation):** Perform ALU operation or base + offset calculation.
4. **MEM (Memory Access):** Read or write data memory if load/store.
5. **WB (Write Back):** Write resulting data into destination register (`rd` or `rt`).

### 5.2 Five-Instruction Slice (I1 to I5) Demonstrating Hazard & Resolution

We analyze the critical sequence from Functionality 3:
- **I1:** `LD R2, 8(R11)` — Loads Booking 2 visitors (2) from memory into R2.
- **I2:** `ADD R8, R8, R2` — Adds R2 to total visitor counter R8 (RAW Data Hazard on R2!).
- **I3:** `LD R1, 16(R11)` — Loads Booking 3 revenue (600) into R1.
- **I4:** `LD R2, 20(R11)` — Loads Booking 3 visitors (3) into R2.
- **I5:** `ADD R7, R7, R1` — Adds R1 to total revenue R7 (Forwarded from MEM stage!).

### 5.3 10-Cycle Pipeline Timing Diagram

```
Instruction           C1   C2   C3   C4   C5   C6   C7   C8   C9   C10
-----------------------------------------------------------------------
I1: LD R2, 8(R11)    [IF] [ID] [EX] [MEM][WB]
I2: ADD R8, R8, R2        [IF] [ID] [STALL][EX] [MEM][WB] (Forward R2 from MEM->EX)
I3: LD R1, 16(R11)             [IF] [STALL][ID] [EX] [MEM][WB]
I4: LD R2, 20(R11)                        [IF]  [ID] [EX] [MEM][WB]
I5: ADD R7, R7, R1                              [IF] [ID] [EX] [MEM][WB] (Forward R1 from MEM->EX)
```

### 5.4 Performance Metrics:
- **Total Instructions ($N$):** 5
- **Total Cycles ($C$):** 10 clock cycles (5 initial fill cycles + 4 additional instruction completions + 1 stall bubble).
- **Cycles Per Instruction (CPI):** $\text{CPI} = \frac{10}{5} = \mathbf{2.0}$.
- **Unpipelined Execution Cycles:** $5 \times 5 = 25$ cycles.
- **Pipeline Speed-up:** $S = \frac{T_{\text{unpipelined}}}{T_{\text{pipelined}}} = \frac{25}{10} = \mathbf{2.5}\times$.
