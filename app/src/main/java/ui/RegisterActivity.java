package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    EditText fullNameInput, emailInput, passwordInput, confirmPasswordInput, referralCodeInput;
    TextInputLayout referralCodeLayout;
    Button registerButton;
    TextView loginLink;

    String role;

    FirebaseAuth mAuth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        role = getIntent().getStringExtra("role");

        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        referralCodeInput = findViewById(R.id.referralCodeInput);
        referralCodeLayout = findViewById(R.id.referralCodeLayout);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        TextView titleText = findViewById(R.id.titleText);

        if (role != null) {
            titleText.setText(role.toUpperCase() + " Registration");

            if (role.equals("student")) {
                referralCodeLayout.setVisibility(View.VISIBLE);
            }
        }

        registerButton.setOnClickListener(v -> performRegistration());

        loginLink.setOnClickListener(v -> finish());
    }


    private void performRegistration() {

        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String referralCode = referralCodeInput.getText().toString().trim().toUpperCase();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("student") && referralCode.isEmpty()) {
            Toast.makeText(this, "Enter Parent Referral Code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

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

                        registerButton.setEnabled(true);
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show();
                    }

                });

    }


    private void registerParent(String uid, String email, String fullName) {

        String referralCode = uid.substring(0, 6).toUpperCase();

        Map<String, Object> user = new HashMap<>();

        user.put("email", email);
        user.put("role", "parent");
        user.put("fullName", fullName);
        user.put("referralCode", referralCode);

        databaseRef.child(uid).setValue(user).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(this,
                        "Registration Success\nReferral Code : " + referralCode,
                        Toast.LENGTH_LONG).show();

                startActivity(new Intent(this, ParentDashboardActivity.class));
                finish();

            } else {

                registerButton.setEnabled(true);
                Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
            }

        });

    }


    private void registerStudent(String uid, String email, String fullName, String referralCode) {

        Query query = databaseRef.orderByChild("referralCode").equalTo(referralCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    String parentUID = snapshot.getChildren().iterator().next().getKey();

                    saveStudent(uid, email, fullName, parentUID);

                } else {

                    registerButton.setEnabled(true);

                    Toast.makeText(RegisterActivity.this,
                            "Invalid Referral Code",
                            Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

                registerButton.setEnabled(true);

                Toast.makeText(RegisterActivity.this,
                        "Database Error",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveStudent(String uid, String email, String fullName, String parentUID) {

        Map<String, Object> student = new HashMap<>();

        student.put("email", email);
        student.put("role", "student");
        student.put("fullName", fullName);
        student.put("parentUID", parentUID);

        databaseRef.child(uid).setValue(student).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                databaseRef.child(parentUID).child("students").child(uid).setValue(true);

                Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, StudentDashboardActivity.class));
                finish();

            } else {

                registerButton.setEnabled(true);
                Toast.makeText(this, "Error saving data", Toast.LENGTH_LONG).show();
            }

        });
    }

}