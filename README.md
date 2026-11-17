# Algorithm Explorer

**CSC2107: Design and Analysis of Algorithms**  
**Uganda Christian University (UCU)**  
**Group 18**

Repository: https://github.com/abangiraprecious/algorithm-explorer-group18

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

Algorithm Explorer is an interactive application built for CSC2107: Design and Analysis of Algorithms. The user enters an input in the browser, a Java back end runs the chosen algorithm, and the interface shows how the algorithm worked.

For every run, the application shows:

- The result produced by the algorithm.
- Every step taken, which can be replayed with play, pause, step, scrub and speed controls.
- Operation counts: comparisons, swaps, writes, arithmetic operations, recursive calls and maximum recursion depth.
- The running time, taken as the average of five timed runs with step recording switched off.

The project covers recursion, data structures, sorting, searching and time-complexity analysis. It runs entirely on the local machine. It needs no internet connection, external API, framework or third-party library.

## 2. Project Objectives

- Implement algorithms and data structures from scratch.
- Let users supply their own inputs and watch each algorithm run.
- Present results and execution steps in an interactive interface.
- Count basic operations and measure running time.
- Compare theoretical complexity with measured growth.
- Strengthen practical understanding of algorithm design and analysis.

## 3. Main Features

### 3.1 Sorting Algorithms (Module C)

The five sorting algorithms required by the brief:

1. Bubble sort
2. Selection sort
3. Insertion sort
4. Merge sort
5. Quick sort, with the **first element as the pivot** and the **yellow/green partition** taught in class, plus a second option with a **randomised pivot**

Heap sort and counting sort are included as extras.

The animation shows each comparison, swap and write, together with the pivot, the yellow (smaller than the pivot) and green (greater than or equal) parts of a quick sort partition, the active sub-array and the elements already in their final positions. The *After each pass only* option shows the array after every pass (bubble, selection, insertion) or after every merge or partition step (merge sort, quick sort), with Step and Play (auto-play) buttons. Up to 120 numbers are animated. Up to 100,000 numbers are counted and timed.

The **Compare all** button runs bubble, selection, insertion, merge and both quick sorts on the same input and shows comparisons, swaps, moves, recursive calls, running time, best/average/worst case, extra space and stability side by side in one table.

### 3.2 Searching Algorithms

- Linear search
- Iterative binary search
- Recursive binary search

The interface shows the search range shrinking. The recursive version also shows its call stack.

### 3.3 Recursion (Module A)

- Fibonacci in three ways: naive recursion (limited to n ≤ 30, with the reason shown on screen), memoised recursion and bottom-up dynamic programming. Each shows the result, the number of calls or loop iterations, and the time.
- Factorial, both recursive and iterative. n = 0 is handled (0! = 1) and negative input is rejected with a clear message.
- Tower of Hanoi, 1 to 10 disks, with the source, destination and auxiliary pegs chosen by the user. Every move is listed ("Move disk 1 from A to C"), the disks are animated from peg to peg with Start, Pause, Step, Reset and speed controls, and a running move counter confirms that the total is 2ⁿ − 1.
- Extras: fast power, Euclid's greatest common divisor, permutations by backtracking and a recursive palindrome check.

Each recursive run draws the recursion tree and the call stack as execution proceeds.

### 3.4 Data Structures and Applications (Module B)

Every structure is our own class. Each operation has its own button (it adds the command to the list and runs it), and the structure is redrawn after every operation with a message such as "Popped 42" or "Queue is full". Commands can also be typed, one per line.

| Data structure | Implementation and supported operations |
|---|---|
| Stack | Array-based, **fixed capacity set by the user**: push, pop, peek, size, is_empty, is_full, display; overflow and underflow messages |
| Linear queue | Fixed-size array: enqueue, dequeue, front, size, is_empty, is_full, display; demonstrates the **false full** problem |
| Circular queue | Fixed-size ring buffer: the same operations, with the front and rear indices shown so the rear can be seen wrapping round to slot 0 |
| Singly linked list | Own `Node` objects with `next`: insert at the beginning, at the end, before and after a given element; delete at the beginning, at the end, before and after a given element, and a given element; search, size, display (also insert at a position, get and reverse) |
| Doubly linked list | Own `Node` objects with `prev` and `next`: the same operations, plus display backward |
| Binary search tree | Insertion, search, deletion (all three cases), traversals, height, minimum and maximum (extra) |
| Min-heap | Array-based binary heap with insertion, extract-min, peek and O(n) heap construction (extra) |
| Hash table | Separate chaining, own hash function, doubles when the load factor passes 0.75 (extra) |
| Dynamic array, integer list | Growable arrays used inside the program |

Two stack-based applications are included:

