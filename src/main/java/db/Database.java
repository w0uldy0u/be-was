package db;

import model.Article;
import model.User;

import java.util.Collection;

public class Database {

    public static void addUser(User user) {
        UserRepository.addUser(user);
    }

    public static User findUserById(String userId) {
        return UserRepository.findUserById(userId);
    }

    public static void addSession(String sid, String userId) {
        SessionRepository.addSession(sid, userId);
    }

    public static User findUserBySid(String sid) {
        return SessionRepository.findUserBySid(sid);
    }

    public static void logout(String sid) {
        SessionRepository.logout(sid);
    }

    public static synchronized void addArticle(String authorId, String content) {
        ArticleRepository.addArticle(authorId, content);
    }

    public static Article findArticleById(int articleId) {
        return ArticleRepository.findArticleById(articleId);
    }

    public static Collection<User> findAll() {
        return UserRepository.findAll();
    }
}
