package model;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    private String profileImage;

    public User(String userId, String password, String name, String email) {
        this(userId, password, name, email, null);
    }

    public User(String userId, String password, String name, String email, String profileImage) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
    
    public String getProfileImage() {
        return profileImage;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + ", profileImage=" + profileImage + "]";
    }
}
