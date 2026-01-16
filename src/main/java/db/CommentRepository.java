package db;

import model.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentRepository {

    public static void addComment(Comment comment) {
        String sql = "INSERT INTO comments (article_id, author_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getArticleId());
            pstmt.setString(2, comment.getAuthorId());
            pstmt.setString(3, comment.getContent());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add comment", e);
        }
    }

    public static Collection<Comment> findAllByArticleId(int articleId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, article_id, author_id, content FROM comments WHERE article_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, articleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                        rs.getInt("id"),
                        rs.getInt("article_id"),
                        rs.getString("author_id"),
                        rs.getString("content")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments by article id", e);
        }
        return comments;
    }
}
