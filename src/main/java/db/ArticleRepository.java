package db;

import model.Article;

import java.sql.*;

public class ArticleRepository {

    public static void addArticle(String authorId, String content, byte[] image) {
        String sql = "INSERT INTO articles (author_id, content, image) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, authorId);
            pstmt.setString(2, content);
            if (image != null && image.length > 0) {
                pstmt.setBytes(3, image);
            } else {
                pstmt.setNull(3, Types.BLOB);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add article", e);
        }
    }

    public static Article findArticleById(int articleId) {
        String sql = "SELECT id, author_id, content, image FROM articles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, articleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Article(
                        rs.getInt("id"),
                        rs.getString("author_id"),
                        rs.getString("content"),
                        rs.getBytes("image")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find article by id", e);
        }
        return null;
    }
}
