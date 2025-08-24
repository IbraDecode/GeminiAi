package com.ibradecode.gemini;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";
    private static final String PREFS_NAME = "ChatsHome";
    private static final String HISTORY_KEY = "history";
    
    private ApiClient apiClient;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    private MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();
    private MutableLiveData<List<ChatSession>> chatSessions = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private String currentChatId;
    private List<ChatMessage> currentMessages = new ArrayList<>();
    private List<ChatSession> sessions = new ArrayList<>();
    
    public ChatViewModel(@NonNull Application application) {
        super(application);
        apiClient = new ApiClient(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        
        loadChatSessions();
        isLoading.setValue(false);
    }
    
    // LiveData getters
    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }
    
    public LiveData<List<ChatSession>> getChatSessions() {
        return chatSessions;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    // Chat session management
    public void loadChatSessions() {
        try {
            String sessionsJson = sharedPreferences.getString(HISTORY_KEY, "[]");
            List<Map<String, Object>> sessionMaps = gson.fromJson(sessionsJson, 
                new TypeToken<List<Map<String, Object>>>(){}.getType());
            
            sessions.clear();
            for (HashMap<String, Object> sessionMap : sessionMaps) {
                ChatSession session = ChatSession.fromMap(sessionMap);
                if (session != null) {
                    sessions.add(session);
                }
            }
            
            chatSessions.setValue(new ArrayList<>(sessions));
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat sessions", e);
            sessions.clear();
            chatSessions.setValue(new ArrayList<>());
        }
    }
    
    public void createNewChatSession(String username) {
        try {
            String chatId = generateChatId();
            ChatSession newSession = new ChatSession(chatId, getCurrentTime());
            
            // Add initial context messages
            List<ChatMessage> initialMessages = new ArrayList<>();
            initialMessages.add(new ChatMessage("model", 
                "Of course " + username + "! From now on I'll start speaking to you by your name.", 
                getCurrentTime()));
            initialMessages.add(new ChatMessage("user", 
                "Hello, I'm " + username + "! From now on you'll start calling by my name.", 
                getCurrentTime()));
            
            newSession.setMessages(initialMessages);
            sessions.add(0, newSession);
            
            saveChatSessions();
            chatSessions.setValue(new ArrayList<>(sessions));
            
            currentChatId = chatId;
            currentMessages = new ArrayList<>(initialMessages);
            chatMessages.setValue(new ArrayList<>(currentMessages));
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating new chat session", e);
            errorMessage.setValue("Failed to create new chat");
        }
    }
    
    public void loadChatSession(String chatId) {
        try {
            currentChatId = chatId;
            
            for (ChatSession session : sessions) {
                if (session.getId().equals(chatId)) {
                    currentMessages = new ArrayList<>(session.getMessages());
                    chatMessages.setValue(new ArrayList<>(currentMessages));
                    return;
                }
            }
            
            // If session not found, create empty list
            currentMessages = new ArrayList<>();
            chatMessages.setValue(new ArrayList<>());
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat session", e);
            errorMessage.setValue("Failed to load chat");
        }
    }
    
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }
        
        try {
            isLoading.setValue(true);
            
            // Add user message
            ChatMessage userMessage = new ChatMessage("user", messageText.trim(), getCurrentTime());
            currentMessages.add(userMessage);
            chatMessages.setValue(new ArrayList<>(currentMessages));
            
            // Prepare conversation history for API
            String conversationHistory = gson.toJson(convertMessagesToApiFormat(currentMessages));
            
            // Send to API
            apiClient.sendMessage(messageText.trim(), conversationHistory, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        // Add AI response
                        ChatMessage aiMessage = new ChatMessage("model", response, getCurrentTime());
                        currentMessages.add(aiMessage);
                        
                        // Update UI
                        chatMessages.postValue(new ArrayList<>(currentMessages));
                        
                        // Save to storage
                        saveChatSession();
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing AI response", e);
                        errorMessage.postValue("Error processing response");
                    } finally {
                        isLoading.postValue(false);
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "API error: " + error);
                    errorMessage.postValue(error);
                    isLoading.postValue(false);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            errorMessage.setValue("Failed to send message");
            isLoading.setValue(false);
        }
    }
    
    public void deleteChatSession(String chatId) {
        try {
            sessions.removeIf(session -> session.getId().equals(chatId));
            saveChatSessions();
            chatSessions.setValue(new ArrayList<>(sessions));
            
            // If current chat was deleted, clear messages
            if (chatId.equals(currentChatId)) {
                currentChatId = null;
                currentMessages.clear();
                chatMessages.setValue(new ArrayList<>());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting chat session", e);
            errorMessage.setValue("Failed to delete chat");
        }
    }
    
    private void saveChatSession() {
        if (currentChatId == null) return;
        
        try {
            // Find and update the current session
            for (ChatSession session : sessions) {
                if (session.getId().equals(currentChatId)) {
                    session.setMessages(new ArrayList<>(currentMessages));
                    session.setLastMessageTime(getCurrentTime());
                    break;
                }
            }
            
            saveChatSessions();
            chatSessions.setValue(new ArrayList<>(sessions));
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat session", e);
        }
    }
    
    private void saveChatSessions() {
        try {
            List<Map<String, Object>> sessionMaps = new ArrayList<>();
            for (ChatSession session : sessions) {
                sessionMaps.add(session.toMap());
            }
            
            String sessionsJson = gson.toJson(sessionMaps);
            sharedPreferences.edit().putString(HISTORY_KEY, sessionsJson).apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat sessions", e);
        }
    }
    
    private List<HashMap<String, Object>> convertMessagesToApiFormat(List<ChatMessage> messages) {
        List<HashMap<String, Object>> apiMessages = new ArrayList<>();
        
        for (ChatMessage message : messages) {
            HashMap<String, Object> apiMessage = new HashMap<>();
            apiMessage.put("role", message.getRole());
            
            List<HashMap<String, String>> parts = new ArrayList<>();
            HashMap<String, String> textPart = new HashMap<>();
            textPart.put("text", message.getText());
            parts.add(textPart);
            
            apiMessage.put("parts", parts);
            apiMessages.add(apiMessage);
        }
        
        return apiMessages;
    }
    
    private String generateChatId() {
        return "GEM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Calendar.getInstance().getTime());
    }
    
    public void clearError() {
        errorMessage.setValue(null);
    }
}

