package ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");
        preferenceManager = new PreferenceManager(this);

        // Initialize views
        initializeViews();

        // Setup click listeners
        loginButton.setOnClickListener(v -> performLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
        });
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Disable button and show progress
        loginButton.setEnabled(false);
        progressDialog.show();

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(user.getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        loginButton.setEnabled(true);

                        String errorMessage = "Login Failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null) {
                                if (error.contains("no user record")) {
                                    errorMessage = "Account not found";
                                } else if (error.contains("password is invalid")) {
                                    errorMessage = "Incorrect password";
                                } else if (error.contains("network")) {
                                    errorMessage = "Network error";
                                } else {
                                    errorMessage = error;
                                }
                            }
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void fetchUserData(String uid) {
        databaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();

                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (role != null) {
                        if (role.equals("parent")) {
                            String referralCode = snapshot.child("referralCode").getValue(String.class);
                            preferenceManager.saveParentData(uid, fullName, email, referralCode);
                        } else {
                            String parentUID = snapshot.child("parentUID").getValue(String.class);
                            preferenceManager.saveStudentData(uid, fullName, email, parentUID);
                        }

                        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard(role);
                    }
                } else {
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("parent".equals(role)) {
            intent = new Intent(this, ParentDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
