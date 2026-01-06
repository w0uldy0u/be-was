package webserver;

import model.HttpResponse;
import model.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class HttpResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public static void send(DataOutputStream dos, HttpResponse res) throws IOException {
        logger.debug(new String(res.getBody(), StandardCharsets.UTF_8));
        writeStatusLine(dos, res.getStatus());
        writeHeaders(dos, res);
        writeBody(dos, res.getBody());
    }

    private static void writeStatusLine(DataOutputStream dos, HttpStatus status) throws IOException {
        dos.writeBytes(
                "HTTP/1.1 "
                        + status.code()
                        + " "
                        + status.reason()
                        + "\r\n"
        );
    }

    private static void writeHeaders(DataOutputStream dos, HttpResponse res) throws IOException {
        dos.writeBytes("Content-Type: " + res.getContentType() + "\r\n");

        byte[] body = res.getBody();
        dos.writeBytes("Content-Length: " + body.length + "\r\n");

        for (Map.Entry<String, String> entry : res.getHeaders().entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

        dos.writeBytes("Connection: close\r\n");
        dos.writeBytes("\r\n");
    }

    private static void writeBody(DataOutputStream dos, byte[] body) throws IOException {
        if (body.length > 0) {
            dos.write(body);
        }
        dos.flush();
    }
}