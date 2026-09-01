# 🔍 JobHunter - İş Arama Asistanı

Android için geliştirilmiş, yapay zeka destekli iş arama ve otomatik başvuru uygulaması.

## 🎯 Özellikler

### ✅ Faz 1 (Mevcut)
- **🏢 Şirket Arama** - Google ve DuckDuckGo ile şirket bulma
- **📧 Email Çıkarma** - Şirket web sitelerinden e-posta adresi bulma
- **✍️ AI Destekli Ön Yazı** - OpenAI/Anthropic ile otomatik içerik oluşturma
- **👤 CV Yönetimi** - Profil, deneyim, eğitim, yetenek yönetimi
- **📤 E-posta Gönderme** - Gmail API veya cihaz e-posta uygulaması ile gönderme
- **📄 PDF Oluşturma** - Profesyonel CV/Özgeçmiş PDF'i oluşturma
- **📋 Şablonlar** - E-posta şablonları kaydetme/yükleme
- **🔔 Bildirimler** - Arka plan e-posta gönderiminde bildirim

### 📋 Faz 2 (Planlanan)
- **🔗 LinkedIn Entegrasyonu** - Profil düzenleme, iş ilanları listeleme
- **🎯 Kariyer.net Otomatik Başvuru** - İlan tarama ve otomatik başvuru
- **📊 İstatistikler** - Başvuru takibi, yanıt oranları

### 🚀 Faz 3 (Gelecek)
- **🤖 Deep Search** - Gelişmiş şirket analizi
- **📬 Toplu E-posta** - Şirket listelerine toplu gönderim (WorkManager ile)
- **📈 Dashboard** - Detaylı raporlar ve grafikler

## 🛠️ Teknolojiler

| Teknoloji | Kullanım |
|-----------|----------|
| **Kotlin** | Programlama dili |
| **Jetpack Compose** | UI framework |
| **Material 3** | Tasarım sistemi |
| **Room Database** | Yerel veritabanı |
| **OkHttp** | HTTP istemcisi |
| **Jsoup** | HTML parsing (web scraping) |
| **iText 7** | PDF oluşturma |
| **WorkManager** | Arka plan işlemleri |
| **Coil** | Resim yükleme |
| **Accompanist** | System UI, İzinler |
| **Gson** | JSON işleme |
| **Kotlin Coroutines** | Asenkron işlemler |
| **Navigation Compose** | Ekranlar arası geçiş |
| **FileProvider** | Dosya paylaşma |
| **GitHub Actions** | CI/CD |

## 📱 Ekranlar

| Ekran | Özellikler |
|-------|-----------|
| 🏠 **Dashboard** | Ana ekran, istatistikler, hızlı arama |
| 🔍 **Şirket Ara** | Web'de şirket arama, filtreleme, kaydetme |
| 🏢 **Şirketlerim** | Kayıtlı şirketler, toplu e-posta, silme |
| 📧 **E-posta** | Oluşturma, AI, şablonlar, gönderim geçmişi |
| 👤 **Profilim** | CV düzenleme, deneyim, eğitim, yetenekler |
| ⚙️ **Ayarlar** | Gmail, AI API, bilgilendirme metinleri |

## 🚀 Kurulum

### APK Oluşturma (GitHub Actions ile)
1. Bu repoyu GitHub'a fork edin veya push edin
2. GitHub Actions otomatik olarak APK oluşturur
3. Actions > Build JobHunter APK sayfasından APK'yı indirin

### APK Oluşturma (Yerel)
```bash
cd JobHunter
chmod +x gradlew
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

APK konumu: `app/build/outputs/apk/debug/app-debug.apk`

## ⚙️ Yapılandırma

### Gmail Bağlama
1. **Ayarlar > Gmail Hesabı**'na gidin
2. "Bağla" butonuna basın
3. Gmail adresinizi girin (ornek@gmail.com)
4. Google hesabınızla giriş yapın
5. İzinleri onaylayın

> 💡 Gmail bağlamadan da e-posta gönderebilirsiniz — cihazın kendi e-posta uygulaması açılır.

### AI API Anahtarı
1. **Ayarlar > AI Ayarları**'na gidin
2. Sağlayıcı seçin:
   - 🟢 **OpenAI** → https://platform.openai.com/api-keys
   - 🟣 **Anthropic** → https://console.anthropic.com/settings/keys
3. API anahtarınızı girin
4. "Kaydet" butonuna basın

> 🔑 Anahtarınız sadece cihazınızda saklanır, hiçbir yere gönderilmez.

### CV/Özgeçmiş Oluşturma
1. **Profilim** ekranına gidin
2. Kişisel bilgilerinizi girin
3. Deneyimlerinizi ekleyin
4. Eğitim bilgilerinizi ekleyin
5. Yeteneklerinizi ekleyin
6. **E-posta** ekranında "CV PDF Oluştur" butonuna basın
7. PDF oluşturulur ve paylaşılabilir

## 📁 Proje Yapısı

```
JobHunter/
├── app/src/main/java/com/jobhunter/app/
│   ├── MainActivity.kt
│   ├── models/
│   │   ├── Company.kt
│   │   ├── EmailTemplate.kt
│   │   └── UserProfile.kt
│   ├── data/
│   │   ├── JobHunterDatabase.kt
│   │   ├── Converters.kt
│   │   └── dao/
│   ├── services/
│   │   ├── WebScraperService.kt
│   │   ├── GmailService.kt
│   │   ├── AiService.kt
│   │   ├── PdfService.kt
│   │   └── BulkEmailWorker.kt
│   ├── viewmodels/
│   │   ├── DashboardViewModel.kt
│   │   ├── CompanySearchViewModel.kt
│   │   ├── EmailViewModel.kt
│   │   └── ProfileViewModel.kt
│   └── ui/
│       ├── navigation/
│       ├── theme/
│       └── screens/
├── .github/workflows/build.yml
└── README.md
```

## 🔒 Gizlilik

- ✅ Tüm veriler sadece cihazınızda saklanır
- ✅ Hiçbir veri sunucuya gönderilmez
- ✅ API anahtarları sadece cihazınızda kalır
- ✅ Web scraping sadece herkese açık bilgileri çeker
- ✅ Açık kaynak (GPL v3.0)

## 📄 Lisans

GPL v3.0 - Ücretsiz ve açık kaynak

## 🤘 Katkı

Hatalar ve öneriler için issue açabilirsiniz!

---

**Yapıldı ile ❤️ Freebuff tarafından**
