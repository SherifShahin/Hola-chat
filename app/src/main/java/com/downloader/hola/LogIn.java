package com.downloader.hola;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LogIn extends AppCompatActivity implements View.OnClickListener
{
    private ImageView buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewRegister;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference databaseReference;
    private FirebaseUser currentuser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);


        buttonSignIn=(ImageView) findViewById(R.id.buttonSignIn);
        editTextEmail=(EditText) findViewById(R.id.EditTextEmail);
        editTextPassword=(EditText) findViewById(R.id.EditTextPassword);

        textViewRegister=(TextView) findViewById(R.id.TextViewRegister);

        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();

        buttonSignIn.setOnClickListener(this);
        textViewRegister.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("settings_data", Context.MODE_PRIVATE);
    }


    private void UserLogin()
    {

        String email= editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email))
        {
            //email empty
            Toast.makeText(this,"please enter email",Toast.LENGTH_SHORT).show();

            //stop the function
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            //password empty
            Toast.makeText(this,"please enter password",Toast.LENGTH_SHORT).show();

            //stopping the function
            return;
        }

        //first we will show progress bar
        progressDialog.setMessage("Login User pls wait....");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                progressDialog.dismiss();
                if(task.isSuccessful())
                {

                    currentuser= FirebaseAuth.getInstance().getCurrentUser();

                    String uid=currentuser.getUid();

                    databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String status=dataSnapshot.child("status").getValue().toString();
                            String image=dataSnapshot.child("image").getValue().toString();


                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("Name",name);
                            editor.putString("status",status);
                            editor.putString("image",image);

                            editor.commit();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });


                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                   // Toast.makeText(getApplicationContext(),"welcome Dad :D",Toast.LENGTH_SHORT).show();
                }

                else
                {
                    Toast.makeText(getApplicationContext(),"wrong email",Toast.LENGTH_SHORT).show();
                    editTextEmail.setText("");
                    editTextPassword.setText("");
                }
            }
        });

    }
    @Override
    public void onClick(View v)
    {
        if(v == buttonSignIn)
            UserLogin();

        if(v == textViewRegister)
        {
            //open Register activity
            finish();
            startActivity(new Intent(LogIn.this,Register.class));
        }
    }
}
