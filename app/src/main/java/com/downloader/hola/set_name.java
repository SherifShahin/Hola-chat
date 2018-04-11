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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class set_name extends AppCompatActivity implements View.OnClickListener
{

    private EditText set_name_tx;

    private Button change_name;
    private Button cancel;

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);

        String name=getIntent().getStringExtra("user_name");

        set_name_tx=(EditText) findViewById(R.id.set_name_tx);
        set_name_tx.setText(name);

        change_name=(Button) findViewById(R.id.change_name);
        cancel=(Button) findViewById(R.id.cancel_name);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        progressDialog=new ProgressDialog(this);

        change_name.setOnClickListener(this);
        cancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View v)
    {
        if(v == change_name)
        {
            progressDialog.setMessage("pls wait for saving changes..");
            progressDialog.show();

            String new_name=set_name_tx.getText().toString().trim();
            databaseReference.child("name").setValue(new_name).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"name updated",Toast.LENGTH_SHORT).show();
                        Intent settings=new Intent(getApplicationContext(), com.downloader.hola.settings.class);
                        startActivity(settings);
                        finish();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"sorry try again later",Toast.LENGTH_SHORT).show();
                        Intent settings=new Intent(getApplicationContext(), com.downloader.hola.settings.class);
                        startActivity(settings);
                        finish();
                    }
                }
            });

        }

        if(v == cancel)
        {
            Intent settings=new Intent(getApplicationContext(), com.downloader.hola.settings.class);
            startActivity(settings);
            finish();
        }
    }
}
