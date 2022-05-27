package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class TargetAdd extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText editDescActivity;
    private Spinner editTypeActivity;
    private Button addButton;
    private FirebaseAuth mAuth;
    private TextView banner;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_add);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Target");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);

        addButton = (Button) findViewById(R.id.addActivity);
        addButton.setOnClickListener(this);

        editDescActivity = (EditText) findViewById(R.id.descActivity);

        editTypeActivity = findViewById(R.id.typeActivity);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this,
                R.array.types,
                R.layout.color_spinner_layout
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        editTypeActivity.setAdapter(adapter);
        editTypeActivity.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.banner:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.addActivity:
                addActivity();
                break;
        }
    }
////////////////////////////////////////////////////////////
    private void addActivity() {
        String type = editTypeActivity.getSelectedItem().toString();
        String desc = editDescActivity.getText().toString().trim();
        //String date = datePickerDialog.getDatePicker().toString().trim();
        String date = getTodayDate();


        if (desc.isEmpty()) {
            editDescActivity.setError("Description of the Activity is required !");
            editDescActivity.requestFocus();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        FirebaseDatabase.getInstance().getReference("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user  = snapshot.getValue(ModelUser.class);

                if(user != null)
                {
                    ModelTarget activity = new ModelTarget(type,desc,date, userID);
                    activity.setOwnName(user.getName());
                    mDatabase.child("Activities").push().setValue(activity).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(TargetAdd.this, "Activity has been added successfully!", Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                Toast.makeText(TargetAdd.this, "Something went wrong. Try again !", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
//////////////////////////////////////////////////////////////////////////////////
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(this, adapterView.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
