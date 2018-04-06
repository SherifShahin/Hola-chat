package com.downloader.hola;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity
{

    private FirebaseAuth firebaseAuth;
    private ViewPager mviewpager;
    private SectionsPagerAdapter msectionsPagerAdapter;
    private TabLayout mtabLayout;

    private FloatingActionButton search ;

    private DatabaseReference databaseReference;


    @Override
    protected void onStart()
    {
        super.onStart();
        //check if the user is logged in
        if(firebaseAuth.getCurrentUser() == null)
        {
            finish();
            Intent intent = new Intent(this,LogIn.class);
            startActivity(intent);
        }

        else
        {
            databaseReference.child("online").setValue("true");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();

        search=(FloatingActionButton) findViewById(R.id.fab_search);

       if(firebaseAuth.getCurrentUser() != null)
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());



        mviewpager=(ViewPager) findViewById(R.id.tabPager);
        msectionsPagerAdapter= new SectionsPagerAdapter(getSupportFragmentManager());

        mviewpager.setAdapter(msectionsPagerAdapter);

        mtabLayout=(TabLayout)  findViewById(R.id.main_tab_layout);
        mtabLayout.setupWithViewPager(mviewpager);

        FirebaseUser user= firebaseAuth.getCurrentUser();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent search_activity=new Intent(MainActivity.this,Search.class);
                startActivity(search_activity);
            }
        });


    }


   /** @Override
    protected void onStop()
    {
        super.onStop();

        databaseReference.child("online").setValue(false);
    }  **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    public void logout(MenuItem item)
    {
        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        finish();
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this,LogIn.class));
    }



    public void settings(MenuItem item)
    {
        startActivity(new Intent(MainActivity.this,settings.class));
    }


}


