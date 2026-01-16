package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
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
                    } else if (path.equals("/mypage/update")){
                        handleProfileUpdate(request);
                    } else if (path.equals("/article/like")){
                        handleLike(request);
                    } else if (path.equals("/comment/create")){
                        handleCommentCreate(request);
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
                    else if (path.equals("/comment")){
                        handleComment(request);
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

    private void handleCommentCreate(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if (currentUser == null) {
            handleUnauthorized();
            return;
        }

        Map<String, String> params = HttpParser.parseQueryParams(req.getBody());
        String articleIdStr = params.get("articleId");
        String content = params.get("content");

        if (articleIdStr == null || content == null) {
            handleBadRequest();
            return;
        }

        int articleId = Integer.parseInt(articleIdStr);
        Comment comment = new Comment(0, articleId, currentUser.getUserId(), content);
        Database.addComment(comment);

        HttpResponse res = HttpResponse.redirect("/main?id=" + articleId);
        HttpResponseSender.send(dos, res);
    }

    private void handleProfileUpdate(ParsedHttpRequest req) throws IOException {
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
        
        String name = data.getTextField("name");
        String password = data.getTextField("password");
        byte[] image = data.getFileField("profileImage");
        String deleteImage = data.getTextField("deleteImage");
        
        if(name != null && !name.isBlank()){
             currentUser.setName(name);
        }

        if(password != null && !password.isBlank()){
            currentUser.setPassword(password);
        }

        if ("true".equals(deleteImage)) {
            currentUser.setProfileImage("./img/profile.png");
        } else if (image != null && image.length > 0) {
            String imagePath = utils.ImageStore.saveImage(image, "jpg");
            currentUser.setProfileImage(imagePath);
        }

        Database.updateUser(currentUser);

        HttpResponse res = HttpResponse.redirect("/main");
        HttpResponseSender.send(dos, res);
    }

    private void handleLike(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if (currentUser == null) {
            handleUnauthorized();
            return;
        }

        Map<String, String> params = HttpParser.parseQueryParams(req.getBody());
        String articleIdStr = params.get("articleId");

        if (articleIdStr == null) {
            handleBadRequest();
            return;
        }

        int articleId = Integer.parseInt(articleIdStr);
        Database.increaseLikes(articleId);
        Article article = Database.findArticleById(articleId);

        String jsonResponse = "{\"likes\": " + article.getLikes() + "}";

        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                .contentType("application/json")
                .body(jsonResponse.getBytes(StandardCharsets.UTF_8));
        HttpResponseSender.send(dos, res);
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

        if (image == null || image.length == 0) {
            handleBadRequest();
            return;
        }

        String imagePath = utils.ImageStore.saveImage(image, "jpg");

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
        String html = TemplateEngine.render("article/index.html", Map.of(
            "username", currentUser.getUserId()
        ));

        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                .contentType("text/html;charset=utf-8")
                .body(html.getBytes(StandardCharsets.UTF_8));

        HttpResponseSender.send(dos, res);
    }

    private void handleComment(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if(currentUser == null){
            HttpResponse res = HttpResponse.redirect("/login");
            HttpResponseSender.send(dos, res);
            return;
        }

        Map<String, String> params = req.getQueryParameters();
        String articleId = params.get("articleId");

        String html = TemplateEngine.render("comment/index.html", Map.of(
            "username", currentUser.getUserId(),
            "articleId", articleId != null ? articleId : ""
        ));

        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                .contentType("text/html;charset=utf-8")
                .body(html.getBytes(StandardCharsets.UTF_8));

        HttpResponseSender.send(dos, res);
    }

    private void handleMypage(ParsedHttpRequest req) throws IOException {
        String sid = req.getCookie("SID");
        User currentUser = Database.findUserBySid(sid);

        if(currentUser == null){
            HttpResponse res = HttpResponse.redirect("/login");
            HttpResponseSender.send(dos, res);
            return;
        }
        
        String profileImage = currentUser.getProfileImage();
        if (profileImage == null) {
            profileImage = "./img/profile.png";
        }

        logger.debug(profileImage);

        String html = TemplateEngine.render("mypage/index.html", Map.of(
            "profileImage", profileImage,
            "username", currentUser.getUserId(),
            "nickname", currentUser.getName()
        ));

        HttpResponse res = HttpResponse.of(HttpStatus.OK)
                .contentType("text/html;charset=utf-8")
                .body(html.getBytes(StandardCharsets.UTF_8));
        
        HttpResponseSender.send(dos, res);
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

        Map<String, String> params = req.getQueryParameters();
        String articleIdStr = params.get("id");
        Article article;

        if (articleIdStr != null && !articleIdStr.isEmpty()) {
            article = Database.findArticleById(Integer.parseInt(articleIdStr));
        } else {
            article = Database.findLatestArticle();
        }

        String articleContent = (article != null) ? article.getContent() : "";
        String articleImage = (article != null && article.getImagePath() != null) ? article.getImagePath() : "";
        
        String profileImage = currentUser.getProfileImage();
        if (profileImage == null) {
            profileImage = "";
        }

        int currentId = (article != null) ? article.getId() : 0;
        int prevId = Database.findPreviousArticleId(currentId);
        int nextId = Database.findNextArticleId(currentId);

        String prevArticleLink = (prevId != -1) ? "/main?id=" + prevId : "#";
        String prevArticleClass = (prevId != -1) ? "" : "disabled";
        
        String nextArticleLink = (nextId != -1) ? "/main?id=" + nextId : "#";
        String nextArticleClass = (nextId != -1) ? "" : "disabled";
        
        int articleLikes = (article != null) ? article.getLikes() : 0;
        String articleId = (article != null) ? String.valueOf(article.getId()) : "";

        String authorUsername = "";
        String authorProfileImage = "";
        StringBuilder commentsHtml = new StringBuilder();
        int commentCount = 0;
        if (article != null) {
            authorUsername = article.getAuthorId();
            User author = Database.findUserById(authorUsername);
            if (author != null && author.getProfileImage() != null) {
                authorProfileImage = author.getProfileImage();
            }
            
            java.util.Collection<Comment> comments = Database.findAllCommentsByArticleId(article.getId());
            commentCount = comments.size();
            int index = 0;
            for (Comment comment : comments) {
                String hiddenClass = (index >= 3) ? " hidden" : "";
                String commentAuthorProfileImage = "./img/profile.png";
                User commentAuthor = Database.findUserById(comment.getAuthorId());
                if (commentAuthor != null && commentAuthor.getProfileImage() != null) {
                    commentAuthorProfileImage = commentAuthor.getProfileImage();
                }

                commentsHtml.append("<li class='comment__item").append(hiddenClass).append("'>")
                            .append("<div class='comment__item__user'>")
                            .append("<img class='comment__item__user__img' src='").append(commentAuthorProfileImage).append("' />")
                            .append("<p class='comment__item__user__nickname'>").append(comment.getAuthorId()).append("</p>")
                            .append("</div>")
                            .append("<p class='comment__item__article'>").append(comment.getContent()).append("</p>")
                            .append("</li>");
                index++;
            }
        }
        
        String showAllButtonDisplay = (commentCount <= 3) ? "display: none;" : "";

        Map<String, String> model = new HashMap<>();
        model.put("username", currentUser.getUserId());
        model.put("article_content", articleContent);
        model.put("article_image", articleImage);
        model.put("profileImage", profileImage);
        model.put("author_username", authorUsername);
        model.put("author_profile_image", authorProfileImage);
        model.put("prevArticleLink", prevArticleLink);
        model.put("prevArticleClass", prevArticleClass);
        model.put("nextArticleLink", nextArticleLink);
        model.put("nextArticleClass", nextArticleClass);
        model.put("article_id", articleId);
        model.put("article_likes", String.valueOf(articleLikes));
        model.put("comments", commentsHtml.toString());
        model.put("comment_count", String.valueOf(commentCount));
        model.put("show_all_display", showAllButtonDisplay);

        String html = TemplateEngine.render("main/index.html", model);
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
        
        if (currentUser == null) {
            handleNotFound(new RuntimeException("User not found"));
            return;
        }

        if(currentUser.getPassword().equals(pw)){
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

        if (userId.length() < 4 || password.length() < 4 || name.length() < 4) {
            handleBadRequest();
            return;
        }

        User existingUserById = Database.findUserById(userId);
        User existingUserByName = Database.findUserByName(name);

        if (existingUserById != null || existingUserByName != null) {
            handleConflict();
            return;
        }

        User newUser = new User(userId, password, name, email);
        if(newUser.getProfileImage() == null || newUser.getProfileImage().isEmpty()){
            newUser.setProfileImage("./img/profile.png");
        }
        Database.addUser(newUser);
        logger.debug(newUser.toString());
        HttpResponse res = HttpResponse.redirect("/login");
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

    private void handleConflict(){
        logger.error("Conflict");
        sendError(HttpStatus.CONFLICT, "<h1>409 Conflict</h1>");
    }
}