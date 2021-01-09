package com.example.arielcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.arielcast.firebase.model.dataObject.Course;
import com.example.arielcast.firebase.model.dataObject.Lecture;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowCourse extends AppCompatActivity {
    private ImageView imageView;
    RecyclerView lecturesListView;
    MyLecturesAdapter myAdapter;
    FirebaseRecyclerOptions<Course> options;
    FirebaseRecyclerAdapter<Course,MyViewHolder> adapter;
    ArrayList<Lecture> lectures ;
    TextView title, description;
    Button deleteButton;
    FloatingActionButton fab;
    DatabaseReference ref;
    int position;
    String Id,email,cID;
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference DataRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_course);

        imageView = findViewById(R.id.lecture_image);
        title = findViewById(R.id.textViewMain);
        description = findViewById(R.id.textViewSub);
        deleteButton = findViewById(R.id.delete_button);
        fab=findViewById(R.id.floatingActionButton);
        ref = FirebaseDatabase.getInstance().getReference().child("Courses");

        lecturesListView = findViewById(R.id.recycleView);
        lecturesListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lecturesListView.setHasFixedSize(true);
        DataRef = FirebaseDatabase.getInstance().getReference().child("Courses");

        // get Extras from previous intent
         email= getIntent().getExtras().getString("Email");
         Id=getIntent().getExtras().getString("ID");
         cID=getIntent().getExtras().getString("CourseId");

        myAdapter = new MyLecturesAdapter(this, getMyList(),Id);
        lecturesListView.setAdapter(myAdapter);

        ref.child(String.valueOf(cID)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String courseName = snapshot.child("courseName").getValue(String.class);
                    String lecturerID = snapshot.child("lecturerId").getValue(String.class);

                    // get lecturer name
                    DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("Lecturers").child(lecturerID);

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String lecName=snapshot.child("fullname").getValue(String.class);

                            description.setText(lecName);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    title.setText(courseName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //check if it's lecturer user
        Query query = myRef.child("Lecturers").orderByChild("lecturerId").equalTo(Id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for(DataSnapshot data:snapshot.getChildren()) {
                        fab.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                } else {
                                  fab.setVisibility(View.GONE);
                                  deleteButton.setVisibility(View.GONE);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException(); // don't ignore errors
            }
        });

    }


    public void onfabClick(View v)
    {
            // save lecturer's email and start AddLectureActivity
            Intent i = new Intent(ShowCourse.this, AddLectureActivity.class);
            i.putExtra("Email", email);
            i.putExtra("ID", Id);
            i.putExtra("CourseId", cID);
            startActivity(i);

    }



    private ArrayList<Lecture> getMyList(){
        lectures = new ArrayList<>();

        Query q = FirebaseDatabase.getInstance().getReference().child("Lectures").orderByChild("courseId").equalTo(cID);;


        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Lecture c = data.getValue(Lecture.class);
                    lectures.add(c);
                    myAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return lectures;
    }

}