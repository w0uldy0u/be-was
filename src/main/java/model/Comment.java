package model;

public class Comment {
    private final int id;
    private final int articleId;
    private final String authorId;
    private final String content;

    public Comment(int id, int articleId, String authorId, String content) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public int getArticleId() {
        return articleId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }
}
