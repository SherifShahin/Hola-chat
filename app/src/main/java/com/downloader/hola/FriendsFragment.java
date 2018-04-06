package com.downloader.hola;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    private DatabaseReference friendsReference;
    private DatabaseReference userReference;

    private FirebaseAuth auth;

    private String Current_user;

    private RecyclerView recyclerView;

    private View mview;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mview=inflater.inflate(R.layout.fragment_friends,container,false);

        auth=FirebaseAuth.getInstance();

        Current_user=auth.getCurrentUser().getUid();

        friendsReference= FirebaseDatabase.getInstance().getReference().child("Friends").child(Current_user);
        friendsReference.keepSynced(true);
        userReference=FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);



        recyclerView=(RecyclerView) mview.findViewById(R.id.friends_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Inflate the layout for this fragment
        return mview;
    }

   @Override
    public void onStart()
    {
        super.onStart();

        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                            Friends.class,
                            R.layout.user_single_layout,
                            FriendsViewHolder.class,
                            friendsReference
                    )
                    {
                        @Override
                        protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends friends, int position)
                        {
                            viewHolder.setdate(friends.getDate());

                            final String list_user_id=getRef(position).getKey();

                            userReference.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    final String name=dataSnapshot.child("name").getValue().toString();
                                    final String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                                    viewHolder.setname(name);
                                    viewHolder.setimage(thumb_image,getContext());


                                    if(dataSnapshot.hasChild("online"))
                                    {
                                        String user_online_status=dataSnapshot.child("online").getValue().toString();
                                        viewHolder.set_online_status(user_online_status);
                                    }


                                    viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            CharSequence[] charSequence=new CharSequence[]{"open profile","send message"};

                                            final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                            builder.setTitle("Select option");

                                            builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    //open profile case
                                                    if(which == 0)
                                                    {
                                                        Intent profileIntent=new Intent(getContext(),profile.class);

                                                        profileIntent.putExtra("user_id",list_user_id);

                                                        ActivityOptionsCompat activityOptions= null;
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                                        {
                                                            profileIntent.putExtra("user_id", list_user_id);
                                                            activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),

                                                                    Pair.create(viewHolder.itemView.findViewById(R.id.Request_single_user_name), "nameTransition"),
                                                                    Pair.create(viewHolder.itemView.findViewById(R.id.Request_single_user_status), "statusTransition"),
                                                                    Pair.create(viewHolder.itemView.findViewById(R.id.RequestcircleImageView), "imageTransition")
                                                            );
                                                            startActivity(profileIntent, activityOptions.toBundle());
                                                        }

                                                        else
                                                        {
                                                            profileIntent.putExtra("user_id",list_user_id);
                                                            startActivity(profileIntent);
                                                        }

                                                    }

                                                    //send message case
                                                    if(which == 1)
                                                    {
                                                        Intent chatIntent =new Intent(getContext(),chatActivity.class);
                                                        chatIntent.putExtra("user_id",list_user_id);
                                                        chatIntent.putExtra("user_name",name);
                                                        chatIntent.putExtra("image",thumb_image);
                                                        startActivity(chatIntent);
                                                    }

                                                }
                                            });

                                            builder.show();

                                        }
                                    });


                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    };
                    recyclerView.setAdapter(firebaseRecyclerAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View view;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setdate(String date)
        {
            TextView friend_date=(TextView) view.findViewById(R.id.Request_single_user_status);
            friend_date.setText(date);
        }

        public void setname(String name)
        {
            TextView user_name=(TextView) view.findViewById(R.id.Request_single_user_name);
            user_name.setText(name);
        }

        public void setimage(String image,Context applicationContext)
        {
            CircleImageView user_image=(CircleImageView) view.findViewById(R.id.RequestcircleImageView);

            Picasso.with(applicationContext).load(image).placeholder(R.mipmap.profile).into(user_image);
        }

        public void set_online_status(String online_status)
        {

            ImageView online=(ImageView) view.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true"))
            {
                online.setVisibility(View.VISIBLE);
            }
            else
            {
                online.setVisibility(View.INVISIBLE);
            }
        }
    }
}
