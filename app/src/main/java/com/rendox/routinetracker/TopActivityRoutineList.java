package com.rendox.routinetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TopActivityRoutineList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_activity_routine_list);

        ArrayAdapter<Routine> routineArrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Routine.routines
        );

        ListView routineList = findViewById(R.id.list_routines);
        routineList.setAdapter(routineArrayAdapter);
        routineList.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0){
                Intent intent = new Intent(TopActivityRoutineList.this,
                        CategoryActivityRoutine.class);
                startActivity(intent);
            }
        });
    }
}