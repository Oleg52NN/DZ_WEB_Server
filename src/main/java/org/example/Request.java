package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    final List<String> acceptableMethods = Arrays.asList("GET", "POST");
    BufferedInputStream in;
    BufferedOutputStream out;
    private String method;
    private String path;
    private final StringBuilder query = new StringBuilder();
   // private String[] body;
    private final Map<String, String> headingsMap = new HashMap<>();
    private List<NameValuePair> parameters;

    public List<NameValuePair> getBody() {
        return body;
    }

    private List<NameValuePair> body;

    public Request(BufferedInputStream in, BufferedOutputStream out) {
        this.in = in;
        this.out = out;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeadings() {
        return headingsMap;
    }



    public List<NameValuePair> getQueryParameters() {
        return parameters;
    }

    public void requestParser() throws IOException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);
        byte[] separatorOne = {'\r', '\n'};
        byte[] separatorTwo = {'\r', '\n', '\r', '\n'};
        String headings;
        int requestLineEnd = indexStringByte(buffer, separatorOne, 0, read);
        if (requestLineEnd == -1) {
            method = null;
        } else {
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                method = null;
            } else {
                if (!acceptableMethods.contains(requestLine[0])) {
                    method = null;
                } else {
                    method = requestLine[0];
                }
            }
            if (requestLine[1].charAt(0) != '/') {
                path = null;
            } else {
                if (requestLine[1].contains("?")) {
                    StringBuilder s = new StringBuilder();
                    int i = 0;
                    while (requestLine[1].charAt(i) != '?') {
                        s.append(requestLine[1].charAt(i++));
                    }
                    for (int j = i + 1; j < requestLine[1].length(); j++) {
                        query.append(requestLine[1].charAt(j));
                    }
                    requestLine[1] = s.toString();
                }
                path = requestLine[1];
                queryParameters(query.toString());
            }
        }
        int headingsLineEnd = indexStringByte(buffer, separatorTwo, requestLineEnd + separatorOne.length, read);
        if (headingsLineEnd == -1) {
            headings = null;
        } else {
            final var headingsLine = new String(Arrays.copyOfRange(buffer, requestLineEnd + separatorOne.length, headingsLineEnd)).split("\r\n");
            for (int i = 0; i < headingsLine.length; i++) {
                System.out.println(headingsLine[i]);
                var s = headingsLine[i].split(": ");
                headingsMap.put(s[0] + ": ", s[1]);
            }
        }
        if (method.equals("POST")) {

            if (headingsMap.containsKey("Content-Length: ")) {
                String cLength = headingsMap.get("Content-Length: ");
                int bodYes = Integer.parseInt(cLength);
                if (bodYes == 0) {
                    body = null;
                } else {
                    String line = new String(Arrays.copyOfRange(buffer, headingsLineEnd + separatorTwo.length, headingsLineEnd + separatorTwo.length + bodYes));
                    System.out.println("_____________________________________________");
                    if (headingsMap.containsKey("Content-Type: ")) {
                        if ((headingsMap.get("Content-Type: ").equals("application/x-www-form-urlencoded"))) {
                            getPostParams(line);
                        }
                    }
                    List<String> st = getPostParam("value");
                    for (String s: st
                         ) {
                        System.out.println(s);
                    }

                }
            }

        }

    }

    public void queryParameters(String name) {
        this.parameters = URLEncodedUtils.parse(name, StandardCharsets.UTF_8);
        System.out.println(parameters);
        String[] s = getQueryParam("value");
        for (String value : s) {
            System.out.println(value);
        }
    }

    String[] getQueryParam(String name) {
        String[] parameter = new String[parameters.size()];
        int i = 0;
        for (NameValuePair s : parameters
        ) {
            if (s.getName().equals(name)) {
                parameter[i++] = s.getValue();
            }
        }
        return parameter;
    }

    List<String> getPostParam(String line) {
        List<String> param = new ArrayList<String>();
        int i = 0;
        for (NameValuePair s: body
             ) {
            if(s.toString().contains(line)){
                param.add(s.toString().split("=")[1]);
            }
        }
        return param;
    }

    void getPostParams(String line) {
        body = URLEncodedUtils.parse(line, StandardCharsets.UTF_8);
        System.out.println(body);
    }

    public int indexStringByte(byte[] stringByte, byte[] subStringByte, int start, int end) {
        label:
        for (int i = start; i < end - subStringByte.length + 1; i++) {
            for (int j = 0; j < subStringByte.length; j++) {
                if (stringByte[i + j] != subStringByte[j]) {
                    continue label;
                }
            }
            return i;
        }
        return -1;
    }
}
