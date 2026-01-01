package webserver;

import java.io.DataOutputStream;
import java.io.IOException;

public class HttpResponseSender {
    public static void send200(DataOutputStream dos, byte[] body, String contentType) throws IOException {
        sendResponse(dos, 200, "OK", body, contentType);
    }

    public static void send404(DataOutputStream dos) throws IOException {
        byte[] body = "<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8);
        sendResponse(dos, 404, "Not Found", body, "text/html;charset=utf-8");
    }

    public static void send500(DataOutputStream dos) throws IOException {
        byte[] body = "<h1>500 Internal Server Error</h1>".getBytes(StandardCharsets.UTF_8);
        sendResponse(dos, 500, "Internal Server Error", body, "text/html;charset=utf-8");
    }

    private static void sendResponse(
            DataOutputStream dos,
            int statusCode,
            String statusMessage,
            byte[] body,
            String contentType
    ) throws IOException {
        writeStatusLine(dos, statusCode, statusMessage);
        writeHeaders(dos, body.length, contentType);
        writeBody(dos, body);
    }

    private static void writeStatusLine(DataOutputStream dos, int code, String msg) throws IOException {
        dos.writeBytes("HTTP/1.1 " + code + " " + msg + "\r\n");
    }

    private static void writeHeaders(DataOutputStream dos, int length, String contentType) throws IOException {
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        dos.writeBytes("Content-Length: " + length + "\r\n");
        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
    }

    private static void writeBody(DataOutputStream dos, byte[] body) throws IOException {
        dos.write(body);
        dos.flush();
    }
}