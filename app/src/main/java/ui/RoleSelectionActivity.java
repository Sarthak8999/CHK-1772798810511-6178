package ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.studentparent.monitor.ui;

public class RoleSelectionActivity extends AppCompatActivity {

    Button parentButton;
    Button studentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // connect buttons with XML
        parentButton = findViewById(R.id.parentButton);
        studentButton = findViewById(R.id.studentButton);

        // Parent Login Button
        parentButton.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
            intent.putExtra("role", "parent");
            startActivity(intent);
        });

        // Student Login Button
        studentButton.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
            intent.putExtra("role", "student");
            startActivity(intent);
        });
    }
}