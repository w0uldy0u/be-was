package db;

import model.Article;

import java.sql.*;

public class ArticleRepository {

    public static void addArticle(String authorId, String content, String imagePath) {
        String sql = "INSERT INTO articles (author_id, content, image) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, authorId);
            pstmt.setString(2, content);
            pstmt.setString(3, imagePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add article", e);
        }
    }

    public static Article findArticleById(int articleId) {
        String sql = "SELECT id, author_id, content, image, likes FROM articles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, articleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Article(
                        rs.getInt("id"),
                        rs.getString("author_id"),
                        rs.getString("content"),
                        rs.getString("image"),
                        rs.getInt("likes")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find article by id", e);
        }
        return null;
    }
    public static Article findLatestArticle() {
        String sql = "SELECT id, author_id, content, image, likes FROM articles ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new Article(
                        rs.getInt("id"),
                        rs.getString("author_id"),
                        rs.getString("content"),
                        rs.getString("image"),
                        rs.getInt("likes")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find latest article", e);
        }
        return null;
    }

    public static int findPreviousArticleId(int currentId) {
        String sql = "SELECT id FROM articles WHERE id < ? ORDER BY id DESC LIMIT 1";
        return getArticleId(currentId, sql);
    }

    public static int findNextArticleId(int currentId) {
        String sql = "SELECT id FROM articles WHERE id > ? ORDER BY id ASC LIMIT 1";
        return getArticleId(currentId, sql);
    }

    private static int getArticleId(int currentId, String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find article id", e);
        }
        return -1;
    }

    public static void increaseLikes(int articleId) {
        String sql = "UPDATE articles SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increase likes", e);
        }
    }
}
