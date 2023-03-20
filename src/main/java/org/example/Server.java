package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {
    private final int port;
    static ConcurrentMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }


    public void start() {

        int poolThread = 64;
        ExecutorService service = Executors.newFixedThreadPool(poolThread);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                        final var socket = serverSocket.accept();
                        service.execute(() -> handler(socket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }

    public void handler(final Socket socket)  {
        try(
            socket;
            final var in = new BufferedInputStream(socket.getInputStream());
            final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            Request request = new Request(in, out);
            request.requestParser();
            var methodMap = handlers.get(request.getMethod());
            if(methodMap == null){
                notFound(out);
                return;
            }
            var handler = methodMap.get(request.getPath());
            if(handler == null){
                notFound(out);
                return;
            }
            handler.handle(request, out);

        } catch(IOException e){
            System.out.println(e.getMessage());
        }

    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
    public void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}