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
- The running time, taken as the median of five timed runs with step recording switched off.

The project covers recursion, data structures, sorting, searching and time-complexity analysis. It runs entirely on the local machine. It needs no internet connection, external API, framework or third-party library.

## 2. Project Objectives

- Implement algorithms and data structures from scratch.
- Let users supply their own inputs and watch each algorithm run.
- Present results and execution steps in an interactive interface.
- Count basic operations and measure running time.
- Compare theoretical complexity with measured growth.
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

The animation shows each comparison, swap and write, together with the pivot, the active sub-array and the elements already in their final positions. Up to 120 numbers are animated. Up to 100,000 numbers are counted and timed.

### 3.2 Searching Algorithms

- Linear search
- Iterative binary search
- Recursive binary search

The interface shows the search range shrinking. The recursive version also shows its call stack.

### 3.3 Recursion

- Factorial
- Fibonacci using naive recursion
- Fibonacci using memoisation
- Iterative Fibonacci, for comparison
- Tower of Hanoi
- Fast power using exponentiation by squaring
- Euclid's greatest common divisor algorithm
- Permutations using backtracking
- Recursive palindrome check

Each run draws the recursion tree and the call stack as execution proceeds.

### 3.4 Data Structures and Applications

| Data structure | Implementation and supported operations |
|---|---|
| Dynamic array | Resizable array that doubles when full |
| Integer list | Growable integer array |
| Stack | Array-based stack with push, pop, peek, size and isEmpty |
| Queue | Circular-array queue with enqueue, dequeue, peek and size |
| Singly linked list | Head and tail pointers; insertion, removal, search, retrieval and reversal |
| Binary search tree | Insertion, search, deletion (all three cases), traversals, height, minimum and maximum |
| Min-heap | Array-based binary heap with insertion, extract-min, peek and O(n) heap construction |
| Hash table | Separate chaining, own hash function, doubles when the load factor passes 0.75 |

Two stack-based applications are included:

- **Balanced brackets:** checks matching parentheses, square brackets and braces.
- **Expression evaluator:** converts an expression to postfix with the shunting-yard algorithm, then evaluates the postfix form.

### 3.5 Time-Complexity Analysis

Users choose algorithms, input sizes and an input order: random, sorted, reversed, nearly sorted or few distinct values.

The analysis page shows:

- Operation counts against input size.
- Running time against input size.
- An optional log-log view.
- A growth exponent, calculated as the slope of log(operations) against log(input size).
- A best-fit growth model chosen from log n, n, n log n, n², n³ and 2ⁿ.
- A comparison between the measured growth and the theoretical complexity for that input.
- A table of every measurement.

A recursion mode does the same for naive and memoised Fibonacci, Tower of Hanoi, factorial and fast power.

## 4. Algorithms and Theoretical Complexity

| Algorithm | Best case | Average case | Worst case | Extra space |
|---|---|---|---|---|
| Bubble sort | O(n) | O(n²) | O(n²) | O(1) |
| Selection sort | O(n²) | O(n²) | O(n²) | O(1) |
| Insertion sort | O(n) | O(n²) | O(n²) | O(1) |
| Merge sort | O(n log n) | O(n log n) | O(n log n) | O(n) |
| Quick sort | O(n log n) | O(n log n) | O(n²) | O(log n) stack on average, O(n) worst |
| Heap sort | O(n log n) | O(n log n) | O(n log n) | O(1) |
| Counting sort | O(n + k) | O(n + k) | O(n + k) | O(n + k) |
| Linear search | O(1) | O(n) | O(n) | O(1) |
| Binary search | O(1) | O(log n) | O(log n) | O(1) iterative; O(log n) recursive |

### Recurrence Relations

| Recursive algorithm | Recurrence or approach | Time complexity |
|---|---|---|
| Factorial | T(n) = T(n−1) + O(1) | O(n) |
| Naive Fibonacci | T(n) = T(n−1) + T(n−2) + O(1) | O(φⁿ), φ ≈ 1.618 |
| Memoised Fibonacci | Each subproblem is solved once | O(n) |
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
| Maximum depth | Deepest recursion depth reached |
| Total | Sum of all the counters above |

**Sorting, searching and recursion pages:** the application first runs the algorithm with step recording on, to build the replay. It then runs it five more times with recording off and reports the median time, measured with `System.nanoTime()`. Very fast searches are repeated 200 times per measurement and averaged.

