package gr.aueb.lecturelens.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_FULLNAME = "fullName";
    private static final String KEY_USERNAME = "username";
    // 1. Add new keys for the extra data
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DATE = "createdAt";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSession(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String fullName, String username, String email, String password, String role, String creationDate) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_FULLNAME, fullName);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_DATE, creationDate);
        editor.apply();
    }

    public String getCreationDate(){
        return pref.getString(KEY_DATE, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getFullName() {
        return pref.getString(KEY_FULLNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}