package ui;

import android.os.Bundle;
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

import adapter.RequestsAdapter;
import model.TimeRequest;
import utils.PreferenceManager;

public class RequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestsAdapter adapter;
    private PreferenceManager preferenceManager;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        preferenceManager = new PreferenceManager(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Request History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RequestsAdapter(new RequestsAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(TimeRequest request) {
                updateStatus(request, "approved");
            }

            @Override
            public void onReject(TimeRequest request) {
                updateStatus(request, "rejected");
            }
        });

        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {

        String parentUID = preferenceManager.getUserId();

        if (parentUID == null) {
            Toast.makeText(this, "Parent not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(parentUID)
                .child("requests")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        List<TimeRequest> requests = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            TimeRequest request = child.getValue(TimeRequest.class);

                            if (request != null) {
                                request.setId(child.getKey());
                                requests.add(request);
                            }
                        }

                        adapter.setRequestList(requests);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(RequestsActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatus(TimeRequest request, String status) {

        String parentUID = preferenceManager.getUserId();

        databaseRef.child(parentUID)
                .child("requests")
                .child(request.getId())
                .child("status")
                .setValue(status)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}