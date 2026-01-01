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

            ParsedHttpRequest request = parseRequest(br);
            serveStaticFile(dos, request.getPath());
        } catch (java.nio.file.NoSuchFileException e) {
            handleNotFound();
        } catch (Exception e) {
            handleServerError(e);
        }
    }

    private ParsedHttpRequest parseRequest(BufferedReader br) throws IOException {
        ParsedHttpRequest request = new HttpParser().parse(br);
        logger.debug(request.getHeader());
        return request;
    }

    private String normalizePath(String path) {
        if (path.endsWith("/")) {
            return path + "index.html";
        }
        return path;
    }

    private void serveStaticFile(DataOutputStream dos, String path) throws IOException {
        path = normalizePath(path);
        File file = new File("src/main/resources/static" + path);
        byte[] body = Files.readAllBytes(file.toPath());
        HttpResponseSender.send200(dos, body, ContentTypes.fromPath(path));
    }

    private void handleNotFound() {
        try {
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            HttpResponseSender.send404(dos);
        } catch (IOException ignored) {
        }
    }

    private void handleServerError(Exception e) {
        logger.error("Internal Server Error", e);
        try {
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            HttpResponseSender.send500(dos);
        } catch (IOException ignored) {
        }
    }
}