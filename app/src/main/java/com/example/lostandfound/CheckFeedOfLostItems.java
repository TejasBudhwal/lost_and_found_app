package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CheckFeedOfLostItems extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference myRef;

    private ArrayList<Posts> postsList;
    private PostAdapter postAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_feed_of_lost_items);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        myRef = FirebaseDatabase.getInstance().getReference();

        postsList = new ArrayList<>();

        ClearAll();

        GetDataFromFirebase();
    }

    private void GetDataFromFirebase() {

        Query query = myRef.child("Lost Item Posts");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ClearAll();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Posts posts = new Posts();
                    posts.setPostImage(snapshot.child("postImage").getValue().toString());
                    posts.setFullName(snapshot.child("fullName").getValue().toString());
                    posts.setPhone(snapshot.child("phone").getValue().toString());
                    posts.setLocation(snapshot.child("location").getValue().toString());
                    posts.setMessage(snapshot.child("message").getValue().toString());

                    postsList.add(posts);
                }

                postAdapter = new PostAdapter(getApplicationContext(), postsList);
                recyclerView.setAdapter(postAdapter);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ClearAll(){
        if(postsList != null)
        {
            postsList.clear();

            if(postAdapter != null){
                postAdapter.notifyDataSetChanged();
            }
        }

        postsList = new ArrayList<>();
    }
}