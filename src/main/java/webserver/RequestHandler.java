package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import db.Database;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.parser.HttpParser;
import webserver.parser.MultipartParser;


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
            route(parseRequest(in));
        } catch (Exception e) {
            handleServerError(e);
        }
    }

    private void route(ParsedHttpRequest request){
        String path = request.getPath();
        HttpMethod method = request.getMethod();

        try {
            switch (method) {
                case POST:
                    if (path.equals("/create")) {
                        handleRegister(request);
                        return;
                    } else if (path.equals("/login")) {
                        handleLogin(request);
                    } else if (path.equals("/logout")){
                        handleLogout(request);
                    } else if (path.equals("/article")){
                        handleArticleUpload(request);
                    }
                    break;

                case GET:
                    if(path.equals("/main")) {
                        handleMain(request);
                    }
                    else if (path.equals("/")){
                        handleHome(request);
                    }
                    else if (path.equals("/mypage")){
                        handleMypage(request);
                    }
                    else if (path.equals("/article")){
                        handleWrite(request);
                    }
                    else {
                        serveStaticFile(path);
                    }
            }
        }
        catch (java.nio.file.NoSuchFileException e) {
            handleNotFound(e);
        }
        catch (Exception e) {
            handleServerError(e);
        }
    }

    private void handleArticleUpload(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if (currentUser == null) {
            handleUnauthorized();
            return;
        }

        String boundary = MultipartParser.extractBoundary(req.getHeader("content-type"));

        if (boundary == null) {
            handleBadRequest();
            return;
        }

        MultipartParser.MultipartData data = MultipartParser.parse(req.getRawBody(), boundary);

        String content = data.getTextField("content");
        byte[] image = data.getFileField("image");

        if (content == null || content.isBlank()) {
            handleBadRequest();
            return;
        }

        String imagePath = null;
        if (image != null && image.length > 0) {
            imagePath = utils.ImageStore.saveImage(image, "jpg");
        }

        Database.addArticle(currentUser.getUserId(), content, imagePath);

        HttpResponse res = HttpResponse.redirect("/main");
        HttpResponseSender.send(dos, res);
    }

    private void handleWrite(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if(currentUser == null){
            HttpResponse res = HttpResponse.redirect("/login");
            HttpResponseSender.send(dos, res);
            return;
        }
        serveStaticFile("/article");
    }

    private void handleMypage(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if(currentUser == null){
            HttpResponse res = HttpResponse.redirect("/login");
            HttpResponseSender.send(dos, res);
            return;
        }
        serveStaticFile("/mypage");
    }

    private void handleLogout(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        Database.logout(sid);
        String cookie = "SID=; Path=/; Max-Age=0";
        HttpResponse res = HttpResponse.redirect("/").header("Set-Cookie", cookie);
        HttpResponseSender.send(dos, res);
    }

    private void handleHome(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        if(Database.findUserBySid(sid) != null){
            HttpResponse res = HttpResponse.redirect("/main");
            HttpResponseSender.send(dos, res);
            return;
        }
        serveStaticFile("/");
    }

    private void handleMain(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if( currentUser == null){
            HttpResponse res = HttpResponse.redirect("/");
            HttpResponseSender.send(dos, res);
            return;
        }

        String html = TemplateEngine.render("main/index.html", Map.of(
                "username", currentUser.getUserId()
        ));

        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                .contentType("text/html;charset=utf-8")
                .body(html.getBytes(StandardCharsets.UTF_8));

        HttpResponseSender.send(dos, res);
    }

    private void handleLogin(ParsedHttpRequest req) throws IOException {
        Map<String, String> parameters = HttpParser.parseQueryParams(req.getBody());
        String userId = parameters.get("userId");
        String pw = parameters.get("password");
        if(userId == null || pw == null) {
            handleBadRequest();
            return;
        }

        User currentUser = Database.findUserById(userId);
        if(currentUser != null && currentUser.getPassword().equals(pw)){
            logger.debug("Login Success");
            String sid = UUID.randomUUID().toString();

            Database.addSession(sid, userId);

            String cookie = "SID=" + sid + "; Path=/";
            HttpResponse res = HttpResponse.redirect("/main").header("Set-Cookie", cookie);
            HttpResponseSender.send(dos, res);
            return;
        }

        handleUnauthorized();
    }

    private void handleRegister(ParsedHttpRequest req) throws IOException {
        Map<String, String> parameters = HttpParser.parseQueryParams(req.getBody());
        String userId = parameters.get("userId");
        String password = parameters.get("password");
        String name = parameters.get("name");
        String email = parameters.get("email");

        if (userId.isBlank() || password.isBlank() || name.isBlank() || email.isBlank()) {
            handleBadRequest();
            return;
        }

        User newUser = new User(userId, password, name, email);
        Database.addUser(newUser);
        logger.debug(newUser.toString());
        HttpResponse res = HttpResponse.redirect("/");
        HttpResponseSender.send(dos, res);
    }

    private ParsedHttpRequest parseRequest(InputStream in) throws IOException {
        ParsedHttpRequest request = new HttpParser().parse(in);
        logger.debug(request.getPath());
        logger.debug(request.getMethod().toString());
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
        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                                        .contentType(ContentTypes.fromPath(path))
                                        .body(body);
        HttpResponseSender.send(dos, res);
    }

    private void sendError(HttpStatus status, String message) {
        try {
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            HttpResponse res = HttpResponse.of(status)
                    .contentType("text/html;charset=utf-8")
                    .body(body);

            HttpResponseSender.send(dos, res);
        } catch (IOException ignored) {
        }
    }

    private void handleNotFound(Exception e) {
        logger.error("Not Found", e);
        sendError(HttpStatus.NOT_FOUND,"<h1>404 Not Found</h1>");
    }

    private void handleServerError(Exception e) {
        logger.error("Internal Server Error", e);
        sendError(HttpStatus.INTERNAL_SERVER_ERROR, "<h1>500 Internal Server Error</h1>");
    }

    private void handleBadRequest(){
        logger.error("Bad Request");
        sendError(HttpStatus.BAD_REQUEST, "<h1>400 Bad Request</h1>");
    }

    private void handleUnauthorized(){
        logger.error("Unauthorized");
        sendError(HttpStatus.UNAUTHORIZED, "<h1>401 Unauthorized</h1>");
    }
}