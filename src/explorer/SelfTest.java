package explorer;

import explorer.algorithms.DataStructures;
import explorer.server.Params;

/**
 * Automated Self-Tests for Data Structures + Server Params
 */
public class SelfTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("PASS: " + name);
            passed++;
        } else {
            System.out.println("FAIL: " + name);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("Running SelfTest...");

        // DataStructures tests
        check("Balanced (a+b) is true", DataStructures.isBalanced("(a+b)"));
        check("Balanced (a+b] is false", !DataStructures.isBalanced("(a+b]"));
        check("Balanced empty is true", DataStructures.isBalanced(""));
        check("Reverse hello = olleh", DataStructures.reverseString("hello").equals("olleh"));
        check("Postfix 2 3 + 5 * = 25", DataStructures.evaluatePostfix("2 3 + 5 *") == 25);
        check("Postfix 10 2 / = 5", DataStructures.evaluatePostfix("10 2 /") == 5);

        // Params tests
        Params p1 = new Params("action=balanced&input=(a+b)");
        check("Params action=balanced", "balanced".equals(p1.get("action", "")));
        check("Params input=(a+b)", "(a+b)".equals(p1.get("input", "")));

        Params p2 = new Params(null);
        check("Params default works", "demo".equals(p2.get("action", "demo")));

        Params p3 = new Params("action=reverse&input=hello%20world");
        check("Params decodes %20", "hello world".equals(p3.get("input", "")));

        System.out.println("---------------------------------");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            System.out.println("ALL TESTS PASSED");
        } else {
            System.out.println("SOME TESTS FAILED");
            System.exit(1);
        }
    }
}
