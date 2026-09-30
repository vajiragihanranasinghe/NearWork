package lk.vajira.nearwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private ArrayList<String> resultsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ResultsAdapter(ArrayList<String> resultsList, OnItemClickListener listener) {
        this.resultsList = resultsList;
        this.listener = listener;
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
        if (resultsList != null && position < resultsList.size()) {
            String item = resultsList.get(position);
            holder.textView.setText(item);
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return resultsList != null ? resultsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
