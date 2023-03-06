package com.rendox.routinetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DetailActivityTask extends AppCompatActivity {

    public static final String TASK_ID = "task_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_task);

        Intent taskIntent = getIntent();
        Task task = Task.english[taskIntent.getIntExtra(TASK_ID,0)];

        TextView taskName = findViewById(R.id.task_name);
        taskName.setText(task.getName());
        TextView taskDescription = findViewById(R.id.task_description);
        taskDescription.setText(task.getDescription());
        Button taskOpenWebsiteButton = findViewById(R.id.task_open_website_button);
        taskOpenWebsiteButton.setOnClickListener(v -> {
            Uri uri;
            try {
                uri = Uri.parse(task.getLink());
            } catch (NullPointerException ex){
                return;
            }
            Intent linkIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(linkIntent);
        });
    }
}