package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.ParsedHttpRequest;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.parser.HttpParser;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private DataOutputStream dos;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            route(parseRequest(br));
        } catch (Exception e) {
            handleServerError(e);
        }
    }

    private void route(ParsedHttpRequest request){
        String path = request.getPath();

        try {
            if (path.startsWith("/create")) {
                handleRegister(request);
            } else {
                serveStaticFile(path);
            }
        }
        catch (java.nio.file.NoSuchFileException e) {
            handleNotFound();
        }
        catch (Exception e) {
            handleServerError(e);
        }
    }

    private void handleRegister(ParsedHttpRequest req) throws IOException {
        Map<String, String> queryParams = req.getQueryParameters();
        User newUser = new User(queryParams.get("userId"), queryParams.get("password"), queryParams.get("name"), queryParams.get("email"));
        logger.debug(newUser.toString());
        HttpResponseSender.send303(dos, "/");
    }

    private ParsedHttpRequest parseRequest(BufferedReader br) throws IOException {
        ParsedHttpRequest request = new HttpParser().parse(br);
        logger.debug(request.getHeader());
        return request;
    }

    private String normalizePath(String path) {
        if(path.matches(".*\\.[^./\\\\]{1,4}$")){
            return path;
        }
        return path + "/index.html";
    }

    private void serveStaticFile(String path) throws IOException {
        path = normalizePath(path);
        File file = new File("src/main/resources/static" + path);
        byte[] body = Files.readAllBytes(file.toPath());
        HttpResponseSender.send200(dos, body, ContentTypes.fromPath(path));
    }

    private void handleNotFound() {
        try {
            HttpResponseSender.send404(dos);
        } catch (IOException ignored) {
        }
    }

    private void handleServerError(Exception e) {
        logger.error("Internal Server Error", e);
        try {
            HttpResponseSender.send500(dos);
        } catch (IOException ignored) {
        }
    }
}