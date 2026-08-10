package com.example.nearwork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    RecyclerView chatRecyclerView;
    EditText messageInput;
    Button sendButton;
    TextView chatTitle;
    
    List<Message> messages = new ArrayList<>();
    ChatAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatTitle = findViewById(R.id.chatTitle);
        
        String contactName = getIntent().getStringExtra("contact_name");
        chatTitle.setText("Chat with " + (contactName != null ? contactName : "Service Provider"));
        
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Add sample messages
        messages.add(new Message("Hello! I'm interested in your service.", "me", System.currentTimeMillis()));
        messages.add(new Message("Hi! Sure, tell me what you need.", "them", System.currentTimeMillis()));
        messages.add(new Message("I need help with plumbing.", "me", System.currentTimeMillis()));
        messages.add(new Message("I can help with that. When are you free?", "them", System.currentTimeMillis()));
        
        adapter = new ChatAdapter(messages);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
        
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        
        messages.add(new Message(text, "me", System.currentTimeMillis()));
        adapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(messages.size() - 1);
        messageInput.setText("");
        
        // Auto-reply
        new android.os.Handler().postDelayed(() -> {
            messages.add(new Message("Thanks for your message! I'll get back to you soon.", "them", System.currentTimeMillis()));
            adapter.notifyDataSetChanged();
            chatRecyclerView.scrollToPosition(messages.size() - 1);
        }, 1000);
    }
    
    // Message Model
    static class Message {
        String text;
        String sender;
        long timestamp;
        
        Message(String text, String sender, long timestamp) {
            this.text = text;
            this.sender = sender;
            this.timestamp = timestamp;
        }
    }
    
    // Chat Adapter
    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        List<Message> messages;
        
        ChatAdapter(List<Message> messages) {
            this.messages = messages;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message msg = messages.get(position);
            holder.messageText.setText(msg.text);
            
            if (msg.sender.equals("me")) {
                holder.messageText.setBackgroundColor(0xFF667eea);
                holder.messageText.setTextColor(0xFFFFFFFF);
                holder.messageText.setGravity(android.view.Gravity.END);
            } else {
                holder.messageText.setBackgroundColor(0xFF2d3748);
                holder.messageText.setTextColor(0xFFFFFFFF);
                holder.messageText.setGravity(android.view.Gravity.START);
            }
        }
        
        @Override
        public int getItemCount() {
            return messages.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            
            ViewHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }
        }
    }
}
