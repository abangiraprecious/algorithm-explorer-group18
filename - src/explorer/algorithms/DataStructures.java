
package explorer.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Structure Applications - Stack-based demonstrations
 * For Algorithm Explorer Group 18
 */
public class DataStructures {

    // 1. Check if brackets are balanced using a Stack
    public static boolean isBalanced(String expression) {
        Deque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> pairs = new HashMap<>();
        pairs.put(')', '(');
        pairs.put(']', '[');
        pairs.put('}', '{');

        for (char c : expression.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    // 2. Reverse a string using a Stack
    public static String reverseString(String input) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : input.toCharArray()) {
            stack.push(c);
        }
        StringBuilder reversed = new StringBuilder();
        while (!stack.isEmpty()) {
            reversed.append(stack.pop());
        }
        return reversed.toString();
    }

    // 3. Evaluate a postfix expression using a Stack
    public static int evaluatePostfix(String expression) {
        Deque<Integer> stack = new ArrayDeque<>();
        for (String token : expression.trim().split("\\s+")) {
            if (token.matches("-?\\d+")) {
                stack.push(Integer.parseInt(token));
            } else {
                int b = stack.pop();
                int a = stack.pop();
                switch (token) {
                    case "+": stack.push(a + b); break;
                    case "-": stack.push(a - b); break;
                    case "*": stack.push(a * b); break;
                    case "/": stack.push(a / b); break;
                    default: throw new IllegalArgumentException("Unknown operator: " + token);
                }
            }
        }
        return stack.pop();
    }

    // 4. Demo method to show all applications
    public static String demo() {
        StringBuilder out = new StringBuilder();
        out.append("Balanced (a+b): ").append(isBalanced("(a+b)")).append("\n");
        out.append("Balanced (a+b]: ").append(isBalanced("(a+b]")).append("\n");
        out.append("Reverse 'hello': ").append(reverseString("hello")).append("\n");
        out.append("Postfix '2 3 + 5 *': ").append(evaluatePostfix("2 3 + 5 *")).append("\n");
        return out.toString();
    }

    public static void main(String[] args) {
        System.out.println(demo());
    }
}
