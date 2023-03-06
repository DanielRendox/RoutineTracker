package com.rendox.routinetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class CategoryActivityRoutine extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity_routine);

        ImageView image = findViewById(R.id.routine_image);
        image.setImageResource(Routine.routines[0].getImageResourceId());

        ArrayAdapter<Task> englishTaskArrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Task.english
        );
        ListView englishTaskList = findViewById(R.id.list_tasks);
        englishTaskList.setAdapter(englishTaskArrayAdapter);
        englishTaskList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(CategoryActivityRoutine.this,
                    DetailActivityTask.class);
            intent.putExtra(DetailActivityTask.TASK_ID,position);
            startActivity(intent);
        });
    }
}