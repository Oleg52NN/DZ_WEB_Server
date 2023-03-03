package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class MyServer {
    static MyServer work = null;
    private final int port;

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private MyServer(int port) {
        this.port = port;
    }

    public static MyServer myServerGet(int numberPort) {
        if (work == null) {
            work = new MyServer(numberPort);
        }
        return work;
    }

    public void serverStart() {

        int poolThread = 64;
        ExecutorService service = Executors.newFixedThreadPool(poolThread);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {

                        Future<String> namePool = service.submit(handler(in, out));
                        System.out.println(namePool.get());

               } catch (ExecutionException | InterruptedException e) {
                   throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }

    public Callable<String> handler(BufferedReader in, BufferedOutputStream out) throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        final var path = parts[1];
        if (parts.length != 3) {
            return null;
        }


        if (!validPaths.contains(parts[1])) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return null;
        }

        final var filePath = Path.of(".", "public/", path);
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
        return () -> Thread.currentThread().getName();
    }
}