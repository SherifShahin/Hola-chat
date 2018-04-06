package com.downloader.hola;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Search extends AppCompatActivity
{
    private EditText search_text;
    private ImageButton search_Buttton;
    private RecyclerView recyclerView;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_Buttton=(ImageButton) findViewById(R.id.search_Button);
        recyclerView=(RecyclerView) findViewById(R.id.search_RecycleView);
        search_text=(EditText) findViewById(R.id.search_text);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        search_Buttton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String search_t=search_text.getText().toString();

                if(!TextUtils.isEmpty(search_t))
                search(search_t);

                else
                    Toast.makeText(Search.this,"no text",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void search(String search)
    {

        Query search_query=databaseReference.orderByChild("name").startAt(search).endAt(search+"\ufBff");

        FirebaseRecyclerAdapter<single_user,usersViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<single_user, usersViewHolder>
                (   single_user.class,
                        R.layout.user_single_layout,
                        usersViewHolder.class,
                        search_query
                )
        {

            @Override
            protected void populateViewHolder(final usersViewHolder viewHolder, single_user user, final int position)
            {

                viewHolder.setName(user.getName());
                viewHolder.setstatus(user.getStatus());
                viewHolder.setimage(user.getThumb_image(),getApplicationContext());

                viewHolder.view.setOnClickListener(new View.OnClickListener() {

                    String user_id=getRef(position).getKey();


                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent=new Intent(Search.this,profile.class);

                        ActivityOptionsCompat activityOptions= null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                        {
                            profileIntent.putExtra("user_id", user_id);
                            activityOptions= ActivityOptionsCompat.makeSceneTransitionAnimation(Search.this,

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


     recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class usersViewHolder extends RecyclerView.ViewHolder {
        View view;

        public usersViewHolder(View itemView)
        {
            super(itemView);
            view = itemView;
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
}
