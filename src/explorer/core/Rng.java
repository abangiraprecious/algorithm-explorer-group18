package explorer.core;

/** Xorshift64* pseudo-random generator (written by hand instead of java.util.Random). */
public final class Rng {
    private long state;

    public Rng(long seed) {
        state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
    }

    public long nextLong() {
        state ^= state >>> 12;
        state ^= state << 25;
        state ^= state >>> 27;
        return state * 2685821657736338717L;
    }

    /** Uniform int in [0, bound). */
    public int nextInt(int bound) {
        long r = (nextLong() >>> 1) % bound;
        return (int) r;
    }

    /** Uniform int in [lo, hi]. */
    public int between(int lo, int hi) {
        return lo + nextInt(hi - lo + 1);
    }
}
