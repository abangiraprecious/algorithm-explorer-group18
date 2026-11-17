package explorer;

import explorer.algorithms.DataStructures;
import explorer.algorithms.Recursion;
import explorer.algorithms.Searching;
import explorer.algorithms.Sorting;
import explorer.core.Input;
import explorer.core.Rng;
import explorer.core.Tracer;
import explorer.ds.ArrayStack;
import explorer.ds.BinarySearchTree;
import explorer.ds.CircularQueue;
import explorer.ds.DoublyLinkedList;
import explorer.ds.DynamicArray;
import explorer.ds.HashTable;
import explorer.ds.IntList;
import explorer.ds.LinearQueue;
import explorer.ds.MinHeap;
import explorer.ds.SinglyLinkedList;

/**
 * Self-test: checks every algorithm and data structure against known answers.
 *   java -cp out explorer.SelfTest
 */
public final class SelfTest {
    private static int passed, failed;

    private static void check(boolean ok, String what) {
        if (ok) passed++;
        else { failed++; System.out.println("FAIL: " + what); }
    }

    public static void main(String[] args) {
        Rng rng = new Rng(12345);

        // ---- sorting: every algorithm on 300 random arrays, incl. empty, single, duplicates, negatives
        for (int trial = 0; trial < 300; trial++) {
            int n = trial < 5 ? trial : rng.nextInt(200);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rng.between(-50, 50);
            for (int k = 0; k < Sorting.KEYS.length; k++) {
                int[] c = Input.copy(a);
                Sorting.run(Sorting.KEYS[k], c, new Tracer(trial % 50 == 0, 500));
                check(Sorting.isSorted(c) && sameItems(a, c), Sorting.KEYS[k] + " on " + java(a));
            }
        }
        // operation-count sanity: selection sort always makes n(n-1)/2 comparisons
        Tracer ts = Tracer.silent();
        Sorting.selection(new int[]{5, 4, 3, 2, 1, 0, 9, 8, 7, 6}, ts);
        check(ts.comparisons == 45, "selection comparisons = 45, got " + ts.comparisons);
        // bubble sort on sorted input stops after one pass: n-1 comparisons
        Tracer tb = Tracer.silent();
        Sorting.bubble(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, tb);
        check(tb.comparisons == 7 && tb.swaps == 0, "bubble best case");

        // first-element quick sort on sorted input: every split is 0 : n-1, so n(n-1)/2 comparisons
        Tracer tq = Tracer.silent();
        int[] asc = new int[200];
        for (int i = 0; i < asc.length; i++) asc[i] = i;
        Sorting.quick(asc, tq);
        check(tq.comparisons == 200 * 199 / 2, "first-pivot quick sort worst case = n(n-1)/2, got " + tq.comparisons);
        // the random pivot avoids that worst case on the same input
        Tracer tr = Tracer.silent();
        for (int i = 0; i < asc.length; i++) asc[i] = i;
        Sorting.quickRandom(asc, tr);
        check(Sorting.isSorted(asc) && tr.comparisons < 200 * 199 / 4, "random-pivot quick sort on sorted input, got " + tr.comparisons);
        // insertion sort on sorted input: n-1 comparisons and no shifts
        Tracer ti = Tracer.silent();
        for (int i = 0; i < asc.length; i++) asc[i] = i;
        Sorting.insertion(asc, ti);
        check(ti.comparisons == 199, "insertion best case n-1 comparisons");

        // ---- searching
        int[] sorted = {-5, -1, 0, 3, 3, 7, 10, 22, 40};
        for (int i = 0; i < sorted.length; i++) {
            for (int k = 0; k < Searching.KEYS.length; k++) {
                int idx = Searching.run(Searching.KEYS[k], sorted, sorted[i], new Tracer(true, 100));
                check(idx >= 0 && sorted[idx] == sorted[i], Searching.KEYS[k] + " find " + sorted[i]);
            }
        }
        for (int k = 0; k < Searching.KEYS.length; k++)
            check(Searching.run(Searching.KEYS[k], sorted, 5, Tracer.silent()) == -1, Searching.KEYS[k] + " absent");
        Tracer tbin = Tracer.silent();
        int[] big = new int[1024];
        for (int i = 0; i < big.length; i++) big[i] = i;
        Searching.binary(big, 5000, tbin);
        check(tbin.comparisons == 11, "binary search on 1024 items takes 11 probes, got " + tbin.comparisons);

        // ---- recursion
        check(Recursion.factorial(10, new Tracer(true, 100)) == 3628800L, "10!");
        check(Recursion.factorial(20, Tracer.silent()) == 2432902008176640000L, "20!");
        check(Recursion.fibNaive(20, Tracer.silent()) == 6765, "fib 20 naive");
        check(Recursion.fibMemo(90, Tracer.silent()) == 2880067194370816120L, "fib 90 memo");
        check(Recursion.fibBottomUp(50, Tracer.silent()) == 12586269025L, "fib 50 bottom-up");
        Tracer tf = Tracer.silent();
        Recursion.fibNaive(10, tf);
        check(tf.calls == 177, "fib(10) makes 177 calls, got " + tf.calls);
        Tracer tfm = Tracer.silent();
        Recursion.fibMemo(10, tfm);
        check(tfm.calls == 19, "memoised fib(10) makes 2n-1 = 19 calls, got " + tfm.calls);
        Tracer tfd = Tracer.silent();
        check(Recursion.fibBottomUp(10, tfd) == 55 && tfd.iterations == 9, "bottom-up fib(10) = 55 in 9 iterations");
        check(Recursion.fibBottomUp(0, Tracer.silent()) == 0 && Recursion.fibBottomUp(1, Tracer.silent()) == 1, "fib(0), fib(1) bottom-up");
        check(Recursion.factorialIterative(0, Tracer.silent()) == 1 && Recursion.factorialIterative(20, Tracer.silent()) == 2432902008176640000L,
                "iterative factorial 0! and 20!");
        check(Recursion.factorial(0, Tracer.silent()) == 1, "recursive 0! = 1");
        check(Recursion.hanoi(10, Tracer.silent()) == 1023, "hanoi 10 = 1023 moves");
        for (int disks = 1; disks <= 8; disks++) {
            DynamicArray<String> mv = new DynamicArray<String>();
            Tracer th = new Tracer(true, 4000);
            long m = Recursion.hanoi(disks, 1, 0, 2, mv, th);       // from B to A using C
            check(m == (1L << disks) - 1 && mv.size() == m, "hanoi " + disks + " disks B to A");
        }
        check(Recursion.power(3, 13, Tracer.silent()) == 1594323L, "3^13");
        check(Recursion.power(-2, 63, Tracer.silent()) == Long.MIN_VALUE, "(-2)^63");
        check(Recursion.gcd(1071, 462, Tracer.silent()) == 21, "gcd(1071,462)");
        check(Recursion.permutations("abcd", Tracer.silent()).size() == 24, "4! permutations");
        check(Recursion.palindrome("racecar", Tracer.silent()), "racecar");
        check(!Recursion.palindrome("algorithm", Tracer.silent()), "algorithm not palindrome");

        // ---- data structures
        ArrayStack<Integer> st = new ArrayStack<Integer>(2);
        for (int i = 0; i < 100; i++) st.push(i);
        boolean lifo = true;
        for (int i = 99; i >= 0; i--) lifo &= st.pop() == i;
        check(lifo && st.isEmpty(), "stack LIFO");

        CircularQueue<Integer> q = new CircularQueue<Integer>(3);
        boolean fifo = true;
        int next = 0, expect = 0;
        for (int round = 0; round < 50; round++) {
            q.enqueue(next++); q.enqueue(next++);
            fifo &= q.dequeue() == expect++;
        }
        while (!q.isEmpty()) fifo &= q.dequeue() == expect++;
        check(fifo, "queue FIFO across wrap-around and resizes");

        ArrayStack<Integer> fixed = new ArrayStack<Integer>(3, true);
        fixed.push(1); fixed.push(2); fixed.push(3);
        boolean overflow = false;
        try { fixed.push(4); } catch (IllegalStateException e) { overflow = true; }
        check(overflow && fixed.isFull() && fixed.size() == 3, "fixed stack overflow");
        fixed.pop(); fixed.pop(); fixed.pop();
        boolean underflow = false;
        try { fixed.pop(); } catch (IllegalStateException e) { underflow = true; }
        check(underflow && fixed.isEmpty(), "fixed stack underflow");

        LinearQueue<Integer> lq = new LinearQueue<Integer>(3);
        lq.enqueue(1); lq.enqueue(2); lq.enqueue(3);
        check(lq.dequeue() == 1 && lq.dequeue() == 2, "linear queue FIFO");
        boolean falseFull = false;
        try { lq.enqueue(4); } catch (IllegalStateException e) { falseFull = true; }
        check(falseFull && lq.isFalseFull() && lq.size() == 1, "linear queue false full");

        CircularQueue<Integer> cq = new CircularQueue<Integer>(3, true);
        cq.enqueue(1); cq.enqueue(2); cq.enqueue(3);
        cq.dequeue(); cq.dequeue();
        cq.enqueue(4); cq.enqueue(5);            // rear wraps round to slots 0 and 1
        check(cq.isFull() && cq.rear() == 2 && cq.dequeue() == 3 && cq.dequeue() == 4 && cq.dequeue() == 5, "circular queue wraps and reuses slots");
        boolean qFull = false;
        CircularQueue<Integer> cq2 = new CircularQueue<Integer>(1, true);
        cq2.enqueue(9);
        try { cq2.enqueue(10); } catch (IllegalStateException e) { qFull = true; }
        check(qFull, "fixed circular queue full");

        SinglyLinkedList s1 = new SinglyLinkedList();
        s1.addLast(10); s1.addLast(20); s1.addLast(30);
        s1.insertBefore(20, 15); s1.insertAfter(30, 35); s1.insertBefore(10, 5);   // 5 10 15 20 30 35
        check(s1.display().equals("head → 5 → 10 → 15 → 20 → 30 → 35 → null"), "singly insert before/after: " + s1.display());
        check(s1.removeBefore(15) == 10 && s1.removeAfter(20) == 30 && s1.removeLast() == 35 && s1.removeFirst() == 5, "singly deletes");
        check(s1.display().equals("head → 15 → 20 → null") && s1.indexOf(20) == 1 && s1.size() == 2, "singly after deletes: " + s1.display());

        DoublyLinkedList d = new DoublyLinkedList();
        d.addLast(10); d.addLast(20); d.addFirst(5);
        d.insertBefore(20, 15); d.insertAfter(20, 25);                            // 5 10 15 20 25
        check(d.displayForward().equals("head → 5 → 10 → 15 → 20 → 25 → null"), "doubly forward: " + d.displayForward());
        check(d.displayBackward().equals("tail → 25 → 20 → 15 → 10 → 5 → null"), "doubly backward: " + d.displayBackward());
        check(d.removeBefore(15) == 10 && d.removeAfter(15) == 20 && d.removeLast() == 25 && d.removeFirst() == 5 && d.size() == 1, "doubly deletes");
        check(d.removeValue(15) == 0 && d.size() == 0 && d.head() == null && d.tail() == null, "doubly delete given element");

        DataStructures ds1 = new DataStructures(true, 3);
        ds1.run("stack", "push 1 2 3; push 4; pop; isFull; display");
        check(ds1.summary.startsWith("Stack holds 2 of 3"), "stack script with overflow, got " + ds1.summary);
        DataStructures ds2 = new DataStructures(true, 3);
        ds2.run("linear-queue", "enqueue 1 2 3; dequeue; dequeue; enqueue 4");
        check(ds2.summary.indexOf("false full") >= 0, "linear queue script shows false full, got " + ds2.summary);
        DataStructures ds3 = new DataStructures(true);
        ds3.run("dlist", "insertLast 1 2 3; insertBefore 2 9; deleteAfter 9; displayBackward");
        check(ds3.summary.equals("Doubly linked list has 3 node(s)"), "dlist script, got " + ds3.summary);

        SinglyLinkedList ll = new SinglyLinkedList();
        ll.addLast(2); ll.addFirst(1); ll.addLast(4); ll.insertAt(2, 3);
        check(ll.get(0) == 1 && ll.get(1) == 2 && ll.get(2) == 3 && ll.get(3) == 4, "list order");
        ll.reverse();
        check(ll.get(0) == 4 && ll.get(3) == 1, "list reverse");
        check(ll.removeValue(3) == 1 && ll.size() == 3 && ll.indexOf(3) == -1, "list remove");

        BinarySearchTree bst = new BinarySearchTree();
        int[] keys = {50, 30, 70, 20, 40, 60, 80, 35, 45, 65};
        for (int i = 0; i < keys.length; i++) bst.insert(keys[i]);
        IntList in = new IntList();
        bst.inorder(bst.root(), in);
        check(isSortedList(in) && in.size() == 10, "bst inorder sorted");
        bst.delete(30); bst.delete(50); bst.delete(20);
        IntList in2 = new IntList();
        bst.inorder(bst.root(), in2);
        check(isSortedList(in2) && in2.size() == 7 && !bst.search(30) && bst.search(65), "bst delete");

        MinHeap h = new MinHeap();
        for (int i = 0; i < 200; i++) h.insert(rng.between(-1000, 1000));
        int prev = Integer.MIN_VALUE;
        boolean heapOk = true;
        while (h.size() > 0) { int v = h.extractMin(); heapOk &= v >= prev; prev = v; }
        check(heapOk, "heap extracts in order");

        HashTable<String> ht = new HashTable<String>(2);
        for (int i = 0; i < 500; i++) ht.put("key" + i, "v" + i);
        boolean hashOk = ht.size() == 500;
        for (int i = 0; i < 500; i++) hashOk &= ("v" + i).equals(ht.get("key" + i));
        for (int i = 0; i < 500; i += 2) ht.remove("key" + i);
        hashOk &= ht.size() == 250 && ht.get("key2") == null && "v3".equals(ht.get("key3"));
        check(hashOk, "hash table put/get/remove with resizing");

        DataStructures d1 = new DataStructures(false);
        d1.run("expression", "3 + 4 * (2 - 1) ^ 2");
        check(d1.summary.startsWith("Value: 7,"), "expression 3+4*(2-1)^2 = 7, got " + d1.summary);
        DataStructures d2 = new DataStructures(false);
        d2.run("expression", "-(2+3)*2 - 10/4");
        check(d2.summary.startsWith("Value: -12.5,"), "expression with unary minus, got " + d2.summary);
        DataStructures d3 = new DataStructures(false);
        d3.run("brackets", "{[a+b]*(c-d)}");
        check(d3.summary.equals("Balanced"), "balanced brackets");
        DataStructures d4 = new DataStructures(false);
        d4.run("brackets", "{[a+b)*(c-d)}");
        check(d4.summary.startsWith("Not balanced"), "unbalanced brackets");

        System.out.println(passed + " checks passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    private static boolean sameItems(int[] a, int[] sortedB) {
        // count-compare without library help
        int[] c = Input.copy(a);
        Sorting.insertion(c, Tracer.silent());
        if (c.length != sortedB.length) return false;
        for (int i = 0; i < c.length; i++) if (c[i] != sortedB[i]) return false;
        return true;
    }

    private static boolean isSortedList(IntList l) {
        for (int i = 1; i < l.size(); i++) if (l.get(i - 1) >= l.get(i)) return false;
        return true;
    }

    private static String java(int[] a) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < a.length && i < 20; i++) { if (i > 0) s.append(','); s.append(a[i]); }
        return s.append(a.length > 20 ? ",...]" : "]").toString();
    }
}
