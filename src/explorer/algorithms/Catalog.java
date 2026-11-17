package explorer.algorithms;

import explorer.core.Json;

/** Theoretical facts about every algorithm, shown next to the measured numbers. */
public final class Catalog {
    private Catalog() {}

    private static String info(String name, String best, String avg, String worst, String space,
                               String stable, String recurrence, String idea) {
        return Json.obj().put("name", name).put("best", best).put("avg", avg).put("worst", worst)
                .put("space", space).put("stable", stable).put("recurrence", recurrence).put("idea", idea).end();
    }

    public static String name(String key) {
        switch (key) {
            case "bubble": return "Bubble sort";
            case "selection": return "Selection sort";
            case "insertion": return "Insertion sort";
            case "merge": return "Merge sort";
            case "quick": return "Quick sort (first-element pivot)";
            case "quick-random": return "Quick sort (random pivot)";
            case "heap": return "Heap sort";
            case "counting": return "Counting sort";
            case "linear": return "Linear search";
            case "binary": return "Binary search (iterative)";
            case "binary-rec": return "Binary search (recursive)";
            case "factorial": return "Factorial (recursive)";
            case "factorial-iter": return "Factorial (iterative)";
            case "fib-naive": return "Fibonacci (naive recursion)";
            case "fib-memo": return "Fibonacci (memoised recursion)";
            case "fib-dp": return "Fibonacci (bottom-up DP)";
            case "hanoi": return "Tower of Hanoi";
            case "power": return "Fast power";
            case "gcd": return "Euclid's GCD";
            case "permutations": return "Permutations (backtracking)";
            case "palindrome": return "Palindrome check";
            default: return key;
        }
    }

