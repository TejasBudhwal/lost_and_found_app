package com.example.lostandfound;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application
{
    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null && firebaseUser.isEmailVerified())
        {
            startActivity(new Intent(this, Menu.class));
        }
        else
        {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
