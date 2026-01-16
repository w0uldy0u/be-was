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

    public static User findUserByName(String name) {
        return UserRepository.findUserByName(name);
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

    public static void addArticle(String authorId, String content, String imagePath) {
        ArticleRepository.addArticle(authorId, content, imagePath);
    }

    public static Article findArticleById(int articleId) {
        return ArticleRepository.findArticleById(articleId);
    }

    public static Collection<User> findAll() {
        return UserRepository.findAll();
    }
    public static Article findLatestArticle() {
        return ArticleRepository.findLatestArticle();
    }

    public static void updateProfileImage(String userId, String imagePath) {
        UserRepository.updateProfileImage(userId, imagePath);
    }

    public static int findPreviousArticleId(int currentId) {
        return ArticleRepository.findPreviousArticleId(currentId);
    }

    public static int findNextArticleId(int currentId) {
        return ArticleRepository.findNextArticleId(currentId);
    }

    public static void increaseLikes(int articleId) {
        ArticleRepository.increaseLikes(articleId);
    }

    public static void updateUser(User user) {
        UserRepository.updateUser(user);
    }
}
