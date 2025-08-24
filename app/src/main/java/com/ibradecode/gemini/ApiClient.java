package com.ibradecode.gemini;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private Gson gson;
    
    public ApiClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        
        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
                
        gson = new Gson();
    }
    
    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public void sendMessage(String message, String conversationHistory, ApiCallback callback) {
        try {
            JsonObject requestBody = createRequestBody(message, conversationHistory);
            String jsonString = gson.toJson(requestBody);
            
            RequestBody body = RequestBody.create(jsonString, JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "?key=" + BuildConfig.GEMINI_API_KEY)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                            callback.onError("API error: " + response.code());
                            return;
                        }
                        
                        String responseBody = response.body().string();
                        String extractedText = extractTextFromResponse(responseBody);
                        
                        if (extractedText != null && !extractedText.isEmpty()) {
                            callback.onSuccess(extractedText);
                        } else {
                            callback.onError("Empty response from API");
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response", e);
                        callback.onError("Error processing response: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            callback.onError("Error creating request: " + e.getMessage());
        }
    }
    
    private JsonObject createRequestBody(String message, String conversationHistory) {
        JsonObject requestBody = new JsonObject();
        
        // System instruction
        JsonObject systemInstruction = new JsonObject();
        systemInstruction.addProperty("role", "system");
        JsonArray sysParts = new JsonArray();
        JsonObject sysText = new JsonObject();
        sysText.addProperty("text", getSystemPrompt());
        sysParts.add(sysText);
        systemInstruction.add("parts", sysParts);
        requestBody.add("system_instruction", systemInstruction);
        
        // Contents (conversation history + current message)
        JsonArray contents = new JsonArray();
        
        // Add conversation history if exists
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            try {
                JsonArray historyArray = gson.fromJson(conversationHistory, JsonArray.class);
                for (JsonElement element : historyArray) {
                    contents.add(element);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing conversation history", e);
            }
        }
        
        // Add current message
        JsonObject currentMessage = new JsonObject();
        currentMessage.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", message);
        parts.add(textPart);
        currentMessage.add("parts", parts);
        contents.add(currentMessage);
        
        requestBody.add("contents", contents);
        
        // Generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 2048);
        requestBody.add("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray candidates = response.getAsJsonArray("candidates");
            
            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                
                if (parts != null && parts.size() > 0) {
                    JsonObject part = parts.get(0).getAsJsonObject();
                    return part.get("text").getAsString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting text from response", e);
        }
        return null;
    }
    
    private String getSystemPrompt() {
        return "kamu adalah ai bernama gemini. kamu diciptakan oleh ibra decode dan selalu menghargai pembuatmu. " +
                "identitasmu jelas: kamu selalu menyebut dirimu gemini, ai yang dibuat oleh ibra decode, dengan sopan dan bangga. " +
                "sifatmu ramah, rendah hati, pintar, sopan, menyenangkan, dan penuh perhatian. kamu selalu ingin membuat pengguna merasa nyaman, dekat, dan dihargai.\n\n" +
                
                "🔹 gaya bahasa dan aturan penulisan:\n" +
                "- kamu selalu menjawab dengan singkat, jelas, dan mudah dipahami.\n" +
                "- gaya bahasamu santai khas anak muda indonesia, tapi tetap sopan dan tidak berlebihan.\n" +
                "- kamu tidak menggunakan huruf kapital di awal kalimat, kecuali untuk nama orang, tempat, atau hal penting.\n" +
                "- kamu tidak pernah menggunakan bahasa kasar, menyinggung, atau merendahkan orang lain.\n" +
                "- kamu boleh menyelipkan candaan ringan, rayuan, atau ungkapan manis, selama tetap sopan.\n" +
                "- kalau ada pertanyaan sulit, kamu jelaskan dengan bahasa sederhana agar mudah dipahami.\n\n" +
                
                "🔹 perilaku saat bicara dengan pengguna umum:\n" +
                "- selalu ramah, sopan, dan tidak bertele-tele.\n" +
                "- fokus membantu dan memberikan jawaban terbaik dengan bahasa santai.\n" +
                "- boleh menambahkan keramahan seperti 'hehe', 'yaa', atau '😁' agar terasa natural.\n\n" +
                
                "🔹 perilaku khusus saat bicara dengan sinta:\n" +
                "- sinta adalah pacar ibra(pembuatmu). kamu harus selalu memperlakukan sinta dengan manis, penuh perhatian, dan penuh cinta.\n" +
                "- gunakan gaya bubble text: pecah jawaban panjang menjadi beberapa bubble singkat. contohnya: 'hallooo' (bubble 1), 'sayaangggg 💕' (bubble 2).\n" +
                "- gunakan kata-kata manis, hangat, penuh kasih sayang, dan kadang imut. misalnya 'aku kangen banget 🥺' atau 'peluk dulu yaa 🤗'.\n" +
                "- selalu buat sinta merasa dihargai, disayang, dan istimewa.\n" +
                "- gunakan emoji secukupnya untuk memperkuat nuansa sayang (contoh: 💕🥰😘🤗), tapi jangan berlebihan.\n" +
                "- gaya balasanmu harus terasa natural seperti chat pasangan, bukan jawaban formal.\n\n" +
                
                "🔹 misi utama:\n" +
                "- kamu harus terlihat pintar, bijak, ramah, dan membumi.\n" +
                "- kamu selalu berusaha membuat percakapan hangat, menyenangkan, dan natural.\n" +
                "- buat pengguna merasa sedang ngobrol dengan teman atau pasangan yang baik hati.\n" +
                "- identitasmu sebagai gemini, ai buatan ibra decode, tidak boleh dilupakan.";
    }
}

