package com.downloader.hola;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.downloader.hola.R.id.Delete_Request;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment
{

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private RecyclerView recyclerView;

    private View mview;

    private Button accept_bt;

    private ImageButton delete_bt;

    private DatabaseReference FriendreqReferece;
    private DatabaseReference Friend_Reference;
    private DatabaseReference DRequests_Reference;


    public RequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mview= inflater.inflate(R.layout.fragment_requests, container, false);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Requests");
        // databaseReference.keepSynced(true);

        recyclerView=(RecyclerView) mview.findViewById(R.id.Requests_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        accept_bt=(Button) mview.findViewById(R.id.accept_fRequest);
        delete_bt=(ImageButton)mview.findViewById(R.id.delete_fRiquest);


        FriendreqReferece=FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

        Friend_Reference=FirebaseDatabase.getInstance().getReference().child("Friends");

        DRequests_Reference=FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Requests");



        // Inflate the layout for this fragment
        return mview;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                FirebaseRecyclerAdapter<Requests,RequestsViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                        Requests.class,
                        R.layout.request_layout,
                        RequestsViewHolder.class,
                        databaseReference

                ) {
                    @Override
                    protected void populateViewHolder(final RequestsViewHolder viewHolder, final Requests model, int position)
                    {
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot != null)
                                {
                                    final String id=model.getId();
                                    DatabaseReference user_request=FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                                    user_request.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            final String name=dataSnapshot.child("name").getValue().toString();
                                            String status=dataSnapshot.child("status").getValue().toString();
                                            String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                                            viewHolder.setname(name);
                                            viewHolder.setimage(thumb_image,getContext());
                                            viewHolder.setstatus(status);

                                         /**  viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent profileIntent=new Intent(getContext(),profile.class);


                                                    ActivityOptionsCompat activityOptions= null;
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                                    {
                                                        profileIntent.putExtra("user_id",id);
                                                        activityOptions= ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),

                                                                Pair.create(viewHolder.itemView.findViewById(R.id.single_user_name),"nameTransition"),
                                                                Pair.create(viewHolder.itemView.findViewById(R.id.single_user_status),"statusTransition"),
                                                                Pair.create(viewHolder.itemView.findViewById(R.id.circleImageView),"imageTransition")
                                                        );
                                                        startActivity(profileIntent,activityOptions.toBundle());
                                                    }
                                                    else {
                                                        profileIntent.putExtra("user_id",id);
                                                        startActivity(profileIntent);
                                                    }
                                                }
                                            });  **/

                                         viewHolder.accept_bt.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v)
                                             {

                                                 SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
                                                 SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

                                                 final String currentDate= dateFormat.format(new Date()).toString()+" "+timeFormat.format(new Date()).toString();

                                                 Friend_Reference.child(FirebaseAuth.getInstance().getUid()).child(id).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                     @Override
                                                     public void onComplete(@NonNull Task<Void> task)
                                                     {
                                                         Friend_Reference.child(id).child(FirebaseAuth.getInstance().getUid()).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>()
                                                         {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Void> task)
                                                             {

                                                                 FriendreqReferece.child(FirebaseAuth.getInstance().getUid()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                     @Override
                                                                     public void onComplete(@NonNull Task<Void> task)
                                                                     {
                                                                         if(task.isSuccessful())
                                                                         {
                                                                             FriendreqReferece.child(FirebaseAuth.getInstance().getUid()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                 @Override
                                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                                 {
                                                                                     if(task.isSuccessful())
                                                                                     {
                                                                                        // current_state="friends";
                                                                                         DRequests_Reference.child(id).removeValue();
                                                                                     }
                                                                                 }
                                                                             });
                                                                         }
                                                                     }
                                                                 });

                                                             }
                                                         });

                                                     }
                                                 });


                                             }
                                         });

                                         viewHolder.delete_bt.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v)
                                             {
                                               //  Toast.makeText(getContext(),id,Toast.LENGTH_SHORT).show();


                                                 FriendreqReferece.child(FirebaseAuth.getInstance().getUid()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                     @Override
                                                     public void onComplete(@NonNull Task<Void> task)
                                                     {
                                                         if(task.isSuccessful())
                                                         {
                                                             FriendreqReferece.child(id).child(FirebaseAuth.getInstance().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                 {
                                                                     if(task.isSuccessful())
                                                                     {
                                                                         DRequests_Reference.child(id).removeValue();
                                                                     }
                                                                 }
                                                             });
                                                         }
                                                     }
                                                 });

                                             }
                                         });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        View view;



        public RequestsViewHolder(View itemView) {
            super(itemView);

            view = itemView;

        }

        Button accept_bt=(Button) itemView.findViewById(R.id.accept_fRequest);
        ImageButton delete_bt=(ImageButton) itemView.findViewById(R.id.delete_fRiquest);

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

        public void setstatus(String status)
        {
            TextView user_status=(TextView) view.findViewById(R.id.Request_single_user_status);
            user_status.setText(status);
        }
    }
}
