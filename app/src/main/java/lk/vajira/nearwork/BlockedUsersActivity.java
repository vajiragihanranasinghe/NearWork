package lk.vajira.nearwork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyText;
    BlockedAdapter adapter;
    final List<String> blockedUids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Blocked Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.blockedRecyclerView);
        emptyText    = findViewById(R.id.blockedEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BlockedAdapter();
        recyclerView.setAdapter(adapter);

        loadBlockedUsers();
    }

    private void loadBlockedUsers() {
        BlockManager.getBlockedUsers(new BlockManager.BlockListCallback() {
            @Override public void onResult(List<String> uids) {
                blockedUids.clear();
                if (uids != null) blockedUids.addAll(uids);
                adapter.notifyDataSetChanged();
                emptyText.setVisibility(blockedUids.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(blockedUids.isEmpty() ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(String message) {
                Toast.makeText(BlockedUsersActivity.this,
                        "Failed to load: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(BlockedUsersActivity.this)
                    .inflate(R.layout.item_blocked_user, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            String uid = blockedUids.get(position);
            h.uidText.setText("UID: " + uid);
            h.unblockButton.setOnClickListener(v ->
                BlockManager.unblockUser(uid, new BlockManager.BlockCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(BlockedUsersActivity.this,
                                "Unblocked", Toast.LENGTH_SHORT).show();
                        int pos = h.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            blockedUids.remove(pos);
                            notifyItemRemoved(pos);
                            if (blockedUids.isEmpty()) {
                                emptyText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override public void onError(String message) {
                        Toast.makeText(BlockedUsersActivity.this,
                                "Unblock failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                })
            );
        }
        @Override public int getItemCount() { return blockedUids.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView uidText; Button unblockButton;
            VH(View itemView) {
                super(itemView);
                uidText       = itemView.findViewById(R.id.blockedUserUid);
                unblockButton = itemView.findViewById(R.id.unblockButton);
            }
        }
    }
}
