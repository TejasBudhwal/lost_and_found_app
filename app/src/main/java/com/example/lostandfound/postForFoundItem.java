package com.example.lostandfound;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class postForFoundItem extends AppCompatActivity {

    TextView name, phone;
    FirebaseAuth fAuth;
    String userID;
    Button selectImage, post;
    ImageView selected_image;
    EditText location, message;
    ProgressDialog progressDialog;

    Uri imageUri;
    String url;
    public static final int PICK_IMAGE=100;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    StorageReference storageReference;

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
                        selected_image.setImageURI(imageUri);
                    }
                    else
                    {
                        Toast.makeText(postForFoundItem.this, "No image selected", Toast.LENGTH_SHORT).show();
                        selected_image.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_for_found_item);

        init();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference().child("Found Item Images/");

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
//                startActivityForResult(intent, PICK_IMAGE);
                activityResultLauncher.launch(intent);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri == null)
                {
                    Toast.makeText(postForFoundItem.this, "Select an image", Toast.LENGTH_SHORT).show();
                }
                else if(message.getText().toString().isEmpty())
                {
                    Toast.makeText(postForFoundItem.this, "Enter a valid description", Toast.LENGTH_SHORT).show();
                }
                else if(location.getText().toString().isEmpty())
                {
                    Toast.makeText(postForFoundItem.this, "Enter a valid location", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    uploadPost();
                }
            }
        });

        name = findViewById(R.id.pffi_name);
        phone = findViewById(R.id.pffi_phone);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Log.i("USERID", userID);

        DocumentReference document = FirebaseFirestore.getInstance().collection("users").document(userID);
        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    name.setText(documentSnapshot.getString("fullName"));
                    phone.setText(documentSnapshot.getString("phoneNo"));
                }
                else
                {
                    Toast.makeText(postForFoundItem.this, "Document not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(postForFoundItem.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null)
//        {
//            imageUri = data.getData();
//            selected_image.setImageURI(imageUri);
//        }
//        else
//        {
//            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
//            selected_image.setImageResource(R.drawable.ic_launcher_background);
//        }
//    }

    private void init()
    {
        selectImage = findViewById(R.id.pffi_image_select);
        post = findViewById(R.id.pffi_post);
        selected_image = findViewById(R.id.pffi_image);
        location = findViewById(R.id.pffi_location);
        message = findViewById(R.id.pffi_message);
        progressDialog = new ProgressDialog(this);
    }

    private void uploadPost()
    {
        progressDialog.setTitle("Uploading Post");
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

                            reference = FirebaseDatabase.getInstance().getReference().child("Found Item Posts");

                            String postid = reference.push().getKey();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("postid", postid);
                            map.put("postImage", url);
                            map.put("message", message.getText().toString());
                            map.put("location", location.getText().toString());
                            map.put("publisher", userID);
                            map.put("fullName", name.getText().toString());
                            map.put("phone", phone.getText().toString());

                            progressDialog.dismiss();
                            reference.child(postid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(postForFoundItem.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(postForFoundItem.this, Menu.class));
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(postForFoundItem.this, "Failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(postForFoundItem.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
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