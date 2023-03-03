package org.example;

public class Main {
    public static final int PORT = 9999;

    public static void main(String[] args) {
        MyServer myServer = MyServer.myServerGet(PORT);
        myServer.serverStart();
    }

}
