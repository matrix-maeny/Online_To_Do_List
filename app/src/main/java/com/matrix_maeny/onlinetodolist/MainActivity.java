package com.matrix_maeny.onlinetodolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.onlinetodolist.registerActivities.LoginActivity;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements WorkDialog.WorkDialogListener, WorkAdapter.WorkAdapterListener {

    private RecyclerView recyclerView;
    private WorkAdapter adapter;
    private ArrayList<WorkModel> list;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private TextView emptyTv;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }

    private void initialize() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        emptyTv = findViewById(R.id.emptyTv);

        list = new ArrayList<>();
        adapter = new WorkAdapter(MainActivity.this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);


//        fetchWorks();
        getCurrentUserData();
    }


    private void getCurrentUserData() {
        showProgressDialog("Fetching tasks");
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
//                            String temp = "<u>" + model.getUsername() + "  </u>"; // creating an underlined String from html
                            Objects.requireNonNull(getSupportActionBar()).setTitle(model.getUsername()); // setting title of the toolbar
                            fetchWorks();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_:
                // add work
                showAddDialog();
                break;
            case R.id.log_out:
                // log out
                signOut();
                break;
            case R.id.clear_all:
                // go to about activity
                clearAll();
                break;

            case R.id.about_:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearAll() {

        database.getReference().child("Tasks").child(Objects.requireNonNull(auth.getUid()))
                .removeValue().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "All cleared", Toast.LENGTH_SHORT).show();
                        list.clear();
                        refreshAdapter();
                        emptyTv.setVisibility(View.VISIBLE);

                    } else
                        Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddDialog() {
        WorkDialog dialog = new WorkDialog();
        dialog.show(getSupportFragmentManager(), "Create dialog");
    }

    private void signOut() {
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));

    }

    private void showProgressDialog(String title) {
        progressDialog.setTitle(title);
        progressDialog.setMessage("Please wait...");

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void saveWork(String workName) {
        uploadWork(workName);
    }

    private void uploadWork(String workName) {
        showProgressDialog("Uploading task");
        WorkModel model = new WorkModel(workName);
        database.getReference().child("Tasks").child(Objects.requireNonNull(auth.getUid()))
                .child(workName).setValue(model).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        fetchWorks();
                    } else
                        Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                    dismissProgressDialog();

                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchWorks() {
        database.getReference().child("Tasks").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                WorkModel model = s.getValue(WorkModel.class);

                                if (model != null) {
                                    list.add(model);
                                }


                            }

                        }

                        if (list.isEmpty()) {
                            emptyTv.setVisibility(View.VISIBLE);
                        } else {
                            emptyTv.setVisibility(View.GONE);
                        }
                        dismissProgressDialog();
                        refreshAdapter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public void refreshTasks() {
        fetchWorks();
    }
}