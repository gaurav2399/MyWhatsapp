package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    FloatingActionButton addStory;

    //layout
    CircleImageView mDisplayImage;
    TextView mName,mStatus;
    Button mStatusChange,mImageBtn;

    //Storage Firebase
    private StorageReference mImageStorage;

    //Progress Dialog
    ProgressDialog mProgresssDialog;
    byte[] thumb_bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = findViewById(R.id.settings_image);
        mName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        mStatusChange = findViewById(R.id.settings_status_btn);
        mImageBtn = findViewById(R.id.settings_image_btn);
        //addStory=findViewById(R.id.add_story);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        String uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("enter in adta settings ", "yes");
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                Log.e("settigs status",status);
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                String settingsStatus = dataSnapshot.child("online").getValue().toString();
                Log.e("reference",dataSnapshot.child("online").getRef().toString());
                Log.e("now settings satus",settingsStatus);
                if (!image.equals("default")) {

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image1).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image1).into(mDisplayImage);
                        }
                    });
                }

                mName.setText(name);
                mStatus.setText(status);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("some error ocured", "yes");
            }
        });
        mStatusChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = mStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crop.pickImage(SettingsActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("on activity result","odne");
        if (resultCode==RESULT_OK){
            if (requestCode==Crop.REQUEST_PICK){
                Uri source_uri=data.getData();

                //THIS IS OF NO USE NOW....

                //image storing to firebase storage
                //store_image(source_uri);

                Uri destination_uri=Uri.fromFile(new File(getCacheDir(),"cropped"));
                Crop.of(source_uri,destination_uri).asSquare().start(this);
                //mDisplayImage.setImageURI(Crop.getOutput(data));
            }
            else if (requestCode==Crop.REQUEST_CROP) {
                //CROPPED IMAGE WE GET HERE ONLY
                //USER PRESS DONE..

                mProgresssDialog=new ProgressDialog(SettingsActivity.this);
                mProgresssDialog.setTitle("Uploading Image...");
                mProgresssDialog.setMessage("Please wait while we upload and process the image.");
                mProgresssDialog.setCanceledOnTouchOutside(false);
                mProgresssDialog.show();
                Uri source_uri=Crop.getOutput(data);

                File thumb_file=new File(source_uri.getPath());
                Bitmap thumb_bitmap = null;

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                } catch (IOException e) {
                    Log.e("Image not compressing","failed");
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_bytes= baos.toByteArray();
                //image storing to firebase storage
                Toast.makeText(this, "First work done", Toast.LENGTH_SHORT).show();
                store_image(source_uri);

            }
            else {
                //IF USER PRESS CANCEL..
                Toast.makeText(this, "An error occurred while setting image", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void store_image(Uri source_uri) {
        Log.e("storing image","done");
        Toast.makeText(this, "Storing image start", Toast.LENGTH_SHORT).show();
        //download_url[0] contain surl of image
        //download_url[1] contain url of thumb
        final String[] download_url = new String[2];

        //-----> for fastening of this section add updation in one time using hashMap.
        //----> it is main you have to change it/doing wrong method.

        final StorageReference filepath=mImageStorage.child("profile_images").child(mCurrentUser.getUid()+ ".jpg");
        final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(mCurrentUser.getUid()+ ".jpg");

        //filepath used to get uri of image
        filepath.putFile(source_uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri ImageUri = task.getResult();
                    download_url[0] =ImageUri.toString();
                    Log.e("00000000000000",download_url[0]);

                    //storing of image url
                    mUserDatabase.child("image").setValue(download_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //thumb_filepath used to get uri of thumb
                            thumb_filepath.putBytes(thumb_bytes).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()){
                                        throw task.getException();
                                    }
                                    return filepath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        download_url[1]=task.getResult().toString();
                                        Log.e("9999999999999999",download_url[1]);

                                        //storing of thumb url
                                        mUserDatabase.child("thumb_image").setValue(download_url[1]).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgresssDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Image Uploaded successfully.",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

    }

}
