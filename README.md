# Gemini Chat Android App

Aplikasi Android chat dengan AI Gemini yang telah diperbarui dan disempurnakan dengan arsitektur modern dan fitur-fitur terbaru.

## 🚀 Fitur Utama

### ✨ Fitur Baru & Perbaikan
- **MVVM Architecture**: Implementasi arsitektur MVVM dengan ViewModel dan LiveData
- **Modern API Client**: OkHttp dengan logging interceptor dan error handling yang proper
- **Secure API Key**: API key dipindahkan ke BuildConfig untuk keamanan
- **Enhanced UI**: Material Design 3 dengan warna dan typography yang lebih modern
- **Better Error Handling**: Error handling yang comprehensive dengan user feedback
- **Markdown Support**: Rendering markdown dengan Markwon library
- **Improved Performance**: Optimisasi memory dan network usage

### 📱 Fitur Aplikasi
- **Splash Screen**: Animasi loading dengan auto-redirect
- **User Registration**: Input username untuk personalisasi
- **Chat Management**: Create, view, dan delete conversations
- **AI Chat**: Percakapan dengan Gemini AI dengan system prompt yang personal
- **Message Features**: Copy text, markdown rendering, dan link support
- **Persistent Storage**: Penyimpanan chat history dengan SharedPreferences

## 🛠 Teknologi & Dependencies

### Core Android
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 21 (Android 5.0)
- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)

### Libraries
```gradle
// AndroidX Core
androidx.appcompat:appcompat:1.7.0
androidx.core:core:1.13.1
androidx.lifecycle:lifecycle-viewmodel:2.8.4
androidx.lifecycle:lifecycle-livedata:2.8.4

// UI Components
com.google.android.material:material:1.12.0
androidx.recyclerview:recyclerview:1.3.2
androidx.constraintlayout:constraintlayout:2.1.4

// Network
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0
com.google.code.gson:gson:2.11.0

// Image Loading
com.github.bumptech.glide:glide:4.16.0
de.hdodenhof:circleimageview:3.1.0

// Markdown
io.noties.markwon:core:4.6.2
io.noties.markwon:linkify:4.6.2

// Animation
com.airbnb.android:lottie:6.1.0
```

## 🏗 Arsitektur

### MVVM Pattern
```
├── Model
│   ├── ChatMessage.java      # Data model untuk pesan
│   ├── ChatSession.java      # Data model untuk sesi chat
│   └── ApiClient.java        # Network layer
├── View
│   ├── MainActivity.java     # Splash screen
│   ├── RegistorActivity.java # User registration
│   ├── HomeActivity.java     # Chat list
│   └── ChatActivity.java     # Chat interface
└── ViewModel
    └── ChatViewModel.java    # Business logic & state management
```

### Data Flow
1. **View** → **ViewModel**: User interactions
2. **ViewModel** → **Model**: Data operations
3. **Model** → **ViewModel**: Data updates via LiveData
4. **ViewModel** → **View**: UI updates via Observer pattern

## 🔧 Setup & Installation

### Prerequisites
- Android Studio Arctic Fox atau lebih baru
- JDK 8 atau lebih baru
- Android SDK dengan API level 21-34

### Build Instructions
1. Clone repository
2. Open project di Android Studio
3. Sync Gradle files
4. Build & Run

```bash
git clone <repository-url>
cd MyProject
./gradlew assembleDebug
```

### API Configuration
API key Gemini sudah dikonfigurasi di `BuildConfig`. Untuk production, ganti dengan API key Anda sendiri di `app/build.gradle`:

```gradle
buildConfigField "String", "GEMINI_API_KEY", "\"YOUR_API_KEY_HERE\""
```

## 📱 Screenshots & UI

### Modern Material Design
- **Color Scheme**: Blue primary (#1565C0) dengan accent teal (#03DAC6)
- **Typography**: Google Sans font untuk konsistensi
- **Components**: Material Design 3 components
- **Animations**: Smooth transitions dan loading states

### Responsive Layout
- Support untuk berbagai ukuran layar
- Adaptive UI components
- Proper touch targets (48dp minimum)

## 🔒 Security & Privacy

### Security Measures
- API key tidak hardcoded di source code
- Input validation untuk mencegah injection
- Secure network communication dengan HTTPS
- Proper error handling tanpa expose sensitive data

### Privacy
- Data chat disimpan lokal di device
- Tidak ada tracking atau analytics
- Minimal permissions required

## 🚀 Performance Optimizations

### Network
- Connection pooling dengan OkHttp
- Request/response caching
- Timeout configuration
- Retry mechanism

### Memory
- Proper lifecycle management
- Image loading optimization dengan Glide
- RecyclerView untuk efficient list rendering
- Memory leak prevention

### Storage
- Efficient JSON serialization dengan Gson
- Optimized SharedPreferences usage
- Lazy loading untuk chat history

## 🧪 Testing

### Unit Tests
- ViewModel testing dengan LiveData
- Model class testing
- API client testing

### Integration Tests
- Database operations
- Network layer testing
- UI flow testing

## 📈 Future Enhancements

### Planned Features
- **Dark Mode**: Theme switching support
- **Voice Input**: Speech-to-text integration
- **Image Sharing**: Send dan receive images
- **Export Chat**: Export conversations
- **Search**: Search dalam chat history
- **Multiple AI Models**: Support untuk model AI lainnya
- **Push Notifications**: Real-time notifications
- **Cloud Sync**: Backup chat ke cloud

### Technical Improvements
- **Room Database**: Replace SharedPreferences
- **Dependency Injection**: Dagger/Hilt implementation
- **Coroutines**: Replace callbacks dengan coroutines
- **Compose UI**: Migrate ke Jetpack Compose
- **Modularization**: Multi-module architecture

## 👨‍💻 Developer

**Ibra Decode**
- Original creator dan developer
- Specialized dalam Android development
- Focus pada user experience dan performance

## 📄 License

Project ini dibuat untuk educational purposes. Silakan gunakan dan modifikasi sesuai kebutuhan.

## 🤝 Contributing

Contributions welcome! Please:
1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📞 Support

Untuk bug reports atau feature requests, silakan buat issue di repository ini.

---

**Version**: 2.0.0  
**Last Updated**: December 2024  
**Minimum Android Version**: 5.0 (API 21)  
**Target Android Version**: 14 (API 34)

