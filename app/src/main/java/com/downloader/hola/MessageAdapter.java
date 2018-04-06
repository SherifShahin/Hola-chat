package com.downloader.hola;

import android.drm.DrmStore;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.button;
import static android.R.attr.fingerprintAuthDrawable;
import static com.downloader.hola.R.id.message_text;

/**
 * Created by Lenovo on 3/22/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Messages> messagesList;

    private DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");


    public MessageAdapter(List<Messages> messagesList)
    {
        this.messagesList = messagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
   {

       public TextView message_text;
       public CircleImageView message_image;
       public TextView message_time;
       public ImageView imageView;




       public MessageViewHolder(View itemView)
       {
           super(itemView);

           message_text=(TextView) itemView.findViewById(R.id.message_single_text);
           message_image=(CircleImageView) itemView.findViewById(R.id.message_single_profile);
           imageView=(ImageView) itemView.findViewById(R.id.send_image);

       }
   }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position)
    {
        String current_user=FirebaseAuth.getInstance().getUid().toString();

        Messages c=messagesList.get(position);


        String from_user=c.getFrom();

        String message_type=c.getType();


        databaseReference.child(from_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String image=dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.with(holder.message_image.getContext()).load(image).placeholder(R.mipmap.profile).into(holder.message_image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        if(from_user.equals(current_user))
        {
            holder.message_text.setBackgroundResource(R.drawable.message_background2);
            holder.message_text.setTextColor(Color.BLACK);

        /**    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.message_text.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.ALIGN_PARENT_END);

            holder.message_text.setLayoutParams(params); **/
        }
        else
        {
            holder.message_text.setBackgroundResource(R.drawable.message_background);
            holder.message_text.setTextColor(Color.WHITE);

        }


        if(message_type.equals("text"))
        {
            holder.message_text.setText(c.getMessage());

            holder.imageView.setVisibility(View.INVISIBLE);
        }

        else
        {
            holder.message_text.setVisibility(View.INVISIBLE);

            Picasso.with(holder.imageView.getContext()).load(c.getMessage()).into(holder.imageView);
        }





    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

}
