package gr.aueb.lecturelens.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public UserSession(Context context) {
        this.context = context;
        // PRIVATE_mode ensures only this app can access the data
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Save user details
    public void createLoginSession(String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply(); // Saves the data asynchronously in the background
    }

    // Get stored username (returns null if not found)
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear all data (useful for Logout)
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}