package com.downloader.hola;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.StringPrepParseException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.downloader.hola.R.id.textView;
import static java.security.AccessController.getContext;

public class profile extends AppCompatActivity
{

    private TextView uname;
    private TextView ustatus;
    private CircleImageView uimage;
    private ImageView Friend_Request;
    private ImageView Delete_Request;
    private static int resource;
    private static int resource2;


    private ProgressDialog progressDialog;

    private DatabaseReference databaseReference;
    private DatabaseReference FriendreqReferece;
    private DatabaseReference Friend_Reference;
    private DatabaseReference notificationsReference;
    private DatabaseReference onlineReference;
    private DatabaseReference Requests_Reference;
    private DatabaseReference DRequests_Reference;

    private FirebaseUser currentuser;

    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        Friend_Request=(ImageView) findViewById(R.id.Friend_request);
        Delete_Request=(ImageView) findViewById(R.id.Delete_Request);


        currentuser=FirebaseAuth.getInstance().getCurrentUser();

        current_state="not_friend";

        final String uid=currentuser.getUid();

        if(uid.equals(user_id))
        {
            Friend_Request.setVisibility(View.INVISIBLE);
            Friend_Request.setEnabled(false);

            Delete_Request.setVisibility(View.INVISIBLE);
            Delete_Request.setEnabled(false);
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("wait for loading user's data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        uimage=(CircleImageView) findViewById(R.id.profile_image1);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        FriendreqReferece=FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

        Friend_Reference=FirebaseDatabase.getInstance().getReference().child("Friends");

        notificationsReference=FirebaseDatabase.getInstance().getReference().child("notifications");

        onlineReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        Requests_Reference=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).child("Requests");

        DRequests_Reference=FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Requests");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                uname=(TextView) findViewById(R.id.name);
                uname.setText(name);

                ustatus=(TextView) findViewById(R.id.status);
                ustatus.setText(status);

                Picasso.with(profile.this).load(thumb_image).placeholder(R.mipmap.profile).into(uimage);

                uimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if(uid.equals(user_id) || current_state.equals("friends"))
                        {
                            Intent image_viewer= new Intent(getApplicationContext(),Image_viewer.class);
                            image_viewer.putExtra("image",image);
                            startActivity(image_viewer);
                        }

                    }
                });


                //Request Feature


                Friend_Reference.child(currentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            current_state="friends";

                            resource=R.drawable.unfriend;

                            Friend_Request.setTag(resource);
                            resource=(Integer) Friend_Request.getTag();
                            Friend_Request.setImageResource(resource);


                            resource2=R.drawable.message;

                            Delete_Request.setTag(resource2);
                            resource2=(Integer) Delete_Request.getTag();
                            Delete_Request.setImageResource(resource2);


                            Delete_Request.setVisibility(View.VISIBLE);
                        }

                        else
                        {
                            FriendreqReferece.child(currentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.hasChild(user_id)) {
                                        String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                                        if (req_type.equals("received"))
                                        {
                                            current_state = "req_received";


                                            resource=R.drawable.accept;

                                            Friend_Request.setTag(resource);
                                            resource=(Integer) Friend_Request.getTag();
                                            Friend_Request.setImageResource(resource);

                                            Delete_Request.setVisibility(View.VISIBLE);
                                        }
                                        else if (req_type.equals("sent"))
                                        {
                                            current_state = "req_sent";
                                            Delete_Request.setEnabled(false);

                                            resource=R.drawable.cancel_request;

                                            Friend_Request.setTag(resource);
                                            resource=(Integer) Friend_Request.getTag();
                                            Friend_Request.setImageResource(resource);

                                        }

                                        progressDialog.dismiss();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                      progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        Friend_Request.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Friend_Request.setEnabled(false);

                //-------send Request state--------
                if(current_state.equals("not_friend"))
                {

                    FriendreqReferece.child(uid).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                FriendreqReferece.child(user_id).child(uid).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {


                                        HashMap<String,String> notificationData=new HashMap<String, String>();
                                        notificationData.put("from",currentuser.getUid());
                                        notificationData.put("type","request");

                                        notificationsReference.child(user_id).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                Friend_Request.setEnabled(true);


                                                resource=R.drawable.cancel_request;

                                                Friend_Request.setTag(resource);
                                                resource=(Integer) Friend_Request.getTag();
                                                Friend_Request.setImageResource(resource);

                                                current_state="req_sent";
                                                Delete_Request.setEnabled(false);
                                                Requests_Reference.child(uid).child("id").setValue(uid);
                                                Toast.makeText(getApplicationContext(),"Request sent",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }

                            else
                            {
                                Toast.makeText(getApplicationContext(),"failed sending Request",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

               //-------------Cancel Request state---------------

                if(current_state.equals("req_sent"))
                {

                    FriendreqReferece.child(uid).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                FriendreqReferece.child(user_id).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            Friend_Request.setEnabled(true);

                                            resource=R.drawable.send_request;

                                            Friend_Request.setTag(resource);
                                            resource=(Integer) Friend_Request.getTag();
                                            Friend_Request.setImageResource(resource);

                                            current_state="not_friend";
                                            Delete_Request.setEnabled(false);
                                            Requests_Reference.child(uid).removeValue();
                                            Toast.makeText(getApplicationContext(),"Request canceled",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                // --- accept FriendRequest-------

                if(current_state.equals("req_received"))
                {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

                    final String currentDate= dateFormat.format(new Date()).toString()+" "+timeFormat.format(new Date()).toString();

                    Friend_Reference.child(currentuser.getUid()).child(user_id).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            Friend_Reference.child(user_id).child(currentuser.getUid()).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {

                                    FriendreqReferece.child(uid).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                FriendreqReferece.child(user_id).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                             Friend_Request.setEnabled(true);


                                                            resource=R.drawable.unfriend;

                                                            Friend_Request.setTag(resource);
                                                            resource=(Integer) Friend_Request.getTag();
                                                            Friend_Request.setImageResource(resource);

                                                            current_state="friends";
                                                            resource2=R.drawable.message;

                                                            Delete_Request.setTag(resource2);
                                                            resource2=(Integer) Delete_Request.getTag();
                                                            Delete_Request.setImageResource(resource2);

                                                            DRequests_Reference.child(user_id).removeValue();
                                                             Delete_Request.setVisibility(View.VISIBLE);
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

                //------Un FriendUser-------

                if(current_state.equals("friends"))
                {
                    Friend_Reference.child(currentuser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                               Friend_Reference.child(user_id).child(currentuser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task)
                                   {
                                       if(task.isSuccessful())
                                       {
                                           current_state="not_friend";
                                           Delete_Request.setEnabled(false);

                                           resource=R.drawable.send_request;

                                           Friend_Request.setTag(resource);
                                           resource=(Integer) Friend_Request.getTag();
                                           Friend_Request.setImageResource(resource);

                                           Delete_Request.setVisibility(View.INVISIBLE);
                                       }

                                   }
                               });
                            }
                        }
                    });
                }
            }
        });


        Delete_Request.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (resource2 == R.drawable.message)
                {
                   // Toast.makeText(getApplicationContext(),"message",Toast.LENGTH_SHORT).show();

                    final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String image =dataSnapshot.child("thumb_image").getValue().toString();


                            Intent chatIntent =new Intent(getApplicationContext(),chatActivity.class);
                            chatIntent.putExtra("user_id",user_id);
                            chatIntent.putExtra("user_name",name);
                            chatIntent.putExtra("image",image);
                            startActivity(chatIntent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

                }

                else
                {
                FriendreqReferece.child(uid).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendreqReferece.child(user_id).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Friend_Request.setEnabled(true);

                                        resource=R.drawable.send_request;

                                        Friend_Request.setTag(resource);
                                        resource=(Integer) Friend_Request.getTag();
                                        Friend_Request.setImageResource(resource);

                                        current_state = "not_friend";
                                        DRequests_Reference.child(user_id).removeValue();
                                        Delete_Request.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }
                });

            }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        onlineReference.child("online").setValue("true");
    }


  /**  @Override
    protected void onStop()
    {
        super.onStop();

        onlineReference.child("online").setValue(false);
    }**/

}