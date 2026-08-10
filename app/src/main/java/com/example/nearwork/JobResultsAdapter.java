package com.example.nearwork;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class JobResultsAdapter extends RecyclerView.Adapter<JobResultsAdapter.ViewHolder> {

    private ArrayList<String> resultsList;

    public JobResultsAdapter(ArrayList<String> resultsList) {
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        
        view.setBackgroundColor(0xFF1A1A1A);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTextSize(16);
        textView.setPadding(32, 20, 20, 20);
        
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = resultsList.get(position);
        holder.textView.setText(item);
        
        holder.itemView.setOnClickListener(v -> {
            // Open job detail
            Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
            intent.putExtra("job_title", item);
            intent.putExtra("job_company", "Company Name");
            intent.putExtra("job_location", "Location");
            intent.putExtra("job_salary", "$50,000 - $80,000");
            intent.putExtra("job_type", "Full-time");
            intent.putExtra("job_description", "This is a great job opportunity!");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
