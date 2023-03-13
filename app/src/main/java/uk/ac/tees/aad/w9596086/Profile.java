package uk.ac.tees.aad.w9596086;


import android.Manifest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    public CircleImageView profileImage;
    public TextView profileName, profileEmail;
    public Button updateImageButton;
    public Button updateNameButton;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    FirebaseUser user;
    StorageReference storageReference;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        updateImageButton = findViewById(R.id.update_image_button);
        updateNameButton = findViewById(R.id.update_name_button);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();
        loadImage();

        if(fAuth.getCurrentUser()!=null) {
            profileName.setText(user.getDisplayName());
            profileEmail.setText(user.getEmail());
        }

        updateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for permission to access gallery
                if (ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Profile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
                } else {
                    // Launch gallery intent
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE);
                }
            }
        });

        updateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                // TODO: Implement name update functionality
//                Toast.makeText(ProfileActivity.this, "Name update functionality coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Launch gallery intent
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            } else {
                Toast.makeText(this, "Permission denied. Unable to update profile picture.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Get selected image URI
            Uri selectedImage = data.getData();
            uploadProfileImage(selectedImage);
        }
    }

    private void showDialog(){
        // Create an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// Set the title and message of the dialog box
        builder.setTitle("Enter New User Name");
        builder.setMessage("Please enter your new user name:");

        // Create an EditText object for user input
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        // Set the positive and negative buttons for the dialog box
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked OK button
                String newUserName = input.getText().toString();
                // Use the new user name as needed
                updateName(newUserName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Cancel button
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void updateName(String newName){
        // Get the currently signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Create a UserProfileChangeRequest with the new display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        // Update the user profile
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update successful
                            profileName.setText(newName);
                            Toast.makeText(getApplicationContext(),"Successfully Updated",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),task.getException().getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            // An error occurred
                        }
                    }
                });

    }

    private void uploadProfileImage(Uri imageUri) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage mStorage = FirebaseStorage.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        StorageReference imageRef = mStorage.getReference().child("profile_images/" + userId + ".jpg");
        UploadTask uploadTask = imageRef.putFile(imageUri);
        Toast.makeText(getApplicationContext(),"Updating...", Toast.LENGTH_LONG).show();
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL for the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        // Store the download URL in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("profileImageUrl", downloadUrl.toString());

                        mFirestore.collection("images").document(userId).set(user, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Load the new profile image using Glide
                                        Glide.with(Profile.this)
                                                .load(downloadUrl)
                                                .placeholder(R.drawable.logo)
                                                .circleCrop()
                                                .into(profileImage);
                                        Toast.makeText(getApplicationContext(),"Successfully Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadImage(){
        // Load the user's profile image using Glide
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        mFirestore.collection("images").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists() && documentSnapshot.contains("profileImageUrl")) {
                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl != null) {
                                Glide.with(Profile.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.logo)
                                        .circleCrop()
                                        .into(profileImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}