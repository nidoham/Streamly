package me.nidoham.streamly.database.firebase.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String userId;              // Unique identifier
    private String email;               // User's email
    private String fullName;            // User's full name
    private String profilePictureUrl;   // Profile picture URL
    private List<String> interests;     // User interests for personalization
    private String preferredLanguage;   // Language preference
    private boolean notificationsEnabled; // Whether notifications are enabled
    private long createdAt;           // Account creation timestamp
    private long lastLoginAt;         // Last login timestamp

    // Default Constructor
    public User() {
        this.userId = "";
        this.email = "";
        this.fullName = "";
        this.profilePictureUrl = "";
        this.interests = new ArrayList<>();
        this.preferredLanguage = "en";
        this.notificationsEnabled = true;
        this.createdAt = 0L;
        this.lastLoginAt = 0L;
    }

    // Parameterized Constructor
    public User(final String userId, final String email,
                final String fullName, final String profilePictureUrl) {
        this();
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId != null ? userId : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email != null ? email : "";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName != null ? fullName : "";
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(final String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl != null ? profilePictureUrl : "";
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(final List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "en";
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(final boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final long createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt() {
        this.createdAt = System.currentTimeMillis();
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(final long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setLastLoginAt() {
        this.lastLoginAt = System.currentTimeMillis();
    }

    // Utility methods
    public boolean hasProfilePicture() {
        return profilePictureUrl != null && !profilePictureUrl.trim().isEmpty();
    }

    public boolean hasFullName() {
        return fullName != null && !fullName.trim().isEmpty();
    }

    public String getDisplayName() {
        if (hasFullName()) {
            return fullName;
        } else {
            return "User";
        }
    }

    public void addInterest(final String interest) {
        if (interest != null && !interest.trim().isEmpty() && !interests.contains(interest)) {
            interests.add(interest);
        }
    }

    public void removeInterest(final String interest) {
        interests.remove(interest);
    }

    public boolean hasInterest(final String interest) {
        return interests.contains(interest);
    }

    @Override
    public String toString() {
        return "User{"
                + "userId='" + userId + '\''
                + ", email='" + email + '\''
                + ", fullName='" + fullName + '\''
                + ", profilePictureUrl='" + profilePictureUrl + '\''
                + ", interests=" + interests
                + ", preferredLanguage='" + preferredLanguage + '\''
                + ", notificationsEnabled=" + notificationsEnabled
                + ", createdAt=" + createdAt
                + ", lastLoginAt=" + lastLoginAt
                + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final User user = (User) obj;
        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
