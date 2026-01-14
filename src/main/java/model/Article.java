package model;

public class Article {
    private final int id;
    private final String authorId;
    private final String content;
    private final byte[] image;

    public Article(int id, String authorId, String content, byte[] image) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public byte[] getImage() {
        return image;
    }
}