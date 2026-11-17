package explorer.server;

import com.sun.net.httpserver.HttpServer;
import explorer.algorithms.DataStructures;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Application Entry Point - HTTP Server
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default 8080");
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            Params params = new Params(exchange.getRequestURI().getRawQuery());
            String action = params.get("action", "demo");
            String input = params.get("input", "(a+b)");

            String result;
            switch (action) {
                case "balanced":
                    result = "Balanced: " + DataStructures.isBalanced(input);
                    break;
                case "reverse":
                    result = "Reversed: " + DataStructures.reverseString(input);
                    break;
                case "postfix":
                    try {
                        result = "Result: " + DataStructures.evaluatePostfix(input);
                    } catch (Exception e) {
                        result = "Error: " + e.getMessage();
                    }
                    break;
                default:
                    result = DataStructures.demo();
                    break;
            }

            byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Algorithm Explorer running on http://localhost:" + port + "/");
        System.out.println("Try: /?action=balanced&input=(a+b)");
        System.out.println("Try: /?action=reverse&input=hello");
        System.out.println("Try: /?action=postfix&input=2 3 + 5 *");
    }
}
