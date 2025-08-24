package com.ibradecode.gemini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatSession {
    private String id;
    private String lastMessageTime;
    private long timestamp;
    private List<ChatMessage> messages;
    
    public ChatSession(String id, String lastMessageTime) {
        this.id = id;
        this.lastMessageTime = lastMessageTime;
        this.timestamp = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getLastMessageTime() {
        return lastMessageTime;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public String getLastMessageText() {
        if (messages.isEmpty()) {
            return "New conversation";
        }
        
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String text = lastMessage.getText();
        
        // Truncate if too long
        if (text.length() > 60) {
            return text.substring(0, 57) + "...";
        }
        
        return text;
    }
    
    public String getTitle() {
        return id;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        
        // Update last message time if messages exist
        if (!this.messages.isEmpty()) {
            ChatMessage lastMessage = this.messages.get(this.messages.size() - 1);
            this.lastMessageTime = lastMessage.getTime();
        }
    }
    
    public void addMessage(ChatMessage message) {
        if (message != null) {
            messages.add(message);
            lastMessageTime = message.getTime();
        }
    }
    
    // Conversion methods
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("lastMessageTime", lastMessageTime);
        map.put("timestamp", timestamp);
        
        // Convert messages to maps
        List<Map<String, Object>> messageMaps = new ArrayList<>();
        for (ChatMessage message : messages) {
            messageMaps.add(message.toMap());
        }
        map.put("messages", messageMaps);
        
        // For backward compatibility with old format
        map.put("history", new com.google.gson.Gson().toJson(messageMaps));
        
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public static ChatSession fromMap(Map<String, Object> map) {
        try {
            String id = (String) map.get("id");
            String lastMessageTime = (String) map.get("lastMessageTime");
            
            if (id == null) return null;
            if (lastMessageTime == null) lastMessageTime = "";
            
            ChatSession session = new ChatSession(id, lastMessageTime);
            
            // Set timestamp if available
            if (map.containsKey("timestamp")) {
                Object timestampObj = map.get("timestamp");
                if (timestampObj instanceof Number) {
                    session.setTimestamp(((Number) timestampObj).longValue());
                }
            }
            
            // Load messages
            List<ChatMessage> messages = new ArrayList<>();
            
            // Try new format first
            if (map.containsKey("messages")) {
                Object messagesObj = map.get("messages");
                if (messagesObj instanceof List) {
                    List<Map<String, Object>> messageMaps = (List<Map<String, Object>>) messagesObj;
                    for (Map<String, Object> messageMap : messageMaps) {
                        ChatMessage message = ChatMessage.fromMap(messageMap);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                }
            }
            // Fallback to old format
            else if (map.containsKey("history")) {
                String historyJson = (String) map.get("history");
                if (historyJson != null && !historyJson.isEmpty() && !historyJson.equals("[]")) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        List<Map<String, Object>> messageMaps = gson.fromJson(historyJson, 
                            new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType());
                        
                        for (Map<String, Object> messageMap : messageMaps) {
                            // Convert old format to new format
                            String role = "user".equals(messageMap.get("user")) ? "user" : "model";
                            String text = (String) messageMap.get("text");
                            String time = (String) messageMap.get("time");
                            
                            if (text != null && time != null) {
                                messages.add(new ChatMessage(role, text, time));
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors for old format
                    }
                }
            }
            
            session.setMessages(messages);
            return session;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "ChatSession{" +
                "id='" + id + '\'' +
                ", lastMessageTime='" + lastMessageTime + '\'' +
                ", timestamp=" + timestamp +
                ", messagesCount=" + messages.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ChatSession that = (ChatSession) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

