# Algorithmic Time & Space Complexity Reference

This document details the theoretical and empirical complexity metrics for each operational data structure in the Smart Amusement Park Management System.

---

## 1. Master Operation Complexity Table

| Entity / Operation | Chosen Data Structure | Best Time | Average Time | Worst Time | Space Complexity | Access Pattern Justification |
|---|---|:---:|:---:|:---:|:---:|---|
| **Ride Lookup by ID** | Fixed Array (`Ride[]`) | $O(1)$ | $O(\log n)$ | $O(\log n)$ | $O(1)$ auxiliary | Ride catalogue is small (4-10) and fixed in advance. Binary search on sorted array takes $O(\log n)$ with zero allocation. |
| **Direct Ride Array Access** | Fixed Array (`Ride[]`) | $O(1)$ | $O(1)$ | $O(1)$ | $O(n)$ total ($n \le 10$) | Direct indexing via array index provides instantaneous $O(1)$ access and maximum cache line locality. |
| **New Booking Appending** | Doubly Linked List (DLL) | $O(1)$ | $O(1)$ | $O(1)$ | $O(1)$ per node | Using an explicit `tail` pointer enables strictly $O(1)$ tail appends without the reallocation overhead of dynamic arrays. |
| **Booking Traversal (Fwd/Rev)** | Doubly Linked List (DLL) | $O(n)$ | $O(n)$ | $O(n)$ | $O(1)$ auxiliary | Chronological forward and reverse audit displays require linear node traversal. |
| **Booking ID Fast Lookup** | Open Addressing Hash Table | $O(1)$ | $O(1)$ | $O(n)$ | $O(m)$ table size | Linear probing with a maximum load factor of $0.70$ guarantees $O(1)$ average time for ticket cancellation. |
| **Booking Cancellation** | Hash Table + DLL Node update | $O(1)$ | $O(1)$ | $O(n)$ | $O(1)$ auxiliary | Hash table locates the exact DLL node in $O(1)$ average time, unlinking or updating status in $O(1)$ without element shifting. |
| **Waitlist Enqueue (FIFO)** | Circular Queue | $O(1)$ | $O(1)$ | $O(1)$ | $O(k)$ capacity ($k=16$) | Pre-allocated circular ring buffer avoids memory fragmentation and guarantees strict $O(1)$ first-come, first-served queueing. |
| **Waitlist Dequeue / Promotion** | Circular Queue | $O(1)$ | $O(1)$ | $O(1)$ | $O(1)$ auxiliary | Vacated seats immediately promote the front waitlist item in $O(1)$ time. |
| **Undo Last Operation (LIFO)** | Array-based Stack | $O(1)$ | $O(1)$ | $O(1)$ | $O(s)$ capacity ($s=32$) | Stack top index tracks operations; push and pop execute in $O(1)$ time. |
| **Visitor Name Linear Search** | Doubly Linked List (DLL) | $O(1)$ | $O(n)$ | $O(n)$ | $O(k)$ matching results | Linked lists lack random indexing, necessitating an $O(n)$ linear traversal. |
| **Booking History Sorting** | Stable Linked List Merge Sort | $O(n \log n)$ | $O(n \log n)$ | $O(n \log n)$ | $O(\log n)$ stack | Merge sort operates natively on linked lists without copying elements to arrays, maintaining stability in $O(n \log n)$ across all data distributions. |

---

## 2. Empirical Sorting Comparison Rationale

As tested in `dsa/task4_search_sort_hash`:
- **Bubble Sort:** Degrades to $O(n^2)$ when inputs are reversed or randomized ($48.0$ ms for $N=10000$).
- **Insertion Sort:** Degrades to $O(n^2)$ on reversed datasets ($13.0$ ms for $N=10000$), but runs in $O(n)$ on pre-sorted data.
- **Merge Sort:** Maintains optimal $O(n \log n)$ performance across all input distributions ($1.0 - 2.0$ ms for $N=10000$), preserving the stable chronological order of transactions with identical values.
