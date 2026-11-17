package explorer.algorithms;

import explorer.core.Input;
import explorer.core.Json;
import explorer.ds.ArrayStack;
import explorer.ds.BinarySearchTree;
import explorer.ds.CircularQueue;
import explorer.ds.DynamicArray;
import explorer.ds.HashTable;
import explorer.ds.IntList;
import explorer.ds.MinHeap;
import explorer.ds.SinglyLinkedList;

/**
 * Runs a user's script of commands against one of our hand-written data structures,
 * e.g. for a stack:  "push 5 8 2; pop; peek".
 * After every command it records a snapshot of the structure plus the cost of that command.
 */
public final class DataStructures {

    public static final String[] KINDS = {"stack", "queue", "list", "bst", "heap", "hash", "brackets", "expression"};

    private final boolean rec;
    private final DynamicArray<String> steps = new DynamicArray<String>();
    public long totalOps;
    public String summary = "";

    public DataStructures(boolean record) { this.rec = record; }

    public String stepsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(steps.get(i));
        }
        return sb.append(']').toString();
    }

    public int stepCount() { return steps.size(); }

    private void step(String type, String msg, long cost, String big, String snap) {
        totalOps += cost;
        if (!rec) return;
        if (steps.size() >= 2000) return;
        steps.add(Json.obj().put("t", type).put("m", msg).put("cost", cost).put("big", big).raw("snap", snap).end());
    }

    // ------------------------------------------------------------------
    // Script parsing: commands are separated by new lines or ';'
    // ------------------------------------------------------------------
    private static DynamicArray<String> commands(String script) {
        DynamicArray<String> out = new DynamicArray<String>();
        if (script == null) return out;
        int start = 0;
        for (int i = 0; i <= script.length(); i++) {
            if (i == script.length() || script.charAt(i) == '\n' || script.charAt(i) == ';') {
                String c = script.substring(start, i).trim();
                if (c.length() > 0) out.add(c);
                start = i + 1;
            }
        }
        if (out.size() > 300) throw new IllegalArgumentException("Scripts are limited to 300 commands.");
        return out;
    }

    private static String lower(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            b.append(c >= 'A' && c <= 'Z' ? (char) (c + 32) : c);
        }
        return b.toString();
    }

    private static void need(DynamicArray<String> tok, int count, String usage) {
        if (tok.size() < count + 1) throw new IllegalArgumentException("Usage: " + usage);
    }

    private static String value(Object o) {
        if (o == null) return "null";
        if (o instanceof Integer || o instanceof Long) return o.toString();
        if (o instanceof Double) return fmt((Double) o);
        return Json.quote(o.toString());
    }

    static String fmt(double d) {
        if (d == (long) d && Math.abs(d) < 1e15) return String.valueOf((long) d);
        String s = String.valueOf(Math.round(d * 1e6) / 1e6);
        return s;
    }

    public void run(String kind, String script) {
        switch (kind) {
            case "stack": stack(script); break;
            case "queue": queue(script); break;
            case "list": list(script); break;
            case "bst": bst(script); break;
            case "heap": heap(script); break;
            case "hash": hash(script); break;
            case "brackets": brackets(script); break;
            case "expression": expression(script); break;
            default: throw new IllegalArgumentException("Unknown data structure: " + kind);
        }
    }

    // ------------------------------------------------------------------ Stack
    private <T> String stackSnap(ArrayStack<T> s, String extra) {
        StringBuilder items = new StringBuilder("[");
        for (int i = 0; i < s.size(); i++) {
            if (i > 0) items.append(',');
            items.append(value(s.fromBottom(i)));
        }
        items.append(']');
        Json j = Json.obj().put("kind", "stack").raw("items", items.toString()).put("cap", s.capacity());
        if (extra != null) j.raw("extra", extra);
        return j.end();
    }

    private void stack(String script) {
        ArrayStack<Integer> s = new ArrayStack<Integer>(4);
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            try {
                if (op.equals("push")) {
                    need(tok, 1, "push <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        int v = Input.parseInt(tok.get(i), "push");
                        long before = s.ops;
                        s.push(v);
                        String msg = "push(" + v + "): " + v + " is the new top";
                        if (s.lastPushResized) msg += ". The array was full, so capacity doubled to " + s.capacity()
                                + " (copying " + (s.size() - 1) + " items, which costs O(n) this once)";
                        step("op", msg, s.ops - before, s.lastPushResized ? "O(n) resize, O(1) amortised" : "O(1)", rec ? stackSnap(s, null) : null);
                    }
                } else if (op.equals("pop")) {
                    long before = s.ops;
                    int v = s.pop();
                    step("op", "pop() removed the top item " + v, s.ops - before, "O(1)", rec ? stackSnap(s, null) : null);
                } else if (op.equals("peek") || op.equals("top")) {
                    long before = s.ops;
                    int v = s.peek();
                    step("op", "peek() = " + v + " (the stack is unchanged)", s.ops - before, "O(1)", rec ? stackSnap(s, null) : null);
                } else if (op.equals("size")) {
                    step("op", "size() = " + s.size(), 1, "O(1)", rec ? stackSnap(s, null) : null);
                } else if (op.equals("isempty") || op.equals("empty")) {
                    step("op", "isEmpty() = " + s.isEmpty(), 1, "O(1)", rec ? stackSnap(s, null) : null);
                } else throw new IllegalArgumentException("Unknown stack command \"" + tok.get(0) + "\". Use push, pop, peek, size or isEmpty.");
            } catch (IllegalStateException e) {
                step("error", e.getMessage(), 0, "–", rec ? stackSnap(s, null) : null);
            }
        }
        summary = "Stack holds " + s.size() + " item(s); top = " + (s.isEmpty() ? "none" : String.valueOf(s.fromBottom(s.size() - 1)));
    }

    // ------------------------------------------------------------------ Queue
    private String queueSnap(CircularQueue<Integer> q) {
        StringBuilder items = new StringBuilder("[");
        for (int i = 0; i < q.size(); i++) { if (i > 0) items.append(','); items.append(q.fromFront(i)); }
        items.append(']');
        StringBuilder slots = new StringBuilder("[");
        for (int i = 0; i < q.capacity(); i++) { if (i > 0) slots.append(','); slots.append(value(q.slot(i))); }
        slots.append(']');
        return Json.obj().put("kind", "queue").raw("items", items.toString()).raw("slots", slots.toString())
                .put("front", q.front()).put("rear", q.rear()).put("size", q.size()).put("cap", q.capacity()).end();
    }

    private void queue(String script) {
        CircularQueue<Integer> q = new CircularQueue<Integer>(4);
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            try {
                if (op.equals("enqueue") || op.equals("add") || op.equals("offer")) {
                    need(tok, 1, "enqueue <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        int v = Input.parseInt(tok.get(i), "enqueue");
                        long before = q.ops;
                        q.enqueue(v);
                        String msg = "enqueue(" + v + "): added at the rear";
                        if (q.lastEnqueueResized) msg += ". The ring was full, so it was unrolled into an array of capacity " + q.capacity();
                        step("op", msg, q.ops - before, q.lastEnqueueResized ? "O(n) resize, O(1) amortised" : "O(1)", rec ? queueSnap(q) : null);
                    }
                } else if (op.equals("dequeue") || op.equals("poll") || op.equals("remove")) {
                    long before = q.ops;
                    int v = q.dequeue();
                    step("op", "dequeue() removed " + v + " from the front; front moves to slot " + q.front(),
                            q.ops - before, "O(1)", rec ? queueSnap(q) : null);
                } else if (op.equals("peek") || op.equals("front")) {
                    long before = q.ops;
                    int v = q.peek();
                    step("op", "peek() = " + v, q.ops - before, "O(1)", rec ? queueSnap(q) : null);
                } else if (op.equals("size")) {
                    step("op", "size() = " + q.size(), 1, "O(1)", rec ? queueSnap(q) : null);
                } else throw new IllegalArgumentException("Unknown queue command \"" + tok.get(0) + "\". Use enqueue, dequeue, peek or size.");
            } catch (IllegalStateException e) {
                step("error", e.getMessage(), 0, "–", rec ? queueSnap(q) : null);
            }
        }
        summary = "Queue holds " + q.size() + " item(s)" + (q.isEmpty() ? "" : "; front = " + q.fromFront(0));
    }

    // ------------------------------------------------------------------ Linked list
    private String listSnap(SinglyLinkedList l) {
        StringBuilder items = new StringBuilder("[");
        int i = 0;
        for (SinglyLinkedList.Node n = l.head(); n != null; n = n.next) {
            if (i++ > 0) items.append(',');
            items.append(n.value);
        }
        items.append(']');
        Json j = Json.obj().put("kind", "list").raw("items", items.toString());
        if (l.lastIndex >= 0) j.put("hi", new int[]{l.lastIndex});
        return j.end();
    }

    private void list(String script) {
        SinglyLinkedList l = new SinglyLinkedList();
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            long before = l.ops;
            try {
                if (op.equals("addfirst") || op.equals("prepend")) {
                    need(tok, 1, "addFirst <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        before = l.ops;
                        int v = Input.parseInt(tok.get(i), "addFirst");
                        l.addFirst(v);
                        step("op", "addFirst(" + v + "): new node becomes the head", l.ops - before, "O(1)", rec ? listSnap(l) : null);
                    }
                } else if (op.equals("addlast") || op.equals("append") || op.equals("add")) {
                    need(tok, 1, "addLast <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        before = l.ops;
                        int v = Input.parseInt(tok.get(i), "addLast");
                        l.addLast(v);
                        step("op", "addLast(" + v + "): linked after the tail (we keep a tail pointer)", l.ops - before, "O(1)", rec ? listSnap(l) : null);
                    }
                } else if (op.equals("insert") || op.equals("insertat")) {
                    need(tok, 2, "insert <position> <number>");
                    int pos = Input.parseInt(tok.get(1), "position");
                    int v = Input.parseInt(tok.get(2), "value");
                    l.insertAt(pos, v);
                    step("op", "insert(" + pos + ", " + v + "): walked " + (pos > 0 ? pos - 1 : 0) + " link(s) to the node before position " + pos,
                            l.ops - before, "O(n)", rec ? listSnap(l) : null);
                } else if (op.equals("removefirst")) {
                    int v = l.removeFirst();
                    step("op", "removeFirst() removed " + v, l.ops - before, "O(1)", rec ? listSnap(l) : null);
                } else if (op.equals("remove") || op.equals("delete")) {
                    need(tok, 1, "remove <value>");
                    int v = Input.parseInt(tok.get(1), "value");
                    int idx = l.removeValue(v);
                    step(idx >= 0 ? "op" : "error", idx >= 0 ? "remove(" + v + "): found at position " + idx + " and unlinked"
                            : "remove(" + v + "): " + v + " is not in the list", l.ops - before, "O(n)", rec ? listSnap(l) : null);
                } else if (op.equals("search") || op.equals("find") || op.equals("indexof")) {
                    need(tok, 1, "search <value>");
                    int v = Input.parseInt(tok.get(1), "value");
                    int idx = l.indexOf(v);
                    step("op", idx >= 0 ? "search(" + v + "): found at position " + idx + " after " + (idx + 1) + " node visit(s)"
                            : "search(" + v + "): not found after visiting all " + l.size() + " nodes", l.ops - before, "O(n)", rec ? listSnap(l) : null);
                } else if (op.equals("get")) {
                    need(tok, 1, "get <position>");
                    int pos = Input.parseInt(tok.get(1), "position");
                    int v = l.get(pos);
                    step("op", "get(" + pos + ") = " + v + ". A linked list has no random access, so we walk from the head",
                            l.ops - before, "O(n)", rec ? listSnap(l) : null);
                } else if (op.equals("reverse")) {
                    l.reverse();
                    step("op", "reverse(): every next pointer now points backwards", l.ops - before, "O(n)", rec ? listSnap(l) : null);
                } else if (op.equals("size")) {
                    step("op", "size() = " + l.size(), 1, "O(1)", rec ? listSnap(l) : null);
                } else throw new IllegalArgumentException("Unknown list command \"" + tok.get(0)
                        + "\". Use addFirst, addLast, insert, removeFirst, remove, search, get, reverse or size.");
            } catch (IllegalStateException | IndexOutOfBoundsException e) {
                step("error", e.getMessage(), 0, "–", rec ? listSnap(l) : null);
            }
        }
        summary = "List has " + l.size() + " node(s)";
    }

    // ------------------------------------------------------------------ Binary search tree
    private static String treeJson(BinarySearchTree.Node n) {
        if (n == null) return "null";
        return "{\"k\":" + n.key + ",\"l\":" + treeJson(n.left) + ",\"r\":" + treeJson(n.right) + "}";
    }

    private String bstSnap(BinarySearchTree t, String out) {
        Json j = Json.obj().put("kind", "tree").raw("root", treeJson(t.root())).put("path", t.path().toArray());
        if (out != null) j.raw("out", out);
        return j.end();
    }

    private void bst(String script) {
        BinarySearchTree t = new BinarySearchTree();
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            long before = t.ops;
            try {
                if (op.equals("insert") || op.equals("add")) {
                    need(tok, 1, "insert <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        before = t.ops;
                        int v = Input.parseInt(tok.get(i), "insert");
                        boolean added = t.insert(v);
                        step(added ? "op" : "error", added ? "insert(" + v + "): compared along the path, then attached as a leaf"
                                : "insert(" + v + "): " + v + " is already in the tree (no duplicates)", t.ops - before,
                                "O(h): O(log n) balanced, O(n) skewed", rec ? bstSnap(t, null) : null);
                    }
                } else if (op.equals("search") || op.equals("find") || op.equals("contains")) {
                    need(tok, 1, "search <number>");
                    int v = Input.parseInt(tok.get(1), "search");
                    boolean found = t.search(v);
                    step("op", "search(" + v + "): " + (found ? "found" : "not found") + " after " + t.path().size() + " comparison(s)",
                            t.ops - before, "O(h)", rec ? bstSnap(t, null) : null);
                } else if (op.equals("delete") || op.equals("remove")) {
                    need(tok, 1, "delete <number>");
                    int v = Input.parseInt(tok.get(1), "delete");
                    String how = t.delete(v);
                    step(how != null ? "op" : "error", how != null ? "delete(" + v + "): " + how : "delete(" + v + "): not in the tree",
                            t.ops - before, "O(h)", rec ? bstSnap(t, null) : null);
                } else if (op.equals("inorder") || op.equals("preorder") || op.equals("postorder")) {
                    IntList out = new IntList();
                    if (op.equals("inorder")) t.inorder(t.root(), out);
                    else if (op.equals("preorder")) t.preorder(t.root(), out);
                    else t.postorder(t.root(), out);
                    t.clearPath();
                    String note = op.equals("inorder") ? " (in-order of a BST is always sorted)" : "";
                    step("op", op + "() = " + Json.arr(out.toArray()) + note, t.ops - before, "O(n)",
                            rec ? bstSnap(t, Json.arr(out.toArray())) : null);
                } else if (op.equals("height")) {
                    int h = t.height(t.root());
                    t.clearPath();
                    step("op", "height() = " + h + " edges on the longest root-to-leaf path (" + t.size() + " nodes; a balanced tree would have height "
                            + floorLog2(t.size()) + ")", t.ops - before, "O(n)", rec ? bstSnap(t, null) : null);
                } else if (op.equals("min") || op.equals("max")) {
                    int v = op.equals("min") ? t.min() : t.max();
                    step("op", op + "() = " + v + " (keep going " + (op.equals("min") ? "left" : "right") + ")",
                            t.ops - before, "O(h)", rec ? bstSnap(t, null) : null);
                } else throw new IllegalArgumentException("Unknown tree command \"" + tok.get(0)
                        + "\". Use insert, search, delete, inorder, preorder, postorder, height, min or max.");
            } catch (IllegalStateException e) {
                step("error", e.getMessage(), 0, "–", rec ? bstSnap(t, null) : null);
            }
        }
        summary = "Tree has " + t.size() + " node(s), height " + t.height(t.root());
    }

    private static int floorLog2(int n) {
        int h = -1;
        while (n > 0) { n >>= 1; h++; }
        return h;
    }

    // ------------------------------------------------------------------ Min-heap
    private String heapSnap(MinHeap h) {
        int[] items = new int[h.size()];
        for (int i = 0; i < h.size(); i++) items[i] = h.get(i);
        return Json.obj().put("kind", "heap").put("items", items).put("hi", h.touched.toArray()).end();
    }

    private void heap(String script) {
        MinHeap h = new MinHeap();
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            long before = h.ops;
            try {
                if (op.equals("insert") || op.equals("add") || op.equals("push")) {
                    need(tok, 1, "insert <number> [more numbers]");
                    for (int i = 1; i < tok.size(); i++) {
                        before = h.ops;
                        int v = Input.parseInt(tok.get(i), "insert");
                        h.insert(v);
                        step("op", "insert(" + v + "): placed at the end, then sifted up " + (h.touched.size() - 1) + " level(s)",
                                h.ops - before, "O(log n)", rec ? heapSnap(h) : null);
                    }
                } else if (op.equals("extract") || op.equals("extractmin") || op.equals("pop") || op.equals("poll")) {
                    int v = h.extractMin();
                    step("op", "extractMin() = " + v + ": last item moved to the root, then sifted down", h.ops - before,
                            "O(log n)", rec ? heapSnap(h) : null);
                } else if (op.equals("peek") || op.equals("min")) {
                    int v = h.peek();
                    step("op", "peek() = " + v + " (the minimum is always at the root)", h.ops - before, "O(1)", rec ? heapSnap(h) : null);
                } else if (op.equals("build") || op.equals("heapify")) {
                    need(tok, 1, "build <numbers...>");
                    IntList vals = new IntList();
                    for (int i = 1; i < tok.size(); i++) vals.add(Input.parseInt(tok.get(i), "build"));
                    h.build(vals.toArray());
                    step("op", "build(): bottom-up heapify of " + vals.size() + " values", h.ops - before, "O(n)", rec ? heapSnap(h) : null);
                } else if (op.equals("size")) {
                    step("op", "size() = " + h.size(), 1, "O(1)", rec ? heapSnap(h) : null);
                } else throw new IllegalArgumentException("Unknown heap command \"" + tok.get(0) + "\". Use insert, extractMin, peek, build or size.");
            } catch (IllegalStateException e) {
                step("error", e.getMessage(), 0, "–", rec ? heapSnap(h) : null);
            }
        }
        summary = "Heap holds " + h.size() + " item(s)" + (h.size() > 0 ? "; minimum = " + h.get(0) : "");
    }

    // ------------------------------------------------------------------ Hash table
    private String hashSnap(HashTable<String> t, int hiBucket) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < t.bucketCount(); i++) {
            if (i > 0) b.append(',');
            b.append('[');
            boolean first = true;
            for (HashTable.Entry<String> e = t.bucket(i); e != null; e = e.next) {
                if (!first) b.append(',');
                first = false;
                b.append(Json.obj().put("k", e.key).put("v", e.value).end());
            }
            b.append(']');
        }
        b.append(']');
        return Json.obj().put("kind", "hash").raw("buckets", b.toString()).put("hi", hiBucket)
                .put("size", t.size()).end();
    }

    private void hash(String script) {
        HashTable<String> t = new HashTable<String>(8);
        DynamicArray<String> cmds = commands(script);
        for (int c = 0; c < cmds.size(); c++) {
            DynamicArray<String> tok = Input.tokens(cmds.get(c));
            String op = lower(tok.get(0));
            long before = t.ops;
            if (op.equals("put") || op.equals("set") || op.equals("insert")) {
                need(tok, 2, "put <key> <value>");
                String k = tok.get(1), v = tok.get(2);
                String old = t.put(k, v);
                String msg = "put(" + k + ", " + v + "): hash(\"" + k + "\") = " + HashTable.hash(k) + ", bucket " + t.indexFor(k)
                        + (old != null ? "; replaced old value " + old : "");
                if (t.lastPutResized) msg += ". Load factor passed 0.75, so the table doubled to " + t.bucketCount() + " buckets and every key was rehashed";
                step("op", msg, t.ops - before, t.lastPutResized ? "O(n) resize, O(1) amortised" : "O(1) average", rec ? hashSnap(t, t.lastBucket) : null);
            } else if (op.equals("get")) {
                need(tok, 1, "get <key>");
                String v = t.get(tok.get(1));
                step(v != null ? "op" : "error", "get(" + tok.get(1) + ") = " + (v == null ? "not found" : v) + " (bucket " + t.lastBucket + ")",
                        t.ops - before, "O(1) average", rec ? hashSnap(t, t.lastBucket) : null);
            } else if (op.equals("remove") || op.equals("delete")) {
                need(tok, 1, "remove <key>");
                String v = t.remove(tok.get(1));
                step(v != null ? "op" : "error", v != null ? "remove(" + tok.get(1) + ") removed value " + v : "remove(" + tok.get(1) + "): key not found",
                        t.ops - before, "O(1) average", rec ? hashSnap(t, t.lastBucket) : null);
            } else throw new IllegalArgumentException("Unknown hash table command \"" + tok.get(0) + "\". Use put, get or remove.");
        }
        summary = "Table holds " + t.size() + " key(s) in " + t.bucketCount() + " buckets";
    }

    // ------------------------------------------------------------------ Stack application 1: balanced brackets
    private static char partner(char close) {
        return close == ')' ? '(' : close == ']' ? '[' : '{';
    }

    private void brackets(String text) {
        if (text == null || text.trim().length() == 0) throw new IllegalArgumentException("Type some text with brackets, e.g. {[a+b]*(c-d)}");
        if (text.length() > 500) throw new IllegalArgumentException("Keep the text under 500 characters.");
        ArrayStack<String> s = new ArrayStack<String>();
        boolean ok = true;
        String why = "";
        for (int i = 0; i < text.length() && ok; i++) {
            char ch = text.charAt(i);
            long before = s.ops;
            if (ch == '(' || ch == '[' || ch == '{') {
                s.push(String.valueOf(ch));
                step("op", "'" + ch + "' at position " + i + " opens a group: push it", s.ops - before, "O(1)",
                        rec ? stackSnap(s, Json.obj().put("text", text).put("pos", i).end()) : null);
            } else if (ch == ')' || ch == ']' || ch == '}') {
                if (s.isEmpty()) {
                    ok = false;
                    why = "'" + ch + "' at position " + i + " has no matching opening bracket";
                    step("error", why, 1, "O(1)", rec ? stackSnap(s, Json.obj().put("text", text).put("pos", i).end()) : null);
                } else {
                    String top = s.pop();
                    if (top.charAt(0) != partner(ch)) {
                        ok = false;
                        why = "'" + ch + "' at position " + i + " closes '" + top + "', which does not match";
                        step("error", why, s.ops - before + 1, "O(1)", rec ? stackSnap(s, Json.obj().put("text", text).put("pos", i).end()) : null);
                    } else {
                        step("op", "'" + ch + "' matches the top '" + top + "': pop it", s.ops - before + 1, "O(1)",
                                rec ? stackSnap(s, Json.obj().put("text", text).put("pos", i).end()) : null);
                    }
                }
            }
        }
        if (ok && !s.isEmpty()) {
            ok = false;
            why = s.size() + " bracket(s) were never closed";
            step("error", why, 1, "O(1)", rec ? stackSnap(s, Json.obj().put("text", text).put("pos", text.length()).end()) : null);
        }
        if (ok) step("done", "The stack is empty at the end: the brackets are balanced", 1, "O(n) overall",
                rec ? stackSnap(s, Json.obj().put("text", text).put("pos", text.length()).end()) : null);
        summary = ok ? "Balanced" : "Not balanced: " + why;
    }

    // ------------------------------------------------------------------ Stack application 2: infix → postfix → value
    private static int prec(String op) {
        if (op.equals("u-")) return 4;
        if (op.equals("^")) return 3;
        if (op.equals("*") || op.equals("/") || op.equals("%")) return 2;
        return 1; // + -
    }

    private static boolean rightAssoc(String op) { return op.equals("^") || op.equals("u-"); }

    private static boolean isOp(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("%") || s.equals("^") || s.equals("u-");
    }

    private static DynamicArray<String> lex(String e) {
        DynamicArray<String> out = new DynamicArray<String>();
        int i = 0;
        while (i < e.length()) {
            char c = e.charAt(i);
            if (c == ' ' || c == '\t') { i++; continue; }
            if ((c >= '0' && c <= '9') || c == '.') {
                int s = i;
                while (i < e.length() && ((e.charAt(i) >= '0' && e.charAt(i) <= '9') || e.charAt(i) == '.')) i++;
                out.add(e.substring(s, i));
                continue;
            }
            if (c == '-') {
                boolean unary = out.size() == 0 || isOp(out.get(out.size() - 1)) || out.get(out.size() - 1).equals("(");
                out.add(unary ? "u-" : "-");
                i++;
                continue;
            }
            if ("+*/%^()".indexOf(c) >= 0) { out.add(String.valueOf(c)); i++; continue; }
            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + i
                    + ". Use numbers, + - * / % ^ and brackets.");
        }
        return out;
    }

    private String exprSnap(ArrayStack<String> ops, DynamicArray<String> output, String phase, ArrayStack<Double> vals) {
        Json j = Json.obj().put("kind", "expr").put("phase", phase);
        StringBuilder o = new StringBuilder("[");
        for (int i = 0; i < output.size(); i++) { if (i > 0) o.append(','); o.append(Json.quote(output.get(i))); }
        j.raw("output", o.append(']').toString());
        StringBuilder st = new StringBuilder("[");
        if (vals != null) {
            for (int i = 0; i < vals.size(); i++) { if (i > 0) st.append(','); st.append(Json.quote(fmt(vals.fromBottom(i)))); }
        } else {
            for (int i = 0; i < ops.size(); i++) { if (i > 0) st.append(','); st.append(Json.quote(ops.fromBottom(i))); }
        }
        j.raw("stack", st.append(']').toString());
        return j.end();
    }

    private void expression(String text) {
        if (text == null || text.trim().length() == 0) throw new IllegalArgumentException("Type an expression such as 3 + 4 * (2 - 1) ^ 2");
        if (text.length() > 300) throw new IllegalArgumentException("Keep the expression under 300 characters.");
        DynamicArray<String> tokens = lex(text);
        // ---- Phase 1: shunting-yard (Dijkstra) converts infix to postfix with an operator stack
        ArrayStack<String> ops = new ArrayStack<String>();
        DynamicArray<String> output = new DynamicArray<String>();
        for (int i = 0; i < tokens.size(); i++) {
            String tk = tokens.get(i);
            long before = ops.ops;
            if (tk.equals("(")) {
                ops.push(tk);
                step("op", "'(' : push onto the operator stack", ops.ops - before, "O(1)", rec ? exprSnap(ops, output, "convert", null) : null);
            } else if (tk.equals(")")) {
                boolean matched = false;
                while (!ops.isEmpty()) {
                    String top = ops.pop();
                    if (top.equals("(")) { matched = true; break; }
                    output.add(top);
                }
                if (!matched) throw new IllegalArgumentException("A ')' has no matching '('.");
                step("op", "')' : pop operators to the output until '('", ops.ops - before + 1, "O(k)", rec ? exprSnap(ops, output, "convert", null) : null);
            } else if (isOp(tk)) {
                int popped = 0;
                while (!ops.isEmpty() && isOp(ops.peek())) {
                    String top = ops.peek();
                    if (prec(top) > prec(tk) || (prec(top) == prec(tk) && !rightAssoc(tk))) { output.add(ops.pop()); popped++; }
                    else break;
                }
                ops.push(tk);
                String name = tk.equals("u-") ? "unary minus" : "'" + tk + "'";
                step("op", name + " : " + (popped > 0 ? "pop " + popped + " operator(s) of higher or equal precedence, then " : "") + "push it",
                        ops.ops - before, "O(1) amortised", rec ? exprSnap(ops, output, "convert", null) : null);
            } else {
                output.add(tk);
                step("op", tk + " : numbers go straight to the output", 1, "O(1)", rec ? exprSnap(ops, output, "convert", null) : null);
            }
        }
        while (!ops.isEmpty()) {
            String top = ops.pop();
            if (top.equals("(")) throw new IllegalArgumentException("A '(' was never closed.");
            output.add(top);
        }
        StringBuilder postfix = new StringBuilder();
        for (int i = 0; i < output.size(); i++) { if (i > 0) postfix.append(' '); postfix.append(output.get(i).equals("u-") ? "neg" : output.get(i)); }
        step("info", "End of input: pop the remaining operators. Postfix = " + postfix, 1, "O(n)", rec ? exprSnap(ops, output, "convert", null) : null);

        // ---- Phase 2: evaluate the postfix expression with a value stack
        ArrayStack<Double> vals = new ArrayStack<Double>();
        for (int i = 0; i < output.size(); i++) {
            String tk = output.get(i);
            long before = vals.ops;
            if (!isOp(tk)) {
                double v = parseNumber(tk);
                vals.push(v);
                step("op", "Push " + tk, vals.ops - before, "O(1)", rec ? exprSnap(null, output, "evaluate", vals) : null);
            } else if (tk.equals("u-")) {
                if (vals.isEmpty()) throw new IllegalArgumentException("The expression is incomplete.");
                double a = vals.pop();
                vals.push(-a);
                step("op", "Negate " + fmt(a), vals.ops - before + 1, "O(1)", rec ? exprSnap(null, output, "evaluate", vals) : null);
            } else {
                if (vals.size() < 2) throw new IllegalArgumentException("Operator '" + tk + "' is missing an operand.");
                double b = vals.pop(), a = vals.pop();
                double r;
                if (tk.equals("+")) r = a + b;
                else if (tk.equals("-")) r = a - b;
                else if (tk.equals("*")) r = a * b;
                else if (tk.equals("/")) { if (b == 0) throw new IllegalArgumentException("Division by zero."); r = a / b; }
                else if (tk.equals("%")) { if (b == 0) throw new IllegalArgumentException("Modulo by zero."); r = a % b; }
                else r = power(a, b);
                vals.push(r);
                step("op", "Pop " + fmt(a) + " and " + fmt(b) + ", push " + fmt(a) + " " + tk + " " + fmt(b) + " = " + fmt(r),
                        vals.ops - before + 1, "O(1)", rec ? exprSnap(null, output, "evaluate", vals) : null);
            }
        }
        if (vals.size() != 1) throw new IllegalArgumentException("The expression is incomplete: check for missing operators.");
        summary = "Value: " + fmt(vals.peek()) + ", postfix form: " + postfix;
    }

    private static double parseNumber(String s) {
        double v = 0, scale = 0;
        boolean dot = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (dot) throw new IllegalArgumentException("\"" + s + "\" is not a valid number.");
                dot = true;
                scale = 1;
                continue;
            }
            if (dot) { scale /= 10; v += (c - '0') * scale; }
            else v = v * 10 + (c - '0');
        }
        return v;
    }

    /** a^b for whole b by repeated squaring; fractional exponents are rejected. */
    private static double power(double a, double b) {
        if (b != (long) b) throw new IllegalArgumentException("Only whole-number exponents are supported.");
        long n = (long) b;
        boolean neg = n < 0;
        if (neg) n = -n;
        double result = 1, base = a;
        while (n > 0) {
            if ((n & 1) == 1) result *= base;
            base *= base;
            n >>= 1;
        }
        return neg ? 1 / result : result;
    }
}
