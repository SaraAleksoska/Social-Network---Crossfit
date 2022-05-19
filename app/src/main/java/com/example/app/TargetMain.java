package com.example.app;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TargetMain extends AppCompatActivity implements View.OnClickListener{

    private Button addNewActivity, viewActivities;
    String userID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Targets");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        addNewActivity = (Button) findViewById(R.id.add);
        addNewActivity.setOnClickListener(this);

        viewActivities = (Button) findViewById(R.id.view);
        viewActivities.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.add:
                startActivity(new Intent(this, TargetAdd.class));
                break;
            case R.id.view:
                startActivity(new Intent(this, TargetView.class));
                break;
        }
    }
    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            userID = user.getUid();

        }
        else
        {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_target).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}