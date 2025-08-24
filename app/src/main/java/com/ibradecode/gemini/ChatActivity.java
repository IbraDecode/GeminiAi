package com.ibradecode.gemini;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.noties.markwon.Markwon;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;
    private Markwon markwon;
    
    // UI Components
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageView sendButton;
    private ProgressBar progressBar;
    private TextView titleText;
    private ImageView backButton;
    
    private String chatId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        
        initializeViews();
        initializeViewModel();
        initializeRecyclerView();
        initializeMarkdown();
        setupClickListeners();
        loadChatData();
        observeViewModel();
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerview1);
        editText = findViewById(R.id.edittext1);
        sendButton = findViewById(R.id.imageview1);
        progressBar = findViewById(R.id.progressbar1);
        titleText = findViewById(R.id.textview1);
        backButton = findViewById(R.id.imageview2);
        
        // Apply custom font
        applyCustomFont();
    }
    
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }
    
    private void initializeRecyclerView() {
        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void initializeMarkdown() {
        markwon = Markwon.builder(this)
                .usePlugin(io.noties.markwon.linkify.LinkifyPlugin.create())
                .build();
    }
    
    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> onBackPressed());
        
        editText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }
    
    private void loadChatData() {
        chatId = getIntent().getStringExtra("id");
        if (chatId != null) {
            viewModel.loadChatSession(chatId);
            titleText.setText(chatId);
        }
    }
    
    private void observeViewModel() {
        viewModel.getChatMessages().observe(this, messages -> {
            if (messages != null) {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });
        
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                sendButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
                sendButton.setEnabled(!isLoading);
            }
        });
        
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.clearError();
            }
        });
    }
    
    private void sendMessage() {
        String messageText = editText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            viewModel.sendMessage(messageText);
            editText.setText("");
        }
    }
    
    private void showError(String error) {
        Snackbar.make(editText, error, Snackbar.LENGTH_LONG)
                .setAction("Copy", v -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Error", error);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Error copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .show();
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
            } else if (child instanceof EditText) {
                ((EditText) child).setTypeface(typeface);
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    
    // RecyclerView Adapter
    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_USER = 1;
        private static final int VIEW_TYPE_AI = 2;
        
        private List<ChatMessage> messages = new ArrayList<>();
        
        public void updateMessages(List<ChatMessage> newMessages) {
            this.messages = new ArrayList<>(newMessages);
            notifyDataSetChanged();
        }
        
        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
        }
        
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.cht, parent, false);
            
            if (viewType == VIEW_TYPE_USER) {
                return new UserMessageViewHolder(view);
            } else {
                return new AiMessageViewHolder(view);
            }
        }
        
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            
            if (holder instanceof UserMessageViewHolder) {
                ((UserMessageViewHolder) holder).bind(message);
            } else if (holder instanceof AiMessageViewHolder) {
                ((AiMessageViewHolder) holder).bind(message);
            }
        }
        
        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
    
    // ViewHolder for user messages
    private class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout userBase;
        private TextView userName;
        private TextView userMessage;
        private TextView userTime;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userBase = itemView.findViewById(R.id.you_base);
            userName = itemView.findViewById(R.id.you_name_text);
            userMessage = itemView.findViewById(R.id.you_prompt_text);
            userTime = itemView.findViewById(R.id.you_message_time);
        }
        
        public void bind(ChatMessage message) {
            userBase.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.ia_base).setVisibility(View.GONE);
            
            userName.setText("You");
            userMessage.setText(message.getText());
            userTime.setText(message.getTime());
            
            userMessage.setTextIsSelectable(true);
            
            // Apply font
            try {
                Typeface font = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/googlesans.ttf");
                userName.setTypeface(font);
                userMessage.setTypeface(font);
                userTime.setTypeface(font);
            } catch (Exception e) {
                Log.w(TAG, "Could not apply font to user message", e);
            }
        }
    }
    
    // ViewHolder for AI messages
    private class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout aiBase;
        private CircleImageView aiProfile;
        private TextView aiName;
        private TextView aiMessage;
        private TextView aiTime;
        private ImageView copyButton;
        
        public AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            aiBase = itemView.findViewById(R.id.ia_base);
            aiProfile = itemView.findViewById(R.id.ia_perfil);
            aiName = itemView.findViewById(R.id.ia_name_text);
            aiMessage = itemView.findViewById(R.id.ia_message_time); // This seems to be the message text in the layout
            aiTime = itemView.findViewById(R.id.ia_message_time);
            copyButton = itemView.findViewById(R.id.imageview1);
        }
        
        public void bind(ChatMessage message) {
            aiBase.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.you_base).setVisibility(View.GONE);
            
            aiName.setText("Gemini");
            
            // Use Markwon to render markdown
            markwon.setMarkdown(aiMessage, message.getText());
            aiMessage.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(aiMessage, Linkify.ALL);
            
            aiTime.setText(message.getTime());
            
            // Set up copy functionality
            if (copyButton != null) {
                copyButton.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("AI Response", message.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(itemView.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                });
            }
            
            // Apply font
            try {
                Typeface font = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/googlesans.ttf");
                aiName.setTypeface(font);
                aiMessage.setTypeface(font);
                aiTime.setTypeface(font);
            } catch (Exception e) {
                Log.w(TAG, "Could not apply font to AI message", e);
            }
        }
    }
}
