package db;

import model.Article;
import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, String> sessions = new HashMap<>();
    private static Map<Integer, Article> articles = new HashMap<>();
    private static int currentArticleId = 1;

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static void addSession(String sid, String userId){
        sessions.put(sid, userId);
    }

    public static User findUserBySid(String sid) {
        if (sid == null) return null;

        String userId = sessions.get(sid);
        if (userId == null) return null;

        return users.get(userId);
    }

    public static void logout(String sid){
        sessions.remove(sid);
    }

    public static synchronized void addArticle(String authorId, String content){
        articles.put(currentArticleId, new Article(currentArticleId, authorId, content));
        ++currentArticleId;
    }

    public static Article findArticleById(int articleId){
        return articles.get(articleId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
