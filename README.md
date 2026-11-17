
# Algorithm Explorer

**CSC2107: Design and Analysis of Algorithms**  
**Uganda Christian University (UCU)**  
**Group 18**

## Group Members

| No. | Student name | Registration number |
|---:|---|---|
| 1 | Isaac Isota | M25B23/043 |
| 2 | Teta Nadine Tracy | S25B23/010 |
| 3 | Nyakecho Karen Faith | M25B23/020 |
| 4 | Ainebyona Sheba | M25B23/002 |
| 5 | Precious Abangira Nimmusiima | M24B23/028 |

---

## 1. Project Overview

Algorithm Explorer is an interactive application developed for CSC2107: Design and Analysis of Algorithms. It allows users to enter their own inputs, execute algorithms, and explore how those algorithms work through a web-based interface.

The application presents:

- The result produced by an algorithm.
- The execution steps, which can be replayed using play, pause, step, scrub and speed controls.
- Operation counts, including comparisons, swaps, writes, arithmetic operations, recursive calls and maximum recursion depth.
- Running-time measurements based on the median of five timed runs, with step recording switched off for those measurements.

The project demonstrates recursion, data structures, sorting, searching and time-complexity analysis. It runs locally and does not require an internet connection, external API, framework or third-party library.

## 2. Project Objectives

- Implement and demonstrate algorithms and data structures.
- Allow users to provide custom inputs and observe algorithm execution.
- Present algorithm results and execution steps in an interactive interface.
- Count relevant operations and measure running time.
- Explore theoretical and experimental time complexity.
- Strengthen practical understanding of algorithm design and analysis.

## 3. Main Features

### 3.1 Sorting Algorithms

The application implements seven sorting algorithms:

1. Bubble sort
2. Selection sort
3. Insertion sort
4. Merge sort
5. Quick sort
6. Heap sort
7. Counting sort

The visualisation shows comparisons, swaps and writes, as well as the pivot, active sub-array and elements already in their final positions. Up to 120 numbers can be animated, while larger inputs can be counted and timed up to 100,000 numbers.

### 3.2 Searching Algorithms

Three searching algorithms are provided:

- Linear search
- Iterative binary search
- Recursive binary search

The interface displays how the search range changes. The recursive version also displays its call stack.

### 3.3 Recursion

The recursion section includes:

- Factorial
- Fibonacci using naive recursion
- Fibonacci using memoisation
- Iterative Fibonacci for comparison
- Tower of Hanoi
- Fast power using exponentiation by squaring
- Euclid's greatest common divisor algorithm
- Permutations using backtracking
- Recursive palindrome checking

The application visualises the recursion tree and call stack as execution proceeds.

### 3.4 Data Structures and Applications

The project implements the following data structures:

| Data structure | Implementation and supported operations |
|---|---|
| Dynamic array | Resizable array |
| Integer list | Growable integer array |
| Stack | Array-based stack with push, pop, peek, size and isEmpty |
| Queue | Circular-array queue with enqueue, dequeue, peek and size |
| Singly linked list | Head and tail pointers; insertion, removal, search, retrieval and reversal |
| Binary search tree | Insertion, search, deletion, traversals, height, minimum and maximum |
| Min-heap | Array-based binary heap with insertion, extract-min, peek and heap construction |
| Hash table | Separate chaining, custom hash function and resizing |

The application also demonstrates two stack-based applications:

- **Balanced brackets:** checks matching parentheses, square brackets and braces.
- **Expression evaluator:** uses the shunting-yard method and postfix evaluation to evaluate expressions.

### 3.5 Time-Complexity Analysis

Users can select algorithms, input sizes and input arrangements, including random, sorted, reversed, nearly sorted and few-distinct-value inputs.

The analysis displays:

- Operation counts against input size.
- Running time against input size.
- Optional log-log views and measurement details.
- A growth exponent calculated from the slope of log(operations) against log(input size).
- A best-fit growth model selected from log n, n, n log n, n², n³ and 2ⁿ.
- A comparison between observed behaviour and theoretical complexity.
- A table of measurements.

A recursion analysis mode is also available for Fibonacci, Tower of Hanoi, factorial and fast power.

## 4. Algorithms and Theoretical Complexity

The following table summarises the theoretical complexities presented by the application.

| Algorithm | Best case | Average case | Worst case | Extra space |
|---|---|---|---|---|
| Bubble sort | O(n) | O(n²) | O(n²) | O(1) |
| Selection sort | O(n²) | O(n²) | O(n²) | O(1) |
| Insertion sort | O(n) | O(n²) | O(n²) | O(1) |
| Merge sort | O(n log n) | O(n log n) | O(n log n) | O(n) |
| Quick sort | O(n log n) | O(n log n) | O(n²) | O(log n) stack |
| Heap sort | O(n log n) | O(n log n) | O(n log n) | O(1) |
| Counting sort | O(n + k) | O(n + k) | O(n + k) | O(n + k) |
| Linear search | O(1) | O(n) | O(n) | O(1) |
| Binary search | O(1) | O(log n) | O(log n) | O(1) iterative; O(log n) recursive |

### Selected Recurrence Relations

| Recursive algorithm | Recurrence or approach | Time complexity |
|---|---|---|
| Factorial | T(n) = T(n−1) + O(1) | O(n) |
| Naive Fibonacci | T(n) = T(n−1) + T(n−2) + O(1) | O(φⁿ) |
| Memoised Fibonacci | Each subproblem is solved once | O(n) |
| Tower of Hanoi | T(n) = 2T(n−1) + 1 | O(2ⁿ) moves |
| Fast power | T(n) = T(n/2) + O(1) | O(log n) |
| Euclid's GCD | gcd(a, b) = gcd(b, a mod b) | O(log min(a, b)) |
| Permutations | T(n) = n·T(n−1) + O(n) | O(n · n!) |
| Merge sort | T(n) = 2T(n/2) + O(n) | O(n log n) |

## 5. Implementation and Design

### Technology

- **Programming language:** Java
- **Interface:** HTML, CSS and JavaScript
- **Execution:** Local Java HTTP server
- **Requirement:** Java Development Kit (JDK) 11 or newer

The browser interface sends input to the Java back end. The back end validates the input, executes the selected algorithm, records the steps and returns the results as JSON. The browser then displays the results and replays the recorded steps.

### Implementation Constraints

The algorithms and data structures are implemented from scratch in accordance with the project requirements.

The Java implementation does not use `java.util` collection classes or built-in sorting methods. It does not use ready-made `ArrayList`, `LinkedList`, `Stack`, `ArrayDeque`, `HashMap`, `PriorityQueue`, `Arrays.sort` or `Collections.sort`.

The project includes its own implementations of the data structures and supporting components such as the JSON writer, random-number generator, input parser and web-form parser.

The JavaScript interface is responsible for displaying and replaying results. Algorithm execution takes place in the Java back end.

## 6. Operation Counting and Running-Time Measurement

A tracing component records operations during algorithm execution.

| Counter | What it records |
|---|---|
| Comparisons | Comparisons between elements and relevant base-case tests |
| Swaps | Exchanges of two elements |
| Writes | Element writes, shifts and placements |
| Arithmetic | Arithmetic operations in recursive algorithms |
| Calls | Recursive calls |
| Maximum depth | Deepest recorded recursion depth |
| Total | Sum of the recorded operation categories |

For timing, the application first runs the algorithm with step recording enabled to produce the replay. It then performs five additional runs with recording disabled and reports the median time measured using `System.nanoTime()`. Very fast searches are repeated 200 times per measurement and averaged.

Running times may vary because of the Java Virtual Machine and the computer's workload. Operation counts are used as a more consistent basis for analysing growth.

## 7. Project Structure

```text
AlgorithmExplorer/
├── run.sh
├── run.bat
├── test.sh
├── test.bat
├── src/
│   └── explorer/
│       ├── ds/
│       │   ├── DynamicArray.java
│       │   ├── IntList.java
│       │   ├── ArrayStack.java
│       │   ├── CircularQueue.java
│       │   ├── SinglyLinkedList.java
│       │   ├── BinarySearchTree.java
│       │   ├── MinHeap.java
│       │   └── HashTable.java
│       ├── core/
│       │   ├── Tracer.java
│       │   ├── Json.java
│       │   ├── Rng.java
│       │   └── Input.java
│       ├── algorithms/
│       │   ├── Sorting.java
│       │   ├── Searching.java
│       │   ├── Recursion.java
│       │   ├── DataStructures.java
│       │   ├── Analyzer.java
│       │   └── Catalog.java
│       ├── server/
│       │   ├── Main.java
│       │   ├── Api.java
│       │   └── Params.java
│       └── SelfTest.java
└── web/
    ├── index.html
    ├── style.css
    └── app.js
```

## 8. Installation and Running

### Requirements

Install JDK 11 or newer. Check the installed version with:

```bash
javac -version
```

### Windows

Start the application:

```bat
run.bat
```

Run automated tests:

```bat
test.bat
```

### macOS or Linux

Make the scripts executable:

```bash
chmod +x run.sh test.sh
```

Start the application:

```bash
./run.sh
```

Run automated tests:

```bash
./test.sh
```

Open the application in a browser at:

**http://localhost:8080**

To use another port, provide it as an argument, for example:

```bash
./run.sh 9090
```

Stop the server using `Ctrl+C`.

### Manual Java Commands

Compile the source:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

Start the server:

```bash
java -cp out explorer.server.Main 8080
```

Run the self-test:

```bash
java -cp out explorer.SelfTest
```

## 9. Testing

The project includes a self-test suite. The supplied technical documentation reports **2,159 automated checks**.

The tests cover sorting results, operation counts, known algorithm outputs, data structure behaviour, expression evaluation, bracket checking and input validation.

Examples include checking sorting algorithms against random arrays, verifying expected comparison counts, testing stack and queue behaviour, testing binary search tree deletion cases, checking heap ordering and testing hash-table resizing.

Run the test script for your operating system to execute the available checks.

## 10. Demonstration Guide

A short demonstration can include:

1. **Sorting:** Run quick sort on an input array and inspect its partitioning steps. Compare random input with already sorted input.
2. **Recursion:** Compare naive and memoised Fibonacci to observe the difference in recursive calls.
3. **Data structures:** Demonstrate stack operations, binary search tree operations and hash-table resizing.
4. **Complexity analysis:** Compare selected sorting algorithms on sorted and random inputs and examine their measured growth.

## 11. Academic Information

| Item | Details |
|---|---|
| Institution | Uganda Christian University (UCU) |
| Course unit | CSC2107: Design and Analysis of Algorithms |
| Project title | Algorithm Explorer |
| Group | 18 |
| Group size | Five students |

---

**Algorithm Explorer** demonstrates the implementation, execution, visualisation and analysis of algorithms and data structures through an interactive local application.
