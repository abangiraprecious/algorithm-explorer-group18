package explorer.core;

import explorer.ds.ArrayStack;
import explorer.ds.DynamicArray;

/**
 * Records what an algorithm does: operation counters, recursion depth and
 * (optionally) a list of steps for the interface to replay.
 *
 * Every algorithm gets one Tracer. The same algorithm is run twice per request:
 *   1. with recording ON  – to collect the steps that are shown to the user
 *   2. with recording OFF – to measure running time without the cost of building steps
 */
public final class Tracer {
    // ---- operation counters ----
    public long comparisons;
    public long swaps;
    public long writes;      // array assignments / element moves (not counted inside swaps)
    public long calls;       // recursive calls
    public long arith;       // arithmetic operations (+, *, %, ...) in recursive algorithms
    public int depth;
    public int maxDepth;

    // ---- step recording ----
    private final boolean record;
    private final int limit;
    private final DynamicArray<String> steps = new DynamicArray<String>(64);
    private boolean truncated;

    // ---- call stack mirror, used to show recursion frames ----
    private final ArrayStack<String> frameLabels = new ArrayStack<String>();
    private final ArrayStack<Integer> frameIds = new ArrayStack<Integer>();
    private int nextId;

    public Tracer(boolean record, int limit) {
        this.record = record;
        this.limit = limit;
    }

    public static Tracer silent() { return new Tracer(false, 0); }

    /**
     * True when a step should be recorded now. Algorithms check this BEFORE
     * building a message string so silent (timing) runs stay fast.
     */
    public boolean on() {
        if (!record) return false;
        if (steps.size() < limit) return true;
        truncated = true;
        return false;
    }

    public void add(String stepJson) { steps.add(stepJson); }

    /** True when this tracer records steps at all (used to skip building labels in timing runs). */
    public boolean rec() { return record; }

    /** Total basic operations = comparisons + swaps + writes + arithmetic + calls. */
    public long totalOps() { return comparisons + swaps + writes + arith + calls; }

    /** Counter values as a JSON object. */
    public String countsJson() {
        return Json.obj().put("comparisons", comparisons).put("swaps", swaps).put("writes", writes)
                .put("arith", arith).put("calls", calls).put("maxDepth", maxDepth)
                .put("total", totalOps()).end();
    }

    public boolean truncated() { return truncated; }
    public int stepCount() { return steps.size(); }

    public String stepsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(steps.get(i));
        }
        return sb.append(']').toString();
    }

    // ------------------------------------------------------------------
    // Recursion helpers
    // ------------------------------------------------------------------

    /** Counts a recursive call without recording a step (used inside sorts). */
    public void enterQuiet() {
        calls++;
        depth++;
        if (depth > maxDepth) maxDepth = depth;
    }

    public void exitQuiet() { depth--; }

    /** Records a call frame being pushed. Returns the frame id to pass to exit(). */
    public int enter(String label) {
        calls++;
        depth++;
        if (depth > maxDepth) maxDepth = depth;
        int id = nextId++;
        if (record) {
            int parent = frameIds.isEmpty() ? -1 : frameIds.peek();
            frameLabels.push(label);
            frameIds.push(id);
            if (on()) {
                add(Json.obj().put("t", "call").put("m", "Call " + label)
                        .put("id", id).put("p", parent).put("d", depth)
                        .put("label", label).raw("stack", stackJson()).end());
            }
        }
        return id;
    }

    /** Records a frame returning a value and being popped off the call stack. */
    public void exit(int id, String label, String value) {
        if (record) {
            if (on()) {
                add(Json.obj().put("t", "return").put("m", label + " returns " + value)
                        .put("id", id).put("d", depth).put("v", value)
                        .raw("stack", stackJson()).end());
            }
            frameLabels.pop();
            frameIds.pop();
        }
        depth--;
    }

    /** A note inside the current frame (e.g. a memo hit or a disk move). */
    public void note(String type, String message, String extraKey, String extraJson) {
        if (!on()) return;
        Json j = Json.obj().put("t", type).put("m", message).put("d", depth)
                .raw("stack", stackJson());
        if (extraKey != null) j.raw(extraKey, extraJson);
        add(j.end());
    }

    private String stackJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < frameLabels.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(Json.quote(frameLabels.fromBottom(i)));
        }
        return sb.append(']').toString();
    }
}
