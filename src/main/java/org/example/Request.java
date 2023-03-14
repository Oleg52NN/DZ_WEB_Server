package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;


public class Request {
    BufferedReader in;
    BufferedOutputStream out;
    String[] answer = new String[4];

    public Request(BufferedReader in, BufferedOutputStream out) {
        this.in = in;
        this.out = out;
    }

    public String getMethod() {
        return answer[0];
    }

    public String getPath() {
        return answer[1];
    }

    public String getHeadings() {
        return answer[2];
    }

    public String getBody() {
        return answer[3];
    }


    void requestParser() throws IOException {

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        if (parts.length != 3) {
            answer[0] = null;
        } else {
            answer[0] = parts[0];
            if (parts[1].charAt(0) != '/') {
                answer[1] = null;
            } else {
                answer[1] = parts[1];
                String s = readBuffer();
                if (answer[0].equals("GET")) {
                    answer[2] = s;
                } else if (answer[0].equals("POST")) {
                    var mass = s.split("\r\n\r\n");
                    if (mass.length > 1) {
                        answer[2] = mass[0];
                        answer[3] = mass[mass.length - 1];
                    }
                }
            }
        }
    }

    String readBuffer() throws IOException {
        StringBuilder buf = new StringBuilder();
        while (in.ready()) {
            buf.append((char) in.read());
        }
        return buf.toString();
    }

}