    public static String get(String key) {
        switch (key) {
            case "bubble": return info("Bubble sort", "O(n)", "O(n²)", "O(n²)", "O(1)", "yes", "",
                    "Repeatedly swap neighbours that are out of order. After pass i the i largest values are in place. Stops early if a pass makes no swaps.");
            case "selection": return info("Selection sort", "O(n²)", "O(n²)", "O(n²)", "O(1)", "no", "",
                    "Find the minimum of the unsorted part and swap it to the front. Always n(n−1)/2 comparisons, but at most n−1 swaps.");
            case "insertion": return info("Insertion sort", "O(n)", "O(n²)", "O(n²)", "O(1)", "yes", "",
                    "Grow a sorted prefix by inserting the next item into place, shifting larger items right. Very fast on nearly sorted data.");
            case "merge": return info("Merge sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)", "yes", "T(n) = 2T(n/2) + O(n)",
                    "Divide the array in half, sort each half recursively, then merge the two sorted halves. log n levels with O(n) merge work per level.");
            case "quick": return info("Quick sort (first-element pivot)", "O(n log n)", "O(n log n)", "O(n²)", "O(log n) stack, O(n) worst", "no",
                    "T(n) = T(k) + T(n−k−1) + O(n)",
                    "Take the first element as the pivot. Yellow/green partition: values smaller than the pivot form the yellow part, values greater or equal form the green part; then the pivot is swapped between them and both parts are sorted recursively. Sorted or reversed input gives the worst case because every split is 0 : n−1.");
            case "quick-random": return info("Quick sort (random pivot)", "O(n log n)", "O(n log n) expected", "O(n²) (very unlikely)", "O(log n) stack expected", "no",
                    "T(n) = T(k) + T(n−k−1) + O(n), k random",
                    "Swap a randomly chosen element to the front, then partition exactly as before. No fixed input order is bad any more, so the expected time is O(n log n) even on sorted input.");
            case "heap": return info("Heap sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(1)", "no", "",
                    "Build a max-heap in O(n), then n times swap the root (maximum) to the end and sift the new root down in O(log n).");
            case "counting": return info("Counting sort", "O(n + k)", "O(n + k)", "O(n + k)", "O(n + k)", "yes", "",
                    "Not comparison based: count how often each value occurs, turn counts into positions with prefix sums, then place items. k = max − min + 1.");
            case "linear": return info("Linear search", "O(1)", "O(n)", "O(n)", "O(1)", "–", "",
                    "Check every item from left to right. Works on unsorted data.");
            case "binary": return info("Binary search (iterative)", "O(1)", "O(log n)", "O(log n)", "O(1)", "–", "",
                    "On sorted data, compare with the middle item and discard half of the range each time.");
            case "binary-rec": return info("Binary search (recursive)", "O(1)", "O(log n)", "O(log n)", "O(log n) stack", "–",
                    "T(n) = T(n/2) + O(1)",
                    "The same halving idea written recursively: each call searches one half. The recursion depth is at most ⌊log₂ n⌋ + 1.");
            case "factorial": return info("Factorial (recursive)", "O(n)", "O(n)", "O(n)", "O(n) stack", "–", "T(n) = T(n−1) + O(1)",
                    "n! = n × (n−1)!, with base case 0! = 1! = 1. One call per value of n, so the call stack grows to depth n. Negative n is rejected; n ≤ 20 because 21! does not fit in 64 bits.");
            case "factorial-iter": return info("Factorial (iterative)", "O(n)", "O(n)", "O(n)", "O(1)", "–", "",
                    "A loop multiplies result by 2, 3, …, n: n − 1 iterations and no call stack. Same answer as the recursive version with O(1) extra space.");
            case "fib-naive": return info("Fibonacci (naive)", "O(φⁿ)", "O(φⁿ) ≈ O(1.618ⁿ)", "O(2ⁿ) bound", "O(n) stack", "–",
                    "T(n) = T(n−1) + T(n−2) + O(1)",
                    "fib(n) = fib(n−1) + fib(n−2). The same sub-problems are solved again and again, so the call tree has 2·fib(n+1) − 1 calls and grows exponentially. That is why this version is limited to n ≤ 30 (2,692,537 calls).");
            case "fib-memo": return info("Fibonacci (memoised)", "O(n)", "O(n)", "O(n)", "O(n)", "–", "each fib(k) computed once",
                    "Top-down dynamic programming: store each fib(k) the first time it is computed and reuse it. About 2n calls instead of ~φⁿ.");
            case "fib-dp": return info("Fibonacci (bottom-up DP)", "O(n)", "O(n)", "O(n)", "O(n) table", "–", "table[i] = table[i−1] + table[i−2]",
                    "Bottom-up dynamic programming: fill a table from table[0] = 0 and table[1] = 1 up to table[n]. n − 1 loop iterations, no recursion and no call stack.");
            case "hanoi": return info("Tower of Hanoi", "O(2ⁿ)", "O(2ⁿ)", "O(2ⁿ)", "O(n) stack", "–", "T(n) = 2T(n−1) + 1 = 2ⁿ − 1",
                    "Move n−1 disks from the source to the auxiliary peg, move the largest disk to the destination, then move the n−1 disks from the auxiliary peg on top of it. Needs exactly 2ⁿ − 1 moves.");
            case "power": return info("Fast power", "O(log n)", "O(log n)", "O(log n)", "O(log n) stack", "–", "T(n) = T(n/2) + O(1)",
                    "xⁿ = (x^(n/2))², times x if n is odd. Halving the exponent gives log n calls instead of n multiplications.");
            case "gcd": return info("Euclid's GCD", "O(1)", "O(log min(a,b))", "O(log min(a,b))", "O(log) stack", "–", "gcd(a, b) = gcd(b, a mod b)",
                    "The remainder shrinks at least by half every two steps, so the number of calls is logarithmic.");
            case "permutations": return info("Permutations", "O(n·n!)", "O(n·n!)", "O(n·n!)", "O(n) stack", "–", "T(n) = n·T(n−1) + O(n)",
                    "Backtracking: fix each character in the first position, recursively permute the rest, then undo the swap.");
            case "palindrome": return info("Palindrome check", "O(1)", "O(n)", "O(n)", "O(n) stack", "–", "T(n) = T(n−2) + O(1)",
                    "Compare the outer two characters; if equal, recurse on the inside.");
            case "ds-stack": return info("Stack (array, fixed capacity)", "", "", "", "O(capacity)", "", "",
                    "Last in, first out, stored in an array whose capacity you choose. push, pop, peek, size, is_empty and is_full are all O(1); display is O(n). Pushing onto a full stack is an overflow, popping an empty one an underflow.");
            case "ds-linear-queue": return info("Queue (linear array)", "", "", "", "O(capacity)", "", "",
                    "First in, first out on a fixed array. enqueue writes at rear and moves rear right; dequeue moves front right. Freed slots before front are never reused, so once rear reaches the end the queue says it is full even if most slots are empty: the false full problem. Every operation is O(1).");
            case "ds-queue": return info("Queue (circular array)", "", "", "", "O(capacity)", "", "",
                    "First in, first out on a fixed array used as a ring: rear = (rear + 1) mod capacity, so after the last slot it wraps to slot 0 and reuses slots freed by dequeue. This removes the false full problem of the linear queue. Every operation is O(1).");
            case "ds-list": return info("Singly linked list", "", "", "", "O(n)", "", "",
                    "Each node holds a value and a next pointer. Insert/delete at the beginning and insert at the end are O(1) (head and tail pointers); delete at the end, before/after a given element, search and display walk the list in O(n).");
            case "ds-dlist": return info("Doubly linked list", "", "", "", "O(n)", "", "",
                    "Each node holds a value and prev and next pointers. Insert/delete at either end is O(1) because tail.prev is known; operations at a given element first search for it in O(n). The prev pointers also allow display backward from the tail.");
            case "ds-bst": return info("Binary search tree", "", "", "", "O(n)", "", "",
                    "Left subtree < node < right subtree. Operations cost O(h): about log n when balanced, but inserting sorted data makes a chain with h = n.");
            case "ds-heap": return info("Binary min-heap", "", "", "", "O(n)", "", "",
                    "A complete binary tree stored in an array where every parent ≤ its children. Insert and extract-min are O(log n); peek is O(1).");
            case "ds-hash": return info("Hash table (chaining)", "", "", "", "O(n)", "", "",
                    "A hash function picks a bucket for each key; collisions share a linked chain. O(1) on average while the load factor stays under 0.75.");
            case "ds-brackets": return info("Balanced brackets (stack)", "O(n)", "O(n)", "O(n)", "O(n)", "", "",
                    "Push every opening bracket; every closing bracket must match the top of the stack, which is popped. Balanced if the stack ends empty. The stack is shown after each symbol.");
            case "ds-expression": return info("Expression evaluator (stacks)", "O(n)", "O(n)", "O(n)", "O(n)", "", "",
                    "Dijkstra's shunting-yard algorithm converts infix to postfix using an operator stack; a value stack then evaluates the postfix form.");
            default: return info(key, "", "", "", "", "", "", "");
        }
    }
}
