package explorer.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts Algorithm Explorer.
 *
 *   java -cp out explorer.server.Main [port]
 *
 * Uses only the HTTP server that ships with the JDK (com.sun.net.httpserver) – no frameworks,
 * no external APIs, works offline. The web page in /web talks to the routes under /run/.
 */
public final class Main {

    private static Path webRoot;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        webRoot = findWebRoot();

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.createContext("/run/", Main::handleRun);
        server.createContext("/", Main::handleStatic);
        server.start();

        String url = "http://localhost:" + port + "/";
        System.out.println("Algorithm Explorer is running at " + url);
        System.out.println("Serving the interface from " + webRoot.toAbsolutePath());
        System.out.println("Press Ctrl+C to stop.");
        openBrowser(url);
    }

    private static Path findWebRoot() {
        String[] candidates = {"web", "../web", "../../web"};
        for (int i = 0; i < candidates.length; i++) {
            Path p = Paths.get(candidates[i]);
            if (Files.isRegularFile(p.resolve("index.html"))) return p;
        }
        throw new IllegalStateException("Could not find the web folder. Run the program from the AlgorithmExplorer folder.");
    }

    private static void openBrowser(String url) {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless() && java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Throwable ignored) {
            // Not fatal: the user can open the URL by hand.
        }
    }

    // ------------------------------------------------------------------ algorithm routes
    private static void handleRun(HttpExchange ex) throws IOException {
        try {
            String route = ex.getRequestURI().getPath().substring("/run/".length());
            String body = readAll(ex.getRequestBody());
            String query = ex.getRequestURI().getRawQuery();
            final Params params = new Params(body.length() > 0 ? body : query);

            // Run on a thread with a large stack so deep recursion (e.g. quick sort on
            // 30,000 sorted numbers) does not overflow the default stack.
            final String[] out = new String[1];
            final Throwable[] err = new Throwable[1];
            Thread worker = new Thread(null, () -> {
                try {
                    switch (route) {
                        case "sort": out[0] = Api.sort(params); break;
                        case "search": out[0] = Api.search(params); break;
                        case "recursion": out[0] = Api.recursion(params); break;
                        case "ds": out[0] = Api.ds(params); break;
                        case "analyze": out[0] = Api.analyze(params); break;
                        default: throw new IllegalArgumentException("Unknown route /run/" + route);
                    }
                } catch (Throwable t) {
                    err[0] = t;
                }
            }, "algorithm-worker", 1L << 29);
            worker.start();
            worker.join();

            if (err[0] == null) {
                send(ex, 200, "application/json; charset=utf-8", out[0].getBytes(StandardCharsets.UTF_8));
            } else if (err[0] instanceof IllegalArgumentException) {
                sendError(ex, 400, err[0].getMessage());
            } else if (err[0] instanceof StackOverflowError) {
                sendError(ex, 400, "The recursion went too deep for the call stack. Try a smaller input.");
            } else if (err[0] instanceof OutOfMemoryError) {
                sendError(ex, 400, "That input needs more memory than is available. Try a smaller input.");
            } else {
                err[0].printStackTrace();
                sendError(ex, 500, "Internal error: " + err[0]);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendError(ex, 500, "Interrupted");
        }
    }

    private static void sendError(HttpExchange ex, int code, String message) throws IOException {
        String json = explorer.core.Json.obj().put("error", message == null ? "Unknown error" : message).end();
        send(ex, code, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------ static files
    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.contains("..")) { send(ex, 403, "text/plain", "Forbidden".getBytes(StandardCharsets.UTF_8)); return; }
        Path file = webRoot.resolve(path.substring(1));
        if (!Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        send(ex, 200, contentType(path), Files.readAllBytes(file));
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void send(HttpExchange ex, int code, String type, byte[] bytes) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int r;
        while ((r = in.read(chunk)) != -1) buf.write(chunk, 0, r);
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }
}
