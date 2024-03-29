package com.example.fyp2secondattempt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ListView mGroupListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGroupListView = findViewById(R.id.groupListView);

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to login activity or handle accordingly
            // Example: startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return;
        }

        // Display groups the user is a part of
        displayUserGroups(currentUser.getUid());
    }

    public void createGroup(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Create a unique key for the group
            String groupId = mDatabase.child("groups").push().getKey();

            // Add the group to the database
            mDatabase.child("groups").child(groupId).child("members").child(currentUser.getUid()).setValue(true)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void displayUserGroups(final String userId) {
        final ArrayList<String> groupNames = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        mGroupListView.setAdapter(adapter);

        // Retrieve groups the user is a part of from the database
        mDatabase.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.child("members").hasChild(userId)) {
                        String groupName = groupSnapshot.getKey();
                        groupNames.add(groupName);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadGroups:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load groups.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
