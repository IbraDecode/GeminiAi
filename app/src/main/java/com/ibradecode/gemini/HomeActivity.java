package com.ibradecode.gemini;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    
    private ChatViewModel viewModel;
    private HomeAdapter homeAdapter;
    
    // UI Components
    private ListView listView;
    private LinearLayout emptyStateLayout;
    private LinearLayout newChatButton;
    private TextView titleText;
    
    private String username;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        initializeViews();
        initializeViewModel();
        setupClickListeners();
        loadUserData();
        observeViewModel();
        applyCustomFont();
    }
    
    private void initializeViews() {
        listView = findViewById(R.id.listview1);
        emptyStateLayout = findViewById(R.id.linear4);
        newChatButton = findViewById(R.id.linear2);
        titleText = findViewById(R.id.textview4);
        
        // Style new chat button
        newChatButton.setBackground(new GradientDrawable() {
            {
                setCornerRadius(28);
                setColor(0xFF1565C0);
            }
        });
    }
    
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }
    
    private void setupClickListeners() {
        newChatButton.setOnClickListener(v -> createNewChat());
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            List<ChatSession> sessions = homeAdapter.getSessions();
            if (position < sessions.size()) {
                ChatSession session = sessions.get(position);
                openChat(session.getId());
            }
        });
        
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            List<ChatSession> sessions = homeAdapter.getSessions();
            if (position < sessions.size()) {
                ChatSession session = sessions.get(position);
                showDeleteDialog(session);
            }
            return true;
        });
    }
    
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("username", Activity.MODE_PRIVATE);
        username = prefs.getString("username", "User");
    }
    
    private void observeViewModel() {
        viewModel.getChatSessions().observe(this, sessions -> {
            if (sessions != null) {
                updateUI(sessions);
            }
        });
        
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Handle error - could show a toast or snackbar
                Log.e(TAG, "Error: " + error);
                viewModel.clearError();
            }
        });
    }
    
    private void updateUI(List<ChatSession> sessions) {
        if (sessions.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            
            if (homeAdapter == null) {
                homeAdapter = new HomeAdapter();
                listView.setAdapter(homeAdapter);
            }
            
            homeAdapter.updateSessions(sessions);
        }
    }
    
    private void createNewChat() {
        viewModel.createNewChatSession(username);
        
        // Get the latest session and open it
        viewModel.getChatSessions().observe(this, sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                ChatSession latestSession = sessions.get(0);
                openChat(latestSession.getId());
            }
        });
    }
    
    private void openChat(String chatId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id", chatId);
        startActivity(intent);
        finish();
    }
    
    private void showDeleteDialog(ChatSession session) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle("Delete Conversation");
        dialog.setIcon(R.drawable.icon_delete_round);
        dialog.setMessage("Are you sure you want to delete this conversation?");
        
        dialog.setNegativeButton("Cancel", (dialogInterface, which) -> {
            // Do nothing
        });
        
        dialog.setPositiveButton("Delete", (dialogInterface, which) -> {
            viewModel.deleteChatSession(session.getId());
        });
        
        dialog.create().show();
    }
    
    private void applyCustomFont() {
        try {
            Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/googlesans.ttf");
            applyFontToViewGroup((ViewGroup) findViewById(android.R.id.content), customFont);
        } catch (Exception e) {
            Log.w(TAG, "Could not apply custom font", e);
        }
    }
    
    private void applyFontToViewGroup(ViewGroup viewGroup, Typeface typeface) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyFontToViewGroup((ViewGroup) child, typeface);
            } else if (child instanceof TextView) {
                ((TextView) child).setTypeface(typeface);
            }
        }
    }
    
    // ListView Adapter
    private class HomeAdapter extends BaseAdapter {
        private List<ChatSession> sessions = new ArrayList<>();
        
        public void updateSessions(List<ChatSession> newSessions) {
            this.sessions = new ArrayList<>(newSessions);
            notifyDataSetChanged();
        }
        
        public List<ChatSession> getSessions() {
            return sessions;
        }
        
        @Override
        public int getCount() {
            return sessions.size();
        }
        
        @Override
        public ChatSession getItem(int position) {
            return sessions.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.chats, parent, false);
            }
            
            ChatSession session = getItem(position);
            
            TextView title = view.findViewById(R.id.title);
            TextView time = view.findViewById(R.id.time);
            TextView preview = view.findViewById(R.id.textview1);
            
            title.setText(session.getTitle());
            time.setText(session.getLastMessageTime());
            preview.setText(session.getLastMessageText());
            
            // Apply custom font
            try {
                Typeface font = Typeface.createFromAsset(getAssets(), "fonts/googlesans.ttf");
                title.setTypeface(font);
                time.setTypeface(font);
                preview.setTypeface(font);
            } catch (Exception e) {
                Log.w(TAG, "Could not apply font to list item", e);
            }
            
            return view;
        }
    }
}
