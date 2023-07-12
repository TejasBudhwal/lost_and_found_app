package com.example.lostandfound;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SignUp extends FragmentActivity {

    public static final String TAG = "TAG";
    public static final int GALLERY_REQUEST_CODE = 105;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;

    EditText UserName, UserRollno, UserEmail, UserPassword, UserCnfpassword, UserPhone;
    ImageView UserImage;
    Button submit, openGallery;
    ProgressDialog loadingBar;
    String userID;
    ProgressDialog progressDialog;

    Uri imageUri;
    String url;
    public static final int PICK_IMAGE=100;

    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseUser user;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int requestCode = activityResult.getResultCode();
                    int resultCode = activityResult.getResultCode();
                    Intent data = activityResult.getData();

                    if(data != null)
                    {
                        imageUri = data.getData();
                        UserImage.setImageURI(imageUri);
                    }
                    else
                    {
                        Toast.makeText(SignUp.this, "No image selected", Toast.LENGTH_SHORT).show();
                        UserImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new Fragment())
                    .commitNow();
        }

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = mAuth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Images/");

        UserEmail = findViewById(R.id.reg_email);
        UserPassword = findViewById(R.id.reg_pass);
        UserCnfpassword = findViewById(R.id.reg_cnfpass);
        UserName = findViewById(R.id.reg_name);
        UserRollno = findViewById(R.id.reg_rollno);
        UserPhone = findViewById(R.id.reg_phone);
        UserImage = findViewById(R.id.reg_image);
        submit = findViewById(R.id.reg_submit);
        openGallery = findViewById(R.id.reg_gallery);

        progressDialog = new ProgressDialog(this);

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
//                startActivityForResult(intent, PICK_IMAGE);
                activityResultLauncher.launch(intent);
            }
        });

        loadingBar = new ProgressDialog(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
    }

    private void Register()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String cnfpassword = UserCnfpassword.getText().toString();
        String fullName = UserName.getText().toString();
        String rollNumber = UserRollno.getText().toString();
        String phoneNo = UserPhone.getText().toString();

        String rollNoPattern = "[0-9]{4}+[a-z]{2}+[0-9]{2}";
        String emailPattern = "[a-z]+_"+rollNumber+"@+iitp.ac.in";
        String phonePattern = "[1-9]{1}+[0-9]{9}";

        if(imageUri == null)
        {
            Toast.makeText(SignUp.this, "Select an image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            uploadPost();
        }
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Email field cannot be empty.", Toast.LENGTH_SHORT).show();
        }
        else if (!email.trim().matches(emailPattern))
        {
            Toast.makeText(this, "Enter valid Institute email ID", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cnfpassword))
        {
            Toast.makeText(this, "Confirm Password cannot be empty.", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cnfpassword))
        {
            Toast.makeText(this,"Password Not matching",Toast.LENGTH_SHORT).show();
        }
        else if(!phoneNo.trim().matches(phonePattern))
        {
            Toast.makeText(this, "Enter valid Phone Number", Toast.LENGTH_SHORT).show();
        }
        else if(!rollNumber.trim().matches(rollNoPattern))
        {
            Toast.makeText(this, "Enter valid Roll Number", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, your account is being created");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(SignUp.this, "User Registered Successfully. Please check you email for verification.", Toast.LENGTH_SHORT).show();

                                    userID = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = fStore.collection("users").document(userID);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fullName", fullName);
                                    user.put("email", email);
                                    user.put("rollNumber", rollNumber);
                                    user.put("phoneNo", phoneNo);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid)
                                        {
                                            Log.d(TAG, "onSuccess: User Profile has been created for "+ userID);
                                        }
                                    });

                                    loadingBar.dismiss();
                                }
                                else
                                {
                                    Toast.makeText(SignUp.this, "Error occured: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SignUp.this, "Error occured: "+ message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }

    private void uploadPost()
    {
        progressDialog.setTitle("Uploading Profile Photo");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (imageUri != null)
        {
            StorageReference sRef = storageReference.child(System.currentTimeMillis()+"."+getExtensionFile(imageUri));
            sRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url = uri.toString();

                            reference = FirebaseDatabase.getInstance().getReference().child("User Profile Photos");

                            String profileImageID = reference.push().getKey();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("profileImageID", profileImageID);
                            map.put("profileImage", url);

                            progressDialog.dismiss();
                            reference.child(profileImageID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SignUp.this, "Profile Image Uploaded", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(SignUp.this, "Failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            });
        }
    }

    public String getExtensionFile(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
