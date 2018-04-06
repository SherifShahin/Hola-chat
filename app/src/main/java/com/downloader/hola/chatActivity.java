package com.downloader.hola;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.downloader.hola.settings.gallery_pick;

public class chatActivity extends AppCompatActivity
{
    private String user_id;
    private String user_name;
    private String image;
    private String current_user;
    private Toolbar chatbar;
    private TextView muser_name;
    private TextView last_seen;
    private CircleImageView user_image;

    private ImageView send_message;
    private EditText message_text;
    private ImageButton send_image;
    private SwipeRefreshLayout refreshLayout;

    private RecyclerView messages_list;

    private final List<Messages> MessagesList=new ArrayList<>();

    private LinearLayoutManager linear_Layout;
    private MessageAdapter messageAdapter;

    private DatabaseReference userReference;
    private DatabaseReference messagesReference;

    private StorageReference image_storage;

    private static final int Total_items_to_load=10;
    private static final int gallery_pick=1;

    private int current_page=1;

    private int item_pos=0;
    private String last_key="";
    private  String prev_key="";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user_id=getIntent().getStringExtra("user_id");
        user_name=getIntent().getStringExtra("user_name");
        image=getIntent().getStringExtra("image");
        current_user=FirebaseAuth.getInstance().getUid().toString();

        userReference=FirebaseDatabase.getInstance().getReference();


        chatbar=(Toolbar)  findViewById(R.id.main_app_bar);

        setSupportActionBar(chatbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //actionBar.setTitle(user_name);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);


        messagesReference=FirebaseDatabase.getInstance().getReference();

        image_storage= FirebaseStorage.getInstance().getReference();

        muser_name=(TextView) findViewById(R.id.custom_bar_title);
        last_seen=(TextView) findViewById(R.id.custom_bar_lastseen);
        user_image=(CircleImageView) findViewById(R.id.custom_bar_image);

        send_message=(ImageView) findViewById(R.id.send_message_btn);
        message_text=(EditText) findViewById(R.id.message_text);
        send_image=(ImageButton) findViewById(R.id.send_image);

        messageAdapter=new MessageAdapter(MessagesList);

        messages_list=(RecyclerView) findViewById(R.id.messages_recycle_view);
        refreshLayout=(SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        linear_Layout=new LinearLayoutManager(this);

        messages_list.setHasFixedSize(true);
        messages_list.setLayoutManager(linear_Layout);

        messages_list.setAdapter(messageAdapter);

        load_messages();

        muser_name.setText(user_name);

        Picasso.with(chatActivity.this).load(image).placeholder(R.mipmap.profile).into(user_image);

        userReference.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String online=dataSnapshot.child("online").getValue().toString();

                if(online.equals("true"))
                {
                    last_seen.setText("online");
                }

                else
                {
                    getTimeAgo get_time_ago=new getTimeAgo();

                    long lastTime=Long.parseLong(online);

                    String lastSeenTime=get_time_ago.getTimeAgo(lastTime,getApplicationContext());

                    last_seen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userReference.child("Chat").child(current_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(user_id))
                {
                    Map chatAddMap=new HashMap();

                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map ChatUserMap =new HashMap();

                    ChatUserMap.put("Chat/"+current_user+"/"+user_id,chatAddMap);
                    ChatUserMap.put("Chat/"+user_id+"/"+current_user,chatAddMap);

                    userReference.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError != null)
                            {
                                Log.d("chat_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                SendMessage();

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                current_page++;

                item_pos=0;

                load_more_messages();
            }
        });

        send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent gallery_intent=new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery_intent,"Select image :D"),gallery_pick);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pick && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();


            final String Current_user_ref="messages/"+current_user+"/"+user_id;

            final String Chat_user_ref="messages/"+user_id+"/"+current_user;

            DatabaseReference user_message_push=userReference.child("messages").child(current_user).child(user_id).push();

            final String push_id=user_message_push.getKey();

            StorageReference filepath=image_storage.child("message_images").child(push_id +".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                {
                    if(task.isSuccessful())
                    {

                        String download_url=task.getResult().getDownloadUrl().toString();


                        Map messageMap=new HashMap();

                        messageMap.put("message",download_url);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",current_user);

                        Map messageUserMap=new HashMap();

                        messageUserMap.put(Current_user_ref+"/"+ push_id,messageMap);
                        messageUserMap.put(Chat_user_ref+"/"+ push_id,messageMap);

                        userReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                            {
                                if(databaseError == null)
                                    message_text.setText("");

                                if(databaseError != null)
                                {
                                    Log.d("message_LOG",databaseError.getMessage().toString());
                                }

                            }
                        });
                    }
                }
            });


        }
    }



    private void load_more_messages()
    {
        DatabaseReference message_ref=FirebaseDatabase.getInstance().getReference().child("messages").child(current_user).child(user_id);

        Query messageQuery=message_ref.orderByKey().endAt(last_key).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Messages message=dataSnapshot.getValue(Messages.class);


                if(!prev_key.equals(dataSnapshot.getKey())) {
                    MessagesList.add(item_pos++, message);
                }
                else
                {
                    prev_key = last_key;
                }

                if(item_pos == 1)
                {
                    last_key=dataSnapshot.getKey();
                }

                messageAdapter.notifyDataSetChanged();

                refreshLayout.setRefreshing(false);


                linear_Layout.scrollToPositionWithOffset(10 , 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void load_messages()
    {
        DatabaseReference message_ref=FirebaseDatabase.getInstance().getReference().child("messages").child(current_user).child(user_id);

        Query messageQuery=message_ref.limitToLast(current_page*Total_items_to_load);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Messages message=dataSnapshot.getValue(Messages.class);

                item_pos++;

                if(item_pos == 1)
                {
                    last_key=dataSnapshot.getKey();
                    prev_key=dataSnapshot.getKey();
                }

                MessagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                messages_list.scrollToPosition(MessagesList.size()-1);

                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void SendMessage()
    {
        String message=message_text.getText().toString().trim();

        if(!TextUtils.isEmpty(message))
        {
            String Current_user_ref="messages/"+current_user+"/"+user_id;

            String Chat_user_ref="messages/"+user_id+"/"+current_user;

            DatabaseReference user_message_push=userReference.child("messages").child(current_user).child(user_id).push();

            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();

            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",current_user);

            Map messageUserMap=new HashMap();

            messageUserMap.put(Current_user_ref+"/"+ push_id,messageMap);
            messageUserMap.put(Chat_user_ref+"/"+ push_id,messageMap);

            userReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                   if(databaseError == null)
                       message_text.setText("");

                    if(databaseError != null)
                    {
                        Log.d("message_LOG",databaseError.getMessage().toString());
                    }

                }
            });
        }

    }

}
