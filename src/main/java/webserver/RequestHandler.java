package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import model.ParsedHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.parser.HttpParser;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        DataOutputStream dos = null;
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            ParsedHttpRequest parsedHttpRequest = new HttpParser().parse(br);

            logger.debug(parsedHttpRequest.getHeader());

            String path = parsedHttpRequest.getPath();
            if (path.endsWith("/")) path = path + "index.html";

            try {
                File file = new File("src/main/resources/static" + path);
                byte[] body = Files.readAllBytes(file.toPath());
                HttpResponseSender.send200(dos, body, ContentTypes.fromPath(path));
            } catch (java.nio.file.NoSuchFileException e) {
                HttpResponseSender.send404(dos);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (dos != null) {
                try { HttpResponseSender.send500(dos); }
                catch (IOException ignored) {}
            }
        }
    }
}
