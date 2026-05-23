package gr.aueb.lecturelens;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        setupReportActions();
    }

    private void setupReportActions() {
        ViewGroup root = findViewById(android.R.id.content);
        findAndSetReportListeners(root);
    }

    private void findAndSetReportListeners(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getId() == R.id.btnIgnore || child.getId() == R.id.btnDelete) {
                child.setOnClickListener(v -> {
                    if (v.getId() == R.id.btnIgnore) {
                        // ignore logic
                    } else if (v.getId() == R.id.btnDelete) {
                        // delete logic
                    }
                });
            } else if (child instanceof ViewGroup) {
                findAndSetReportListeners((ViewGroup) child);
            }
        }
    }
}
