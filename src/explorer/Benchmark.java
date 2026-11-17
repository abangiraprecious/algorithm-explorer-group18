package explorer;

import explorer.algorithms.Analyzer;
import explorer.algorithms.Sorting;
import explorer.core.Input;
import explorer.core.Tracer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Module D experiment: times every sorting algorithm from Module C on random, already sorted
 * and reverse sorted input for n = 100, 500, 1,000, 2,000, 5,000 and 10,000, and merge sort and
 * both quick sorts also for n = 50,000 and 100,000. Each run is repeated (3 times by default)
 * and the average is reported. The results go to results/module_d.csv, which the report reads.
 *
 *   java -cp out explorer.Benchmark [runs]
 */
public final class Benchmark {

    private static final String[] ALGOS = {"bubble", "selection", "insertion", "merge", "quick", "quick-random"};
    private static final String[] ORDERS = {"random", "sorted", "reversed"};
    private static final int[] SIZES = {100, 500, 1000, 2000, 5000, 10000};
    private static final int[] LARGE = {50000, 100000};

    public static void main(String[] args) throws Exception {
        final int runs = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        if (runs < 3) throw new IllegalArgumentException("The brief asks for at least 3 runs.");
        // run on a thread with a big stack: first-element quick sort on 100,000 sorted numbers recurses 100,000 deep
        final Throwable[] err = new Throwable[1];
        Thread worker = new Thread(null, () -> {
            try { measureAll(runs); } catch (Throwable t) { err[0] = t; }
        }, "benchmark", 1L << 30);
        worker.start();
        worker.join();
        if (err[0] != null) throw new RuntimeException(err[0]);
    }

    private static boolean nlogn(String algo) {
        return algo.equals("merge") || algo.equals("quick") || algo.equals("quick-random");
    }

    private static void measureAll(int runs) throws IOException {
        // warm up the JIT so the first sizes are not unfairly slow
        for (int w = 0; w < 20; w++)
            for (int k = 0; k < ALGOS.length; k++)
                for (int o = 0; o < ORDERS.length; o++) Sorting.run(ALGOS[k], Analyzer.generate(2000, ORDERS[o], 7), Tracer.silent());

        new java.io.File("results").mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream("results/module_d.csv"), StandardCharsets.UTF_8);
        StringBuilder head = new StringBuilder("algorithm,order,n");
        for (int r = 1; r <= runs; r++) head.append(",run").append(r).append("_ms");
        head.append(",avg_ms,comparisons,swaps,writes\n");
        out.write(head.toString());

        for (int a = 0; a < ALGOS.length; a++) {
            for (int o = 0; o < ORDERS.length; o++) {
                for (int s = 0; s < SIZES.length + LARGE.length; s++) {
                    boolean large = s >= SIZES.length;
                    if (large && !nlogn(ALGOS[a])) continue;
                    int n = large ? LARGE[s - SIZES.length] : SIZES[s];
                    out.write(measure(ALGOS[a], ORDERS[o], n, runs));
                    out.flush();
                }
            }
        }
        out.close();
        System.out.println("Wrote results/module_d.csv");
    }

    /** Times one algorithm on one input `runs` times and returns one CSV line. */
    private static String measure(String algo, String order, int n, int runs) {
        int[] base = Analyzer.generate(n, order, 42);
        long[] ns = new long[runs];
        Tracer counts = null;
        for (int r = 0; r < runs; r++) {
            int[] a = Input.copy(base);
            Tracer t = Tracer.silent();
            long start = System.nanoTime();
            Sorting.run(algo, a, t);
            ns[r] = System.nanoTime() - start;
            if (!Sorting.isSorted(a)) throw new IllegalStateException(algo + " did not sort");
            if (r == 0) counts = t;
        }
        long sum = 0;
        StringBuilder line = new StringBuilder(algo + "," + order + "," + n);
        for (int r = 0; r < runs; r++) { line.append(',').append(ms(ns[r])); sum += ns[r]; }
        line.append(',').append(ms(sum / runs)).append(',').append(counts.comparisons).append(',')
                .append(counts.swaps).append(',').append(counts.writes).append('\n');
        System.out.print(line);
        return line.toString();
    }

    private static String ms(long ns) {
        long micros = ns / 1000;
        return (micros / 1000) + "." + pad3(micros % 1000);
    }

    private static String pad3(long v) {
        return v < 10 ? "00" + v : v < 100 ? "0" + v : String.valueOf(v);
    }
}