**Complexity analysis page:** the user chooses 1, 3 or 5 timed runs per size, and the median is reported. Each algorithm is run three times first to warm up the Java JIT compiler. Searches are repeated 2,000 times per measurement.

Running times vary with the Java Virtual Machine and the computer's workload. Operation counts never change for the same input, so they are the main basis for judging growth.

Requests run on a worker thread with a 512 MB stack, so deep recursion (for example, quick sort on 30,000 sorted numbers) completes without a stack overflow.

## 7. Project Structure

```text
AlgorithmExplorer/
├── README.md
├── run.sh / run.bat            start the application
├── test.sh / test.bat          run the self-test
├── src/
│   └── explorer/
│       ├── ds/                 hand-written data structures
│       │   ├── DynamicArray.java
│       │   ├── IntList.java
│       │   ├── ArrayStack.java
│       │   ├── CircularQueue.java
│       │   ├── SinglyLinkedList.java
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
│       └── SelfTest.java       automated checks
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
chmod +x run.sh test.sh
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

### Manual Java Commands

**macOS or Linux**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out explorer.server.Main 8080
java -cp out explorer.SelfTest
```

**Windows (Command Prompt)**

```bat
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -cp out explorer.server.Main 8080
java -cp out explorer.SelfTest
```

## 9. Testing

The self-test (`SelfTest.java`) runs 2,159 automated checks. All 2,159 pass.

The checks cover:

- Every sort on 300 random arrays, including empty arrays, single items, duplicates and negative numbers. Each result is checked to be sorted and to hold the same items as the input.
- Exact operation counts predicted by theory. For example, selection sort on 10 items makes exactly 45 comparisons, binary search on 1,024 items makes 11 probes, and fib(10) makes 177 calls.
- Known results: 20!, fib(90), 2¹⁰ − 1 Hanoi moves, 3¹³, gcd(1071, 462) = 21 and the 4! permutations of a word.
- Stack and queue order across resizes and ring wrap-around, all three binary search tree delete cases, heap ordering, a hash table with 500 keys and resizing, expression evaluation and bracket checking.

Invalid input returns a clear message instead of crashing, for example: `"abc" is not a whole number (item 2).`, `n must be between 0 and 32 (you entered 40).` and `stack underflow: the stack is empty`.

## 10. Demonstration Guide

1. **Sorting:** run quick sort on the default numbers and step through one partition. Then click *20 sorted* and run it again. Comparisons jump to n(n−1)/2 = 190, the worst case.
2. **Recursion:** run naive Fibonacci with n = 5 and watch fib(3) and fib(2) being computed again and again. Switch to memoised Fibonacci: 9 calls instead of 15. With n = 25 it is 49 calls instead of 242,785.
3. **Data structures:** run the stack example to see the array double and the underflow message. Run `insert 1 2 3 4 5 6; height` on the binary search tree to see it become a chain of height 5. Run the hash table example to see it resize to 16 buckets.
4. **Complexity analysis:** select bubble, insertion, merge and quick sort, choose *Already sorted*, and run. Bubble and insertion sort fit O(n), merge sort fits O(n log n), and quick sort fits O(n²). Switch to *Random* to see quick sort return to O(n log n).

## 11. Contributions

Precious Abangira Nimmusiima and Isaac Isota led the project. All five members designed the data structures, algorithms, front end and back end together. Each file was then written and committed by one member.

| Member | Role | Main files |
|---|---|---|
| Precious Abangira Nimmusiima | Project lead; recursion, complexity analysis and server integration | `Recursion.java`, `Analyzer.java`, `Tracer.java`, `Api.java` |
| Isaac Isota | Project lead; sorting, searching and part of the web interface | `Sorting.java`, `Searching.java`, `index.html`, `style.css` |
| Teta Nadine Tracy | Interactive browser application | `app.js`, `Catalog.java` |
| Nyakecho Karen Faith | Data structures and supporting utilities | all files in `ds/`, `Input.java`, `Json.java`, `Rng.java` |
| Ainebyona Sheba | Data structure applications and testing | `DataStructures.java`, `Main.java`, `Params.java`, `SelfTest.java` |

## 12. Academic Information

| Item | Details |
|---|---|
| Institution | Uganda Christian University (UCU) |
| Course unit | CSC2107: Design and Analysis of Algorithms |
| Project title | Algorithm Explorer |
| Group | 18 |
| Group size | Five students |
| Reference | Cormen, T. H., Leiserson, C. E., Rivest, R. L. and Stein, C. (2009). *Introduction to Algorithms* (3rd ed.). MIT Press. |