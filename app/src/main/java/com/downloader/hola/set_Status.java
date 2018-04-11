package com.downloader.hola;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class set_Status extends AppCompatActivity
{
    private EditText set_status;
    private DatabaseReference databaseReference;
    private FirebaseUser currentuser;
    private Button change_status;
    private Button cancel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set__status);

        String status=getIntent().getStringExtra("user_status");

        set_status=(EditText) findViewById(R.id.set_status);
        set_status.setText(status);

        change_status=(Button) findViewById(R.id.change_status);
        cancel=(Button)findViewById(R.id.cancel);

        progressDialog =new ProgressDialog(this);
        currentuser= FirebaseAuth.getInstance().getCurrentUser();

        String uid=currentuser.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("status");


        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                progressDialog.setMessage("pls wait for saving changes...");
                progressDialog.show();
                databaseReference.setValue(set_status.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"status updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), settings.class));
                            finish();
                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(set_Status.this, "sorry try again later", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), settings.class));
                            finish();
                        }
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(),settings.class));
                finish();
            }
        });

    }
}
