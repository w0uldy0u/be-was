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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + ", profileImage=" + profileImage + "]";
    }
}
