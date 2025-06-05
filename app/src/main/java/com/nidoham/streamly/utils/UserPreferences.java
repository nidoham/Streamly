package com.nidoham.streamly.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class UserPreferences {

    private static final String PREFS_NAME = "StreamlyUserPrefs";
    private static final String KEY_INTERESTS = "userInterests";
    private static final String KEY_IS_NEW_USER = "isNewUser";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- User Interests --- 

    public static Set<String> getInterests(Context context) {
        // Return a copy to prevent modification of the original set outside this class
        return new HashSet<>(getPrefs(context).getStringSet(KEY_INTERESTS, new HashSet<>()));
    }

    public static void addInterest(Context context, String interest) {
        SharedPreferences prefs = getPrefs(context);
        Set<String> interests = new HashSet<>(prefs.getStringSet(KEY_INTERESTS, new HashSet<>()));
        interests.add(interest.toLowerCase().trim()); // Store in lowercase and trimmed
        prefs.edit().putStringSet(KEY_INTERESTS, interests).apply();
    }

    public static void setInterests(Context context, Set<String> interests) {
        SharedPreferences prefs = getPrefs(context);
        Set<String> lowerCaseInterests = new HashSet<>();
        for (String interest : interests) {
            lowerCaseInterests.add(interest.toLowerCase().trim());
        }
        prefs.edit().putStringSet(KEY_INTERESTS, lowerCaseInterests).apply();
    }

    public static void clearInterests(Context context) {
        getPrefs(context).edit().remove(KEY_INTERESTS).apply();
    }

    // --- New User Check --- 

    public static boolean isNewUser(Context context) {
        // Default to true (is a new user) if the key doesn't exist
        return getPrefs(context).getBoolean(KEY_IS_NEW_USER, true);
    }

    public static void setNewUser(Context context, boolean isNew) {
        getPrefs(context).edit().putBoolean(KEY_IS_NEW_USER, isNew).apply();
    }
}