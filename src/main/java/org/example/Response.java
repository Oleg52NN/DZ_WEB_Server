package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class Response {
    private final String method;
    private final String path;
    private final BufferedOutputStream out;

    public Response(String method, String path, BufferedOutputStream out) {
        this.method = method;
        this.path = path;
        this.out = out;
    }

    public void selectResponse() throws IOException {
        if (method == null) {
            badRequest();
            return;
        }
        if (path == null) {
            send404();
            return;
        }
        try {
            okResponse();
        } catch (NoSuchFileException e) {
            send404();
        }
    }

    public void badRequest() throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request \r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void send404() throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void okResponse() throws IOException {

        if (method.equals("GET")) {
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
        }
        if (method.equals("POST")) {
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + "\r\n" +
                            "Content-Length: " + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        }
    }
}
