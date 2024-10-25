package com.example.estudy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ForwardingListeningExecutorService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

public class HomePage extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    ArrayList<HomePageItemModel> list=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ImageButton profilePictureButton = findViewById(R.id.HomeProfilePictureButton);
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("USER_EMAIL");
        String encodedEmail = encodeEmail(userEmail);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(encodeEmail(userEmail));



        ///recycler
        RecyclerView allList=findViewById(R.id.HomeRecyclerList);
        allList.setLayoutManager(new LinearLayoutManager(this));

        list.add(new HomePageItemModel("HM Jubayed", R.drawable.defaultpic, "Enjoying the sunshine!", R.drawable.defaultpic));
        list.add(new HomePageItemModel("B", R.drawable.ic_launcher_background, "Learning Android development!", R.drawable.ic_launcher_foreground));
        list.add(new HomePageItemModel("C", R.drawable.defaultpic, "Just finished a great workout.", R.drawable.ic_launcher_foreground));
        list.add(new HomePageItemModel("D", R.drawable.defaultpic, "Happy Monday everyone!", R.drawable.ic_launcher_foreground));
        list.add(new HomePageItemModel("D", R.drawable.defaultpic, "Happy Birthday !", R.drawable.ic_launcher_foreground));
        list.add(new HomePageItemModel("D", R.drawable.defaultpic, "Hellow everyone!", R.drawable.ic_launcher_foreground));
        list.add(new HomePageItemModel("D", R.drawable.defaultpic, "Welcome!", R.drawable.ic_launcher_foreground));

        HomeItemRecyclerAdapter adapter=new HomeItemRecyclerAdapter(this,list);
        allList.setAdapter(adapter);


        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this,"Profile",Toast.LENGTH_SHORT).show();
                Intent profileIntent = new Intent(HomePage.this, OwnProfile.class);
                profileIntent.putExtra("USER_EMAIL", userEmail); // Pass user email to profile
                startActivity(profileIntent);
            }
        });
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ImageLoader imageLoader = new ImageLoader();
                String profilePicUrl = dataSnapshot.child(encodedEmail).child("RegistrationPageInformation").child("profilePictureUrl").getValue(String.class);
                ImageView profileImageView = findViewById(R.id.HomeProfilePictureButton);
                imageLoader.loadImageIntoImageView(profilePicUrl, profileImageView, 100, 120, R.drawable.defaultpic);
                if (profilePicUrl != null) {
                    ImageView imageView = findViewById(R.id.HomeProfilePictureButton);
                    Picasso.get().load(profilePicUrl).placeholder(R.drawable.defaultpic).into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("FirebaseError", "Error retrieving image URL", databaseError.toException());
            }
        });

        ImageButton notificationButton = findViewById(R.id.HomeNotificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Notification.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });

        ImageButton HomeHomeButton = findViewById(R.id.HomeHomeButton);
        HomeHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, HomePage.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });

        ImageButton HomeMyMentorsButton = findViewById(R.id.HomeMyMentorsButton);
        HomeMyMentorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MentorList.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });

        ImageButton HomeMyPostButton = findViewById(R.id.HomeMyPostButton);
        HomeMyPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, PostPage.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });

        ImageButton HomeSearchBar = findViewById(R.id.HomeSearchBar);
        HomeSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SearchPage.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });
    }
    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

}