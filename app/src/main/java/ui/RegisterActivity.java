package ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import utils.PreferenceManager;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText fullNameInput, emailInput, passwordInput, confirmPasswordInput, referralCodeInput;
    private TextInputLayout referralCodeLayout;
    private Button registerButton;
    private TextView loginLink;
    private ProgressDialog progressDialog;

    private String role;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");
        preferenceManager = new PreferenceManager(this);

        // Get role from intent
        role = getIntent().getStringExtra("role");

        // Initialize views
        initializeViews();

        // Setup UI based on role
        setupRoleBasedUI();

        // Setup click listeners
        registerButton.setOnClickListener(v -> performRegistration());
        loginLink.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        referralCodeInput = findViewById(R.id.referralCodeInput);
        referralCodeLayout = findViewById(R.id.referralCodeLayout);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
    }

    private void setupRoleBasedUI() {
        TextView titleText = findViewById(R.id.titleText);

        if (role != null) {
            titleText.setText(role.toUpperCase() + " Registration");

            if (role.equals("student")) {
                referralCodeLayout.setVisibility(View.VISIBLE);
            } else {
                referralCodeLayout.setVisibility(View.GONE);
            }
        }
    }

    private void performRegistration() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String referralCode = referralCodeInput.getText().toString().trim().toUpperCase();

        // Validate inputs
        if (!validateInputs(fullName, email, password, confirmPassword, referralCode)) {
            return;
        }

        // Disable button and show progress
        registerButton.setEnabled(false);
        showProgress("Creating account...");

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();

                            if (role.equals("parent")) {
                                registerParent(uid, email, fullName);
                            } else {
                                registerStudent(uid, email, fullName, referralCode);
                            }
                        }
                    } else {
                        hideProgress();
                        registerButton.setEnabled(true);

                        String errorMessage = "Registration Failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null) {
                                if (error.contains("already in use")) {
                                    errorMessage = "Email already registered";
                                } else if (error.contains("network")) {
                                    errorMessage = "Network error. Check connection";
                                } else {
                                    errorMessage = error;
                                }
                            }
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed", task.getException());
                    }
                });
    }

    private boolean validateInputs(String fullName, String email, String password, 
                                   String confirmPassword, String referralCode) {
        // Check empty fields
        if (fullName.isEmpty()) {
            fullNameInput.setError("Name is required");
            fullNameInput.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            fullNameInput.setError("Name must be at least 3 characters");
            fullNameInput.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        // Validate email format
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

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Confirm your password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        // Validate referral code for students
        if (role.equals("student") && referralCode.isEmpty()) {
            referralCodeInput.setError("Referral code is required");
            referralCodeInput.requestFocus();
            Toast.makeText(this, "Enter Parent Referral Code", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (role.equals("student") && referralCode.length() < 6) {
            referralCodeInput.setError("Invalid referral code format");
            referralCodeInput.requestFocus();
            return false;
        }

        return true;
    }

    private void registerParent(String uid, String email, String fullName) {
        showProgress("Saving parent data...");

        // Generate unique referral code from UID
        String referralCode = uid.substring(0, 6).toUpperCase();

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", "parent");
        user.put("fullName", fullName);
        user.put("referralCode", referralCode);
        user.put("registrationDate", System.currentTimeMillis());

        databaseRef.child(uid).setValue(user).addOnCompleteListener(task -> {
            hideProgress();

            if (task.isSuccessful()) {
                // Save to PreferenceManager
                preferenceManager.saveParentData(uid, fullName, email, referralCode);

                Toast.makeText(this,
                        "Registration Successful!\nYour Referral Code: " + referralCode,
                        Toast.LENGTH_LONG).show();

                Log.d(TAG, "Parent registered successfully: " + uid);

                // Navigate to Parent Dashboard
                startActivity(new Intent(this, ParentDashboardActivity.class));
                finish();

            } else {
                registerButton.setEnabled(true);
                String errorMsg = "Failed to save data";
                if (task.getException() != null) {
                    errorMsg = task.getException().getMessage();
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error", task.getException());

                // Delete the auth account if database save failed
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete();
                }
            }
        });
    }

    private void registerStudent(String uid, String email, String fullName, String referralCode) {
        showProgress("Verifying referral code...");

        Query query = databaseRef.orderByChild("referralCode").equalTo(referralCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Referral code found - get parent UID
                    String parentUID = snapshot.getChildren().iterator().next().getKey();

                    Log.d(TAG, "Valid referral code. Parent UID: " + parentUID);
                    saveStudent(uid, email, fullName, parentUID);

                } else {
                    hideProgress();
                    registerButton.setEnabled(true);

                    Toast.makeText(RegisterActivity.this,
                            "Invalid Referral Code. Please check with your parent.",
                            Toast.LENGTH_LONG).show();

                    Log.w(TAG, "Invalid referral code: " + referralCode);

                    // Delete the auth account if referral code is invalid
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.delete();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                hideProgress();
                registerButton.setEnabled(true);

                Toast.makeText(RegisterActivity.this,
                        "Database Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                Log.e(TAG, "Database error", error.toException());
            }
        });
    }

    private void saveStudent(String uid, String email, String fullName, String parentUID) {
        showProgress("Saving student data...");

        Map<String, Object> student = new HashMap<>();
        student.put("email", email);
        student.put("role", "student");
        student.put("fullName", fullName);
        student.put("parentUID", parentUID);
        student.put("registrationDate", System.currentTimeMillis());

        databaseRef.child(uid).setValue(student).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Link student to parent
                databaseRef.child(parentUID).child("students").child(uid).setValue(true)
                        .addOnCompleteListener(linkTask -> {
                            hideProgress();

                            if (linkTask.isSuccessful()) {
                                // Save to PreferenceManager
                                preferenceManager.saveStudentData(uid, fullName, email, parentUID);

                                Toast.makeText(RegisterActivity.this,
                                        "Registration Successful!",
                                        Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "Student registered successfully: " + uid);

                                // Navigate to Student Dashboard
                                startActivity(new Intent(RegisterActivity.this, StudentDashboardActivity.class));
                                finish();

                            } else {
                                registerButton.setEnabled(true);
                                Toast.makeText(RegisterActivity.this,
                                        "Failed to link with parent",
                                        Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Failed to link student to parent", linkTask.getException());
                            }
                        });

            } else {
                hideProgress();
                registerButton.setEnabled(true);

                String errorMsg = "Error saving student data";
                if (task.getException() != null) {
                    errorMsg = task.getException().getMessage();
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Database error", task.getException());

                // Delete the auth account if database save failed
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete();
                }
            }
        });
    }

    private void showProgress(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
