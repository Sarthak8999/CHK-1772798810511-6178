package ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.smartparentcontrol.R;

import java.util.ArrayList;
import java.util.List;

import adapter.StudentRequestsAdapter;
import model.TimeRequest;
import utils.PreferenceManager;

public class StudentRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentRequestsAdapter adapter;
    private PreferenceManager preferenceManager;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_requests);

        preferenceManager = new PreferenceManager(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Request History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentRequestsAdapter();
        recyclerView.setAdapter(adapter);

        loadStudentRequests();
    }

    private void loadStudentRequests() {

        String studentUID = preferenceManager.getUserId();

        if (studentUID == null || studentUID.isEmpty()) {
            Toast.makeText(this, "Student not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(studentUID)
                .child("parentUID")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(StudentRequestsActivity.this,
                                    "Parent not connected",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String parentUID = snapshot.getValue(String.class);

                        if (parentUID != null) {
                            loadRequestsFromParent(parentUID, studentUID);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(StudentRequestsActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRequestsFromParent(String parentUID, String studentUID) {

        databaseRef.child(parentUID)
                .child("requests")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        List<TimeRequest> requests = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            TimeRequest request = child.getValue(TimeRequest.class);

                            if (request != null &&
                                    studentUID.equals(request.getStudentUID())) {

                                request.setId(child.getKey());
                                requests.add(request);
                            }
                        }

                        adapter.setRequestList(requests);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(StudentRequestsActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}