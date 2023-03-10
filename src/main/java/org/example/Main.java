package org.example;

import java.io.BufferedOutputStream;

public class Main {
    public static final int PORT = 9999;

    public static void main(String[] args) {
        MyServer myServer = MyServer.myServerGet(PORT);
        myServer.addHandler("GET", "/index.html", (Request request, BufferedOutputStream out) -> {
            Response response = new Response(request.getMethod(), request.getPath(), out);
            response.selectResponse();
        });
        myServer.addHandler("GET", "/spring.svg", (Request request, BufferedOutputStream out) -> {
            Response response = new Response(request.getMethod(), request.getPath(), out);
            response.selectResponse();
        });
        myServer.addHandler("POST", "/default-get.html", (Request request, BufferedOutputStream out) -> {
            Response response = new Response(request.getMethod(), request.getPath(), out);
            response.selectResponse();
        });
        myServer.serverStart();
    }

}
