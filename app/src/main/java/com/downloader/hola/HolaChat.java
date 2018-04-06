package com.downloader.hola;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import static android.R.attr.data;

/**
 * Created by Lenovo on 3/16/2018.
 */

public class HolaChat extends Application
{
    private DatabaseReference databaseReference;

    private FirebaseAuth auth;

    @Override
    public void onCreate()
    {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        String uid=auth.getInstance().getUid();


        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null)
                    {
                        databaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        //Picasso
        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
