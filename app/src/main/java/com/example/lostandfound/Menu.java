package com.example.lostandfound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Menu extends FragmentActivity {

    private Button LostItem;
    private Button FoundItem;
    private Button FeedOfLostItems;
    private Button FeedOfFoundItems;
    private Button MyPosts;
    private Button updPassword;
    private Button logOut;

    FirebaseAuth fAuth;
    FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        LostItem = (Button) findViewById(R.id.postForLostItem_btn);
        FoundItem = (Button) findViewById(R.id.postForFoundItem_btn);
        FeedOfLostItems = (Button) findViewById(R.id.checkFeedLostItems_btn);
        FeedOfFoundItems = (Button) findViewById(R.id.checkFeedFoundItems_btn);
        MyPosts = (Button) findViewById(R.id.myPosts_btn);
        updPassword = (Button) findViewById(R.id.updatePass_btn);
        logOut = (Button) findViewById(R.id.logout_btn);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        LostItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, postForLostItem.class));
            }
        });

        FoundItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, postForFoundItem.class));
            }
        });

        FeedOfLostItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, CheckFeedOfLostItems.class));
            }
        });

        FeedOfFoundItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, CheckFeedOfFoundItems.class));
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, MyPosts.class));
            }
        });

        updPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText updatePassword = new EditText(v.getContext());

                final AlertDialog.Builder passwordUpdateDialog = new AlertDialog.Builder(v.getContext());
                passwordUpdateDialog.setTitle("Reset Password?");
                passwordUpdateDialog.setMessage("Enter the new password > 6 characters long");
                passwordUpdateDialog.setView(updatePassword);

                passwordUpdateDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = updatePassword.getText().toString();
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Menu.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Menu.this, "Password Reset Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordUpdateDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordUpdateDialog.create().show();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new Fragment())
                    .commitNow();
        }
    }

    public void logout()
    {
        fAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
    }
}