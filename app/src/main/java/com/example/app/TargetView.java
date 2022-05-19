package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class TargetView extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterTarget adapter;
    String userID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("View Targets");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        recyclerView = (RecyclerView) findViewById(R.id.activities);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<ModelTarget> options =
                new FirebaseRecyclerOptions.Builder<ModelTarget>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Activities").orderByChild("own").equalTo(FirebaseAuth.getInstance().getUid()),ModelTarget.class)
                        .build();


        adapter = new AdapterTarget(options);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AdapterTarget.OnItemClickListener() {
            @Override
            public void onItemClick(DataSnapshot documentSnapshot, int position) {

                ModelTarget activity = documentSnapshot.getValue(ModelTarget.class);
                activity.setState("finished");
                activity.setEnddate(getTodayDate());
                FirebaseDatabase.getInstance().getReference().child("Activities").child(documentSnapshot.getKey()).setValue(activity);
            }
        });
    }

    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1)
            return "JAN";
        if (month == 2)
            return "FEB";
        if (month == 3)
            return "MAR";
        if (month == 4)
            return "APR";
        if (month == 5)
            return "MAY";
        if (month == 6)
            return "JUN";
        if (month == 7)
            return "JUL";
        if (month == 8)
            return "AUG";
        if (month == 9)
            return "SEP";
        if (month == 10)
            return "OCT";
        if (month == 11)
            return "NOV";
        if (month == 12)
            return "DEC";
        return "JAN";
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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