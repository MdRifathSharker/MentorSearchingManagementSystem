package com.example.estudy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostPage extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 100;

    private EditText postInput;
    private ImageButton addImageButton;
    private Button postButton;
    private ImageView imagePreview;
    private RecyclerView postsRecyclerView;
    private StorageReference storageReference;

    private Uri imageUri = null;

    private List<PostModel> postList;
    private PostAdapter postAdapter;

    private String userEmail, userName, userProfileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);

        // Initialize Firebase storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        postInput = findViewById(R.id.post_input);
        addImageButton = findViewById(R.id.add_image_button);
        postButton = findViewById(R.id.post_button);
        imagePreview = findViewById(R.id.image_preview);
        postsRecyclerView = findViewById(R.id.posts_recyclerview);

        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(postAdapter);

        // Set up button click listeners
        addImageButton.setOnClickListener(v -> openGallery());
        postButton.setOnClickListener(v -> {
            String postText = postInput.getText().toString().trim();
            if (!postText.isEmpty() || imageUri != null) {
                retrieveUserNameAndPost(postText);
            } else {
                Toast.makeText(PostPage.this, "Enter text or select an image", Toast.LENGTH_SHORT).show();
            }
        });

        loadPostsFromFirebase();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

    private void retrieveUserNameAndPost(String postText) {
        if (userEmail != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(encodeEmail(userEmail))
                    .child("RegistrationPageInformation");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userName = dataSnapshot.child("name").getValue(String.class);
                        userProfileUrl = dataSnapshot.child("profilePictureUrl").getValue(String.class);
                        if (userName != null) {
                            uploadPost(postText);
                        } else {
                            Toast.makeText(PostPage.this, "Failed to retrieve user name", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PostPage.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadPost(String postText) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            String fileName = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/" + fileName);

            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        progressDialog.dismiss();
                        String imageUrl = uri.toString();
                        savePostToDatabase(postText, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(PostPage.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            savePostToDatabase(postText, null);
        }
    }

    private void savePostToDatabase(String postText, String imageUrl) {
        long timestamp = System.currentTimeMillis();
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts").child("Email");
        DatabaseReference postsRef2 = FirebaseDatabase.getInstance().getReference("posts").child("Time");
        String postId = postsRef.push().getKey();
        PostModel post = new PostModel(postId, userProfileUrl, userEmail, userName, postText, imageUrl, timestamp,userEmail);

        if (postId != null) {
            postsRef.child(encodeEmail(userEmail)).child(postId).setValue(post)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PostPage.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                            postInput.setText("");
                            imagePreview.setVisibility(View.GONE);
                            imageUri = null;
                        } else {
                            Toast.makeText(PostPage.this, "Failed to upload post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        if (postId != null) {
            postsRef2.child(postId).setValue(post)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PostPage.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                            postInput.setText("");
                            imagePreview.setVisibility(View.GONE);
                            imageUri = null;
                        } else {
                            Toast.makeText(PostPage.this, "Failed to upload post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadPostsFromFirebase() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child("Email").child(encodeEmail(userEmail));
        postRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel post = snapshot.getValue(PostModel.class);
                    if (post != null) {
                        postList.add(0, post);  // Adds each post to the start of the list
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostPage.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }
}
