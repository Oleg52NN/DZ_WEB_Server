package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.concurrent.*;

public class MyServer {
    static MyServer work = null;
    private final int port;
    static ConcurrentMap<String, HashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

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

                    try {
                        Future<String> namePool = service.submit(handler(in, out));
                    } catch (NullPointerException e) {
                        //System.out.println(e);
                    }


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }

    public Callable<String> handler(BufferedReader in, BufferedOutputStream out) throws IOException {
        Request request = new Request(in, out);
        request.requestParser();
        handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);
        return () -> Thread.currentThread().getName();
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}