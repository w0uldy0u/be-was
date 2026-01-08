package db;

import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, String> sessions = new HashMap<>();

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

    public static Collection<User> findAll() {
        return users.values();
    }
}
