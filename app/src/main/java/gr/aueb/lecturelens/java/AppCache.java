package gr.aueb.lecturelens.java;

import java.util.ArrayList;
import java.util.List;

public class AppCache {
    private static AppCache instance;

    public List<Course> cachedCourses = new ArrayList<>();
    public List<Professor> cachedProfessors = new ArrayList<>();
    public boolean loaded = false;

    private AppCache() {}

    public static AppCache getInstance() {
        if (instance == null) instance = new AppCache();
        return instance;
    }
}