- **Balanced brackets:** checks an expression such as `{ ( [ ] ) }` with our stack and shows the stack after each symbol.
- **Expression evaluator:** converts an expression to postfix with the shunting-yard algorithm, then evaluates the postfix form (extra).

### 3.5 Time-Complexity Analysis (Module D)

The analysis page runs the chosen algorithms on growing input sizes (by default n = 100, 500, 1,000, 2,000, 5,000 and 10,000) for a chosen input order: random, already sorted, reversed, nearly sorted or few distinct values. Each size is timed 3, 5 or 7 times and the **average** is reported.

The analysis page shows:

- Operation counts against input size.
- Running time against input size.
- An optional log-log view.
- A growth exponent, calculated as the slope of log(operations) against log(input size).
- A best-fit growth model chosen from log n, n, n log n, n², n³ and 2ⁿ.
- A comparison between the measured growth and the theoretical complexity for that input.
- A table of every measurement.

A recursion mode does the same for naive and memoised Fibonacci, Tower of Hanoi, factorial and fast power.

For the report, `bench.sh` / `bench.bat` runs the full Module D experiment from the command line: every sort from Module C on random, sorted and reversed input for n = 100, 500, 1,000, 2,000, 5,000 and 10,000, plus merge sort and both quick sorts for n = 50,000 and 100,000, each repeated (3 times by default) and averaged. The results are written to `results/module_d.csv`.

## 4. Algorithms and Theoretical Complexity

| Algorithm | Best case | Average case | Worst case | Extra space |
|---|---|---|---|---|
| Bubble sort | O(n) | O(n²) | O(n²) | O(1) |
| Selection sort | O(n²) | O(n²) | O(n²) | O(1) |
| Insertion sort | O(n) | O(n²) | O(n²) | O(1) |
| Merge sort | O(n log n) | O(n log n) | O(n log n) | O(n) |
| Quick sort, first-element pivot | O(n log n) | O(n log n) | O(n²) (sorted or reversed input) | O(log n) stack on average, O(n) worst |
| Quick sort, random pivot | O(n log n) | O(n log n) expected | O(n²), very unlikely | O(log n) stack expected |
| Heap sort | O(n log n) | O(n log n) | O(n log n) | O(1) |
| Counting sort | O(n + k) | O(n + k) | O(n + k) | O(n + k) |
| Linear search | O(1) | O(n) | O(n) | O(1) |
| Binary search | O(1) | O(log n) | O(log n) | O(1) iterative; O(log n) recursive |

### Recurrence Relations

| Recursive algorithm | Recurrence or approach | Time complexity |
|---|---|---|
| Factorial, recursive | T(n) = T(n−1) + O(1) | O(n) time, O(n) stack |
| Factorial, iterative | One loop, n − 1 iterations | O(n) time, O(1) space |
| Naive Fibonacci | T(n) = T(n−1) + T(n−2) + O(1) | O(φⁿ), φ ≈ 1.618 |
| Memoised Fibonacci | Each subproblem is solved once | O(n) |
| Bottom-up Fibonacci (DP) | table[i] = table[i−1] + table[i−2] | O(n) time, O(n) table |
| Tower of Hanoi | T(n) = 2T(n−1) + 1 | 2ⁿ − 1 moves |
| Fast power | T(n) = T(n/2) + O(1) | O(log n) |
| Euclid's GCD | gcd(a, b) = gcd(b, a mod b) | O(log min(a, b)) |
| Permutations | T(n) = n·T(n−1) + O(n) | O(n · n!) |
| Merge sort | T(n) = 2T(n/2) + O(n) | O(n log n) |

## 5. Implementation and Design

### Technology

- **Programming language:** Java
- **Interface:** HTML, CSS and JavaScript
- **Server:** the JDK's built-in HTTP server, running locally
- **Requirement:** Java Development Kit (JDK) 11 or newer

The browser sends the user's input to the Java back end. The back end validates the input, runs the selected algorithm, records the steps and returns the results as JSON. The browser then displays the results and replays the recorded steps.

### Implementation Constraints

All algorithms and data structures are written from scratch, as the project requires.

The Java code never imports `java.util`. It does not use `ArrayList`, `LinkedList`, `Stack`, `ArrayDeque`, `HashMap`, `PriorityQueue`, `Arrays.sort` or `Collections.sort`. To confirm, run:

```bash
grep -rn "import java.util" src
```

The command returns nothing.

The project also includes its own JSON writer, random-number generator, input parser and web-form parser. The only library code used is plumbing: the built-in HTTP server, file reading for the web page and URL decoding.

The JavaScript interface only draws and replays results. All algorithms run in the Java back end.

## 6. Operation Counting and Running-Time Measurement

A tracing component (`Tracer`) records operations while each algorithm runs.

| Counter | What it records |
|---|---|
| Comparisons | Comparisons between elements, and base-case tests in recursion |
| Swaps | Exchanges of two elements |
| Writes | Element writes, shifts and placements |
| Arithmetic | Arithmetic operations in recursive algorithms |
| Calls | Recursive calls |
| Loop iterations | Iterations of the loop in iterative factorial and bottom-up Fibonacci |
| Maximum depth | Deepest recursion depth reached |
| Total | Sum of all the counters above |

**Sorting, searching, recursion and data structure pages:** the application first runs the algorithm with step recording on, to build the replay. It then runs it five more times with recording off and reports the average time, measured with `System.nanoTime()` (a high-resolution timer). Very fast searches are repeated 200 times per measurement and averaged.

**Complexity analysis page and `bench.sh`:** each size is timed at least 3 times and the average is reported. Each algorithm is run first to warm up the Java JIT compiler. Searches are repeated 2,000 times per measurement.

Running times vary with the Java Virtual Machine and the computer's workload. Operation counts never change for the same input, so they are the main basis for judging growth.

Requests run on a worker thread with a 512 MB stack, so deep recursion (for example, first-element quick sort on 100,000 sorted numbers) completes without a stack overflow.

## 7. Project Structure

```text
AlgorithmExplorer/
├── README.md
├── run.sh / run.bat            start the application
├── test.sh / test.bat          run the self-test
├── bench.sh / bench.bat        run the Module D experiment
├── src/
│   └── explorer/
│       ├── ds/                 hand-written data structures
│       │   ├── DynamicArray.java
│       │   ├── IntList.java
│       │   ├── ArrayStack.java        fixed-capacity (Module B) or growable stack
│       │   ├── LinearQueue.java       fixed array, shows the false-full problem
│       │   ├── CircularQueue.java     ring buffer
│       │   ├── SinglyLinkedList.java
│       │   ├── DoublyLinkedList.java
│       │   ├── BinarySearchTree.java
│       │   ├── MinHeap.java
│       │   └── HashTable.java
│       ├── core/
│       │   ├── Tracer.java     operation counters and step recorder
│       │   ├── Json.java       JSON writer
│       │   ├── Rng.java        xorshift random numbers
│       │   └── Input.java      input parsing and validation
│       ├── algorithms/
│       │   ├── Sorting.java
│       │   ├── Searching.java
│       │   ├── Recursion.java
│       │   ├── DataStructures.java
│       │   ├── Analyzer.java   complexity experiments and curve fitting
│       │   └── Catalog.java    theoretical complexities
│       ├── server/
│       │   ├── Main.java       HTTP server and worker thread
│       │   ├── Api.java        one handler per route
│       │   └── Params.java     form-data parser
│       ├── SelfTest.java       automated checks
│       └── Benchmark.java      Module D timing experiment (writes results/module_d.csv)
├── results/
│   └── module_d.csv            Module D measurements used in the report
└── web/
    ├── index.html
    ├── style.css
    └── app.js
```

The `out/` folder of compiled classes is created when the application is built. It is not part of the repository.

## 8. Installation and Running

### Step 1: Install the JDK

The only dependency is a Java Development Kit (JDK), version 11 or newer. No other libraries are needed.

**Windows**

- Download the Temurin JDK installer from https://adoptium.net and run it. Tick "Set JAVA_HOME" and "Add to PATH" during installation.
- Or, in PowerShell:

  ```powershell
  winget install EclipseAdoptium.Temurin.17.JDK
  ```

**macOS** (with Homebrew)

```bash
brew install openjdk@17
```

**Ubuntu or Debian Linux**

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

Check the installation by opening a new terminal and running:

```bash
javac -version
```

It should print `javac 11` or a higher version.

### Step 2: Get the code

Clone the repository:

```bash
git clone https://github.com/abangiraprecious/algorithm-explorer-group18.git
cd algorithm-explorer-group18
```

Or download the `.zip` file, extract it and open the extracted folder.

### Step 3: Run the application

**Windows**

Double-click `run.bat`, or run it from Command Prompt:

```bat
run.bat
```

**macOS or Linux**

Make the scripts executable once:

```bash
chmod +x run.sh test.sh bench.sh
```

Then start the application:

```bash
./run.sh
```

The script compiles the code and starts the server. Open the application in a browser at:

**http://localhost:8080**

On most desktops the browser opens automatically.

To use another port, give it as an argument:

```bash
./run.sh 9090
```

On Windows: `run.bat 9090`

Stop the server with `Ctrl+C`.

### Step 4: Run the self-test

- Windows: double-click `test.bat`
- macOS or Linux: `./test.sh`

### Step 5: Run the Module D experiment (optional)

- Windows: double-click `bench.bat`
- macOS or Linux: `./bench.sh` (or `./bench.sh 5` for 5 runs per size)

It takes about a minute and writes `results/module_d.csv`. First-element quick sort on 100,000 sorted numbers is the slowest part (about 5 billion comparisons).

### Manual Java Commands

**macOS or Linux**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out explorer.server.Main 8080
java -cp out explorer.SelfTest
java -cp out explorer.Benchmark 3
```

**Windows (Command Prompt)**

```bat
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -cp out explorer.server.Main 8080
java -cp out explorer.SelfTest
java -cp out explorer.Benchmark 3
```

## 9. Testing

The self-test (`SelfTest.java`) runs 2,491 automated checks. All 2,491 pass.

The checks cover:

- Every sort on 300 random arrays, including empty arrays, single items, duplicates and negative numbers. Each result is checked to be sorted and to hold the same items as the input.
- Exact operation counts predicted by theory. For example, selection sort on 10 items makes exactly 45 comparisons, first-element quick sort on 200 sorted items makes 200·199/2 comparisons while the random pivot makes far fewer, insertion sort on sorted input makes n − 1 comparisons, binary search on 1,024 items makes 11 probes, fib(10) makes 177 calls naively and 19 when memoised, and bottom-up fib(10) takes 9 loop iterations.
- Known results: 0! and 20! (recursive and iterative), fib(90), Hanoi with 1 to 8 disks between any pegs (2ⁿ − 1 moves each), 3¹³, gcd(1071, 462) = 21 and the 4! permutations of a word.
- Fixed-capacity stack overflow and underflow, the linear queue's false full, the circular queue wrapping round and reusing slots, every insert and delete of the singly and doubly linked lists (including display backward), all three binary search tree delete cases, heap ordering, a hash table with 500 keys and resizing, expression evaluation and bracket checking.

Invalid input returns a clear message instead of crashing, for example: `"abc" is not a whole number (item 2).`, `Factorial is not defined for negative numbers (you entered -3).`, `Naive recursion is limited to n ≤ 30 (you entered 31). …` and `Stack overflow: the stack is full (capacity 5), so 9 cannot be pushed`.

## 10. Demonstration Guide

1. **Sorting:** run quick sort (first-element pivot) on the default numbers and step through one partition to see the yellow and green parts grow. Tick *After each pass only* to see the array after each partition. Then click *20 sorted* and run it again: comparisons jump to n(n−1)/2 = 190, the worst case. Switch to the random pivot to see it drop. Press *Compare all* to see all five algorithms in one table.
2. **Recursion:** run naive Fibonacci with n = 5 and watch fib(3) and fib(2) being computed again and again. Switch to memoised Fibonacci: 9 calls instead of 15, and bottom-up DP: 4 loop iterations. Run Tower of Hanoi with 4 disks from B to A and press Start to watch the 15 moves.
3. **Data structures:** set the stack capacity to 3 and press *push* four times to see the overflow. On the linear queue, enqueue 5 values, dequeue twice and enqueue again to see the false full; run the same steps on the circular queue to see rear wrap to slot 0. On the doubly linked list, use *insert before*, *delete after* and *display backward*. Check `{ ( [ ] ) }` with the balanced-brackets page.
4. **Complexity analysis:** select bubble, insertion, merge and both quick sorts, choose *Already sorted*, and run. Bubble and insertion sort fit O(n), merge sort and random-pivot quick sort fit O(n log n), and first-element quick sort fits O(n²).

## 11. Contributions

Precious Abangira Nimmusiima and Isaac Isota led the project. All five members designed the data structures, algorithms, front end and back end together. Each file was then written and committed by one member.

| Member | Role | Main files |
|---|---|---|
| Precious Abangira Nimmusiima | Project lead; recursion, complexity analysis and server integration | `Recursion.java`, `Analyzer.java`, `Tracer.java`, `Api.java` |
| Isaac Isota | Project lead; sorting, searching and part of the web interface | `Sorting.java`, `Searching.java`, `index.html`, `style.css` |
| Teta Nadine Tracy | Interactive browser application | `app.js`, `Catalog.java` |
| Nyakecho Karen Faith | Data structures and supporting utilities | all files in `ds/` (including `LinearQueue.java` and `DoublyLinkedList.java`), `Input.java`, `Json.java`, `Rng.java` |
| Ainebyona Sheba | Data structure applications and testing | `DataStructures.java`, `Main.java`, `Params.java`, `SelfTest.java`, `Benchmark.java` |

## 12. Academic Information

| Item | Details |
|---|---|
| Institution | Uganda Christian University (UCU) |
| Course unit | CSC2107: Design and Analysis of Algorithms |
| Project title | Algorithm Explorer |
| Group | 18 |
| Group size | Five students |
| Reference | Cormen, T. H., Leiserson, C. E., Rivest, R. L. and Stein, C. (2009). *Introduction to Algorithms* (3rd ed.). MIT Press. |