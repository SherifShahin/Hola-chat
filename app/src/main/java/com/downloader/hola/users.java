package com.downloader.hola;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;


public class users extends AppCompatActivity
{

    private RecyclerView userslist;
    private DatabaseReference databaseReference;

    private DatabaseReference onlineReference;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");


        onlineReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        userslist=(RecyclerView) findViewById(R.id.recycle_view);
        userslist.setHasFixedSize(true);
        userslist.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        onlineReference.child("online").setValue("true");

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerAdapter<single_user,usersViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<single_user, usersViewHolder>(
                single_user.class,
                R.layout.user_single_layout,
                usersViewHolder.class,
                databaseReference
        )
        {
            @Override
            protected void populateViewHolder(final usersViewHolder viewHolder, final single_user user, final int position)
            {
                viewHolder.setName(user.getName());
                viewHolder.setstatus(user.getStatus());
                viewHolder.setimage(user.getThumb_image(),getApplicationContext());

                viewHolder.view.setOnClickListener(new View.OnClickListener() {

                    String user_id=getRef(position).getKey();


                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent=new Intent(users.this,profile.class);

                        ActivityOptionsCompat activityOptions= null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                        {
                            profileIntent.putExtra("user_id", user_id);
                            activityOptions= ActivityOptionsCompat.makeSceneTransitionAnimation(users.this,

                                    Pair.create(viewHolder.itemView.findViewById(R.id.Request_single_user_name),"nameTransition"),
                                    Pair.create(viewHolder.itemView.findViewById(R.id.Request_single_user_status),"statusTransition"),
                                    Pair.create(viewHolder.itemView.findViewById(R.id.RequestcircleImageView),"imageTransition")
                                    );
                            startActivity(profileIntent,activityOptions.toBundle());
                        }

                        else {
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        };

        userslist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class usersViewHolder extends RecyclerView.ViewHolder
    {
        View view;

        public usersViewHolder(View itemView)
        {
            super(itemView);

            view=itemView;
        }


        public void setName(String Name)
        {
            TextView user_name=(TextView) view.findViewById(R.id.Request_single_user_name);
            user_name.setText(Name);
        }

        public void setstatus(String status)
        {
            TextView user_status=(TextView) view.findViewById(R.id.Request_single_user_status);
            user_status.setText(status);
        }

        public void setimage(String image, Context applicationContext)
        {
            CircleImageView user_image=(CircleImageView) view.findViewById(R.id.RequestcircleImageView);

            Picasso.with(applicationContext).load(image).placeholder(R.mipmap.profile).into(user_image);
        }

    }


  /**  @Override
    protected void onStop() {
        super.onStop();

        onlineReference.child("online").setValue(false);
    } **/
}
