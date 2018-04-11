package com.downloader.hola;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.data;

public class settings extends AppCompatActivity implements View.OnClickListener
{
    private DatabaseReference databaseReference;
    private DatabaseReference onlineReference;
    private FirebaseUser currentuser;

    private TextView username;
    private TextView user_status;
    private CircleImageView user_image;

    private Button set_name;
    private Button set_status;
    private Button change_image;
    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;

    static final int gallery_pick=1;

    private StorageReference image_storage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        username=(TextView) findViewById(R.id.settings_username);
        user_status=(TextView) findViewById(R.id.settings_status);
        user_image=(CircleImageView) findViewById(R.id.profile_image);
        set_name=(Button) findViewById(R.id.set_name);
        set_status=(Button) findViewById(R.id.set_status);
        change_image=(Button) findViewById(R.id.change_image);

        set_name.setOnClickListener(this);
        set_status.setOnClickListener(this);
        change_image.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("settings_data", Context.MODE_PRIVATE);

        username.setText(sharedPreferences.getString("Name", ""));
        user_status.setText(sharedPreferences.getString("status", ""));

        currentuser= FirebaseAuth.getInstance().getCurrentUser();

        image_storage= FirebaseStorage.getInstance().getReference();

        String uid=currentuser.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        onlineReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("Name",name);
                editor.putString("status",status);
                editor.putString("image",image);

                editor.commit();

                username.setText(sharedPreferences.getString("Name", ""));
                user_status.setText(sharedPreferences.getString("status", ""));

                if(!image.equals("default"))
                {
                    Picasso.with(settings.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.profile).into(user_image, new Callback() {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            Picasso.with(settings.this).load(image).placeholder(R.mipmap.profile).into(user_image);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v)
    {
        if(v == set_name)
        {
            final Intent set_name_intent=new Intent(getApplicationContext(),set_name.class);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String name=dataSnapshot.child("name").getValue().toString().trim();

                    set_name_intent.putExtra("user_name",name);

                    startActivity(set_name_intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

        if(v == set_status)
        {
            final Intent intent =new Intent(getApplicationContext(),set_Status.class);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String status=dataSnapshot.child("status").getValue().toString();

                    intent.putExtra("user_status",status);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(v == change_image)
        {
            Intent gallery_intent=new Intent();
            gallery_intent.setType("image/*");
            gallery_intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(gallery_intent,"Select image :D"),gallery_pick);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pick && resultCode == RESULT_OK)
        {
            Uri imageUri =data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                progressDialog =new ProgressDialog(settings.this);
                progressDialog.setTitle("uploading image..");
                progressDialog.setMessage("pls wait for uploading your data..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                Uri resultUri = result.getUri();

                File image_file=new File(resultUri.getPath());
                String current_id=currentuser.getUid();


                Bitmap thumb_bitmap = null;
                try
                {
                    thumb_bitmap = new Compressor(this).
                    setMaxWidth(200).setMaxHeight(200).setQuality(70).setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToBitmap(image_file);
                }
                catch (IOException e)
                {e.printStackTrace();}

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte [] thumb_byte =baos.toByteArray();



                StorageReference filepath=image_storage.child("profile_images").child(current_id+".jpg");
                final StorageReference thumb_filepath=image_storage.child("profile_images").child("thumbs").child(current_id+".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            final String download_url=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask=thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                String thumb_download_url=task.getResult().getDownloadUrl().toString();

                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                                {
                                    Map hashmap=new HashMap();
                                    hashmap.put("image",download_url);
                                    hashmap.put("thumb_image",thumb_download_url);

                                    if(task.isSuccessful())
                                    {

                                        databaseReference.updateChildren(hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(settings.this,"successful",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }

                                    else
                                    {
                                        Toast.makeText(settings.this,"Error in thumb",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(settings.this,"Error",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        onlineReference.child("online").setValue("true");
    }

/**
    @Override
    protected void onStop()
    {
        super.onStop();

        onlineReference.child("online").setValue(false);
    } **/
}
