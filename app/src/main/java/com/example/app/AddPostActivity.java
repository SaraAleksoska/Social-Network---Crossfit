package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDb;
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    Uri image_uri = null;

    ProgressDialog progressDialog;

    String name, email, uid, dp;

    String editTitle, editDescription, editImage;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        actionBar.setSubtitle(email);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);

        Intent intent = getIntent();
        String isUpdateKey=""+intent.getStringExtra("key");
        String editPostId=""+intent.getStringExtra("editPostId");

        if(isUpdateKey.equals("editPost"))
        {
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }
        else
        {
            actionBar.setTitle("Add New Post");
            uploadBtn.setText("Upload");

        }

        userDb = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDb.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = "" +ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if(TextUtils.isEmpty(title))
                {
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(description))
                {
                    Toast.makeText(AddPostActivity.this,"Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editPost"))
                {
                    beginUpdate(title, description, editPostId);
                }
                else
                {
                    uploadData(title,description);
                }

            }
        });

    }

    private void beginUpdate(String title, String description, String editPostId) {
        progressDialog.setMessage("Updating Post...");
        progressDialog.show();

        if(!editImage.equals("noImage")){
            updateWasWithImage(title,description,editPostId);
        }
        else if(imageIv.getDrawable() != null)
        {
            updateWithNowImage(title,description,editPostId);
        }
        else{
            updateWithoutImage(title,description,editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");

        DatabaseReference refData = FirebaseDatabase.getInstance().getReference("Posts");
        refData.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWasWithImage(String title, String description, String editPostId) {
        StorageReference mPicRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/"+"post_"+timeStamp;

                Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful())
                        {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);

                            DatabaseReference refData = FirebaseDatabase.getInstance().getReference("Posts");
                            refData.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(String title, String description, String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful())
                {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid", uid);
                    hashMap.put("uName",name);
                    hashMap.put("uEmail", email);
                    hashMap.put("uDp",dp);
                    hashMap.put("pTitle",title);
                    hashMap.put("pDescr", description);
                    hashMap.put("pImage", downloadUri);

                    DatabaseReference refData = FirebaseDatabase.getInstance().getReference("Posts");
                    refData.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    editTitle=""+ds.child("pTitle").getValue();
                    editDescription=""+ds.child("pDescription").getValue();
                    editImage=""+ds.child("pImage").getValue();

                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);
                    if(!editImage.equals("noImage"))
                    {
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String title, String description) {
        progressDialog.setMessage("Publishing a post...");
        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        if(imageIv.getDrawable() != null){
            Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime",timeStamp);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this,"Post published",Toast.LENGTH_SHORT).show();
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_uri = null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{

            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime",timeStamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this,"Post published",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
        String[] option = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else
                    {
                        pickFromCamera();
                    }
                }
                if(i == 1){
                    if(!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }
                    else
                    {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();

    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result2=  ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result1 && result2;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            email = user.getEmail();
            uid = user.getUid();

        }
        else
        {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_target).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if( cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage both permissions are neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted)
                    {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, "Storage permissions are neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                imageIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                imageIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}