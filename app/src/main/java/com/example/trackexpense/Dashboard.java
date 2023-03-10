package com.example.trackexpense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView amountView;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private String onLineUserId;
    private ProgressDialog loader;
    private TodaysItemsAdapter todaysItemsAdapter;
    private List<Data> myDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Today's Records");

        amountView = findViewById(R.id.totalSpent);

        fab = findViewById(R.id.fab);

        mAuth = FirebaseAuth.getInstance();
        onLineUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("expenes").child(onLineUserId);
        loader = new ProgressDialog(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemSpentOn();
            }
        });

        recyclerView = findViewById(R.id.recView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        todaysItemsAdapter = new TodaysItemsAdapter(Dashboard.this,myDataList);
        recyclerView.setAdapter(todaysItemsAdapter);

        readItems();

    }
    private void readItems(){

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("expenses").child(onLineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Data data = snapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                todaysItemsAdapter.notifyDataSetChanged();

                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount+=pTotal;

                    amountView.setText("Total Amount on Today's Expense: $"+totalAmount);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addItemSpentOn() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner expeneSpinner = myView.findViewById(R.id.spinner);
        ArrayAdapter<String> expeneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.expenses));
        expeneAdapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        expeneSpinner.setAdapter(expeneAdapter);

        final EditText amount = myView.findViewById(R.id.amount);
        final EditText notes = myView.findViewById(R.id.note);
        final Button saveBtn = myView.findViewById(R.id.save);
        final Button cancelBtn = myView.findViewById(R.id.cancel);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mAmount = amount.getText().toString();
                String mNotes = notes.getText().toString();
                String item = expeneSpinner.getSelectedItem().toString();

                if (mAmount.isEmpty()){
                    amount.setError("Amount required!");
                    return;
                }
                if (mNotes.isEmpty()){
                    notes.setError("Note required!");
                    return;
                }
                if (item.equals("select item")){
                    Toast.makeText(Dashboard.this, "Select Item", Toast.LENGTH_SHORT).show();
                }

                else {
                    loader.setMessage("Adding expenses to database");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = ref.push().getKey();

                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    Data data = new Data(item, date, id, mNotes, Integer.parseInt(mAmount));
                    ref.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Dashboard.this, "Expense Added to Database", Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(Dashboard.this, "Failed to Add to Database", Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}