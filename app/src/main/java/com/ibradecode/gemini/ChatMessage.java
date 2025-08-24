package com.ibradecode.gemini;

import java.util.HashMap;
import java.util.Map;

public class ChatMessage {
    private String role; // "user" or "model"
    private String text;
    private String time;
    private long timestamp;
    
    public ChatMessage(String role, String text, String time) {
        this.role = role;
        this.text = text;
        this.time = time;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public String getRole() {
        return role;
    }
    
    public String getText() {
        return text;
    }
    
    public String getTime() {
        return time;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isFromUser() {
        return "user".equals(role);
    }
    
    public boolean isFromAI() {
        return "model".equals(role);
    }
    
    // Setters
    public void setRole(String role) {
        this.role = role;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    // Conversion methods
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        map.put("text", text);
        map.put("time", time);
        map.put("timestamp", timestamp);
        return map;
    }
    
    public static ChatMessage fromMap(Map<String, Object> map) {
        try {
            String role = (String) map.get("role");
            String text = (String) map.get("text");
            String time = (String) map.get("time");
            
            ChatMessage message = new ChatMessage(role, text, time);
            
            if (map.containsKey("timestamp")) {
                Object timestampObj = map.get("timestamp");
                if (timestampObj instanceof Number) {
                    message.setTimestamp(((Number) timestampObj).longValue());
                }
            }
            
            return message;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "role='" + role + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ChatMessage that = (ChatMessage) o;
        
        if (timestamp != that.timestamp) return false;
        if (role != null ? !role.equals(that.role) : that.role != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return time != null ? time.equals(that.time) : that.time == null;
    }
    
    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}

