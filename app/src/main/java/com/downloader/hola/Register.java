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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static android.R.attr.data;


public class Register extends AppCompatActivity implements View.OnClickListener
{
    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewsignin;
    private EditText editTextName;


    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private DatabaseReference databaseReference;
    private boolean s=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buttonRegister=(Button) findViewById(R.id.buttonRegister);
        editTextEmail=(EditText) findViewById(R.id.EditTextEmail);
        editTextPassword=(EditText) findViewById(R.id.EditTextPassword);
        editTextName=(EditText) findViewById(R.id.EditTextName);

        textViewsignin=(TextView) findViewById(R.id.TextViewSignin);

        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        buttonRegister.setOnClickListener(this);
        textViewsignin.setOnClickListener(this);
    }


    private void registerUser()
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

       if(password.length() < 8)
       {
           //password is not enough
           Toast.makeText(this,"this password is short",Toast.LENGTH_SHORT).show();

           editTextPassword.setText("");

           //stopping the function
           return;
       }



       //first we will show progress bar
       progressDialog.setMessage("Registering User....");
     //   progressDialog.setCanceledOnTouchOutside(false);
       progressDialog.show();

        // check if the email is already in database or no
        firebaseAuth.fetchProvidersForEmail(editTextEmail.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>()
        {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task)
            {
                boolean check= task.getResult().getProviders().isEmpty();

                // if the email  existing in database
                if(!check)
                {
                    progressDialog.hide();
                    Toast.makeText(Register.this,"sorry this email is already used",Toast.LENGTH_SHORT).show();
                    editTextEmail.setText("");
                    editTextPassword.setText("");
                }

                // the email not exist
                else
                {
                   s=false;
                }
            }
        });

        // create new account if the email not exist in the database
        if(!s)
        {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();

                        FirebaseUser currentUser =FirebaseAuth.getInstance().getCurrentUser();
                        String uid=currentUser.getUid();

                        database=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                        HashMap<String,String> data=new HashMap<>();
                        data.put("name",editTextName.getText().toString().trim());
                        data.put("status","HI, there i'm using Hola chat app");
                        data.put("image","default");
                        data.put("thumb_image","default");

                        database.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {


                                SharedPreferences sharedPreferences = getSharedPreferences("settings_data", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putString("Name",editTextName.getText().toString().trim());
                                editor.putString("status","HI, there i'm using Hola chat app");
                                editor.putString("image","default");

                                editor.commit();


                                //user is successful registered and logged in
                                Toast.makeText(Register.this,"Registered Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent intent=new Intent(getApplicationContext(),LogIn.class);
                                startActivity(intent);
                            }
                        });

                    }
                    else
                      {
                        progressDialog.hide();
                        Toast.makeText(Register.this, "couldn't register.. pls try again", Toast.LENGTH_SHORT).show();
                      }
                }
            });
        }
    }

    @Override
    public void onClick(View v)
    {

        if(v == buttonRegister)
            registerUser();

        if(v == textViewsignin)
        {
            //open sign in activity
            finish();
            startActivity(new Intent(Register.this,LogIn.class));
        }
    }




}
