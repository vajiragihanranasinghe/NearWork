package com.example.nearwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class JobDetailActivity extends AppCompatActivity {

    TextView jobTitle, jobCompany, jobLocation, jobSalary, jobDescription, jobType;
    Button btnApply, btnSave, btnShare, btnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobTitle = findViewById(R.id.jobTitle);
        jobCompany = findViewById(R.id.jobCompany);
        jobLocation = findViewById(R.id.jobLocation);
        jobSalary = findViewById(R.id.jobSalary);
        jobDescription = findViewById(R.id.jobDescription);
        jobType = findViewById(R.id.jobType);
        btnApply = findViewById(R.id.btnApply);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        btnMap = findViewById(R.id.btnMap);

        // Get data
        String title = getIntent().getStringExtra("job_title");
        String company = getIntent().getStringExtra("job_company");
        String location = getIntent().getStringExtra("job_location");
        String salary = getIntent().getStringExtra("job_salary");
        String description = getIntent().getStringExtra("job_description");
        String type = getIntent().getStringExtra("job_type");

        jobTitle.setText(title != null ? title : "Job Position");
        jobCompany.setText("🏢 " + (company != null ? company : "Company Name"));
        jobLocation.setText("📍 " + (location != null ? location : "Location"));
        jobSalary.setText("💰 " + (salary != null ? salary : "$50,000 - $80,000"));
        jobDescription.setText(description != null ? description : "Job description goes here...");
        jobType.setText("📋 " + (type != null ? type : "Full-time"));

        // Apply Now
        btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "📝 Application submitted!", Toast.LENGTH_LONG).show();
        });

        // Save to Favorites
        btnSave.setOnClickListener(v -> {
            String jobInfo = "💼 " + title + "\n🏢 " + company + "\n📍 " + location;
            FavoritesManager.getInstance(this).addFavorite(jobInfo);
            Toast.makeText(this, "⭐ Job saved to favorites!", Toast.LENGTH_SHORT).show();
        });

        // Share Job
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = "📌 Job: " + title + 
                             "\n🏢 Company: " + company + 
                             "\n📍 Location: " + location +
                             "\n💰 Salary: " + salary +
                             "\n📋 Type: " + type +
                             "\n\nApply now on NearWork! 🚀";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Job via"));
        });

        // Map View
        btnMap.setOnClickListener(v -> {
            Toast.makeText(this, "🗺️ Opening map...", Toast.LENGTH_SHORT).show();
        });
    }
}
