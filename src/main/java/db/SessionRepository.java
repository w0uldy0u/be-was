package db;

import model.User;

import java.sql.*;

public class SessionRepository {

    public static void addSession(String sid, String userId) {
        String sql = "MERGE INTO sessions (sid, user_id) KEY(sid) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sid);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add session", e);
        }
    }

    public static User findUserBySid(String sid) {
        if (sid == null) return null;

        String sql = """
            SELECT u.user_id, u.password, u.name, u.email, u.profile_image
            FROM users u 
            INNER JOIN sessions s ON u.user_id = s.user_id 
            WHERE s.sid = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("profile_image")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by session", e);
        }
        return null;
    }

    public static void logout(String sid) {
        String sql = "DELETE FROM sessions WHERE sid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to logout", e);
        }
    }
}
