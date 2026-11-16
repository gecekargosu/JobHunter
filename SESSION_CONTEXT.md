# 🧠 SESSION CONTEXT - JobHunter Projesi
## Son Güncelleme: 3 Eylül 2026, 01:45

---

# 📋 PROJE ÖZETİ
**JobHunter** - Android iş arama ve otomatik başvuru uygulaması
- **Platform:** Android (Kotlin + Jetpack Compose)
- **Paket:** com.jobhunter.app
- **Versiyon:** 3.0.0 (build: v3, versionCode: 2)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34
- **APK Boyutu:** 18 MB
- **Dosya:** /sdcard/Download/JobHunter.apk

---

# 🌳 MİMARİ AĞAÇ (Architecture Tree)

```
JobHunter/
├── app/
│   ├── build.gradle.kts                    ← Build ayarları, dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml             ← Uygulama izinleri
│       ├── java/com/jobhunter/app/
│       │   │
│       │   ├── MainActivity.kt             ← Ana aktivite, Navigation host
│       │   │
│       │   ├── 📂 data/                    ← VERİ TABANI
│       │   │   ├── JobHunterDatabase.kt    ← Room database
│       │   │   ├── Converters.kt           ← Type converter'lar
│       │   │   └── dao/
│       │   │       ├── CompanyDao.kt       ← Şirket DAO
│       │   │       ├── DocumentDao.kt      ← Belge DAO
│       │   │       ├── EmailDao.kt         ← E-posta DAO
│       │   │       ├── JobApplicationDao.kt← Başvuru DAO
│       │   │       ├── JobListingDao.kt    ← İlan DAO
│       │   │       └── UserProfileDao.kt   ← Profil DAO
│       │   │
│       │   ├── 📂 models/                  ← VERİ MODELLERİ
│       │   │   ├── Company.kt              ← Şirket modeli
│       │   │   ├── Document.kt             ← Belge modeli (CV, sertifika)
│       │   │   ├── EmailTemplate.kt        ← E-posta şablonu
│       │   │   ├── JobApplication.kt       ← Başvuru modeli
│       │   │   └── UserProfile.kt          ← Kullanıcı profili
│       │   │
│       │   ├── 📂 services/                ← İŞ LOGİĞİ SERVİSLERİ
│       │   │   ├── AiService.kt            ← AI servisi (eski/temel)
│       │   │   ├── AtsService.kt           ← ATS uyumluluk kontrolü
│       │   │   ├── BulkEmailWorker.kt      ← Toplu e-posta gönderimi
│       │   │   ├── EmailAttachmentService.kt← E-posta ek yönetimi
│       │   │   ├── GmailService.kt         ← Gmail API entegrasyonu
│       │   │   ├── JobSearchService.kt     ← İş arama servisi
│       │   │   ├── KariyerNetService.kt    ← Kariyer.net scraping
│       │   │   ├── LinkedInService.kt      ← LinkedIn scraping
│       │   │   ├── NvidiaNimService.kt     ← 🤖 NVIDIA AI API (YENİ)
│       │   │   ├── PdfService.kt           ← PDF oluşturma
│       │   │   ├── PlatformJobSearchService.kt← Platform iş arama
│       │   │   ├── SmtpEmailService.kt     ← SMTP e-posta gönderimi
│       │   │   └── WebScraperService.kt    ← Web scraping motoru
│       │   │
│       │   ├── 📂 ui/                      ← KULLANICI ARAYÜZÜ
│       │   │   ├── navigation/
│       │   │   │   └── NavGraph.kt         ← Sayfa navigasyonu
│       │   │   ├── screens/
│       │   │   │   ├── AiAssistantScreen.kt        ← 🤖 AI Asistan (YENİ)
│       │   │   │   ├── ApplicationTrackingScreen.kt← Başvuru takibi
│       │   │   │   ├── CompaniesScreen.kt          ← Şirket listesi
│       │   │   │   ├── CompanySearchScreen.kt      ← Şirket arama
│       │   │   │   ├── DashboardScreen.kt          ← Ana ekran dashboard
│       │   │   │   ├── DocumentManagementScreen.kt ← Belge yönetimi
│       │   │   │   ├── EmailScreen.kt              ← E-posta ekranı
│       │   │   │   ├── EnhancedJobSearchScreen.kt  ← Gelişmiş iş arama
│       │   │   │   ├── PlatformJobSearchScreen.kt  ← Platform iş arama
│       │   │   │   ├── PlatformWebViewScreen.kt    ← WebView platform (YENİ)
│       │   │   │   ├── ProfileScreen.kt            ← Kullanıcı profili
│       │   │   │   ├── SettingsScreen.kt           ← Ayarlar
│       │   │   │   └── SmtpSettingsScreen.kt       ← SMTP ayarları
│       │   │   └── theme/
│       │   │       └── Theme.kt            ← Uygulama teması
│       │   │
│       │   ├── 📂 utils/                   ← YARDIMCI ARAÇLAR
│       │   │   └── AppLogger.kt            ← Logging sistemi
│       │   │
│       │   └── 📂 viewmodels/              ← MVVM VIEWMODELS
│       │       ├── ApplicationTrackingViewModel.kt
│       │       ├── CompanySearchViewModel.kt
│       │       ├── DashboardViewModel.kt
│       │       ├── EmailViewModel.kt
│       │       ├── JobSearchViewModel.kt
│       │       ├── PlatformJobSearchViewModel.kt
│       │       ├── ProfileViewModel.kt
│       │       └── ViewModelFactory.kt     ← ViewModel factory
│       │
│       └── res/                            ← KAYNAK DOSYALARI
│           ├── drawable/                   ← Vektör resimler
│           ├── mipmap-anydpi-v26/          ← Uygulama ikonları
│           ├── values/                     ← Renkler, string, tema
│           └── xml/                        ← XML yapılandırmaları
│
├── build.gradle.kts                        ← Root build dosyası
├── settings.gradle.kts                     ← Proje ayarları
├── gradle.properties                       ← Gradle özellikleri
├── local.properties                        ← Yerel SDK yolu
└── gradlew / gradlew.bat                   ← Gradle wrapper
```

---

# 🔧 TEKNOLOJİ YIGINI (Tech Stack)

| Katman | Teknoloji | Versiyon |
|--------|-----------|----------|
| **Dil** | Kotlin | 1.9.20 |
| **UI** | Jetpack Compose | BOM 2023.10.01 |
| **Tasarım** | Material 3 | compose-bom |
| **Navigasyon** | Navigation Compose | 2.7.5 |
| **Veritabanı** | Room (KSP) | 2.6.1 |
| **Network** | OkHttp | 4.12.0 |
| **JSON** | Kotlinx Serialization | 1.6.0 |
| **Async** | Coroutines | 1.7.3 |
| **HTML Parse** | Jsoup | 1.17.1 |
| **Resim** | Coil | 2.5.0 |
| **Background** | WorkManager | 2.9.0 |
| **Email** | JavaMail | 1.6.7 |
| **AI** | NVIDIA NIM API | OpenAI-uyumlu |
| **Build** | Gradle | 8.6 |
| **JDK** | OpenJDK | 21 |

---

# 🧭 Navigasyon Yapısı (Sayfalar)

```
Bottom Navigation Bar:
├── 🔍 İş Ara (PlatformJobSearch)     ← Varsayılan ana ekran
│   └── → Platform WebView            ← Kariyer.net, LinkedIn, Indeed
├── 🏠 Ana Ekran (Dashboard)
│   ├── → Şirket Ara
│   ├── → E-posta
│   ├── → Başvurular
│   └── → Platform WebView'ler
├── 🏢 Şirketler (Companies)
├── 📧 E-posta (Email)
├── 📋 Başvurular (ApplicationTracking)
└── 👤 Profilim (Profile)

Floating Action Buttons (Dashboard):
├── ✨ AI Asistan (AiAssistant)       ← YENİ
└── ⚙️ Ayarlar (Settings)
    └── → SMTP Ayarları
```

---

# 📜 KONUŞMA GEÇMİŞİ (Tüm Sohbetler)

## 🗓️ 1. Gün - İlk Buluşma
- Kullanıcı: "kanka naber" - Türkçe konuşmak istedi
- Proje zaten başlangıç aşamasındaydı (LockScreen, Jarvis temalı)

## 🗓️ 2. Gün - Proje Analizi
- Proje analiz edildi: JobHunter - İş arama ve otomatik başvuru uygulaması
- Amaç: Kullanıcının arama yaparak iş ilanlarını bulması, seçmesi ve toplu başvuru yapması

## 🗓️ 3. Gün - Hata Düzeltmeleri
- 13 adet build hatası düzeltildi
- AAPT2, Kapt→KSP, AGP güncellemeleri yapıldı
- İlk APK başarıyla derlendi

## 🗓️ 4. Gün - APK Kurulumu
- Xiaomi 15T Pro'ya APK kurulumu yapıldı
- "Bilinmeyen uygulama" izin sorunları çözüldü
- termux-open ile kurulum denendi
- Dosya Yöneticisi ile kurulum başarısı

## 🗓️ 5. Gün - Yeni Özellikler (v2.0.0)
- Ana ekrana platform seçim ekranı eklendi
- Kariyer.net, LinkedIn, Indeed, Google Jobs butonları eklendi
- SMTP e-posta servisi eklendi (JavaMail)
- AI ön yazı servisi eklendi
- Browser-based kurulum sayfası oluşturuldu

## 🗓️ 6. Gün - Scraping Sorunları
- **KRİTİK KEŞİF:** Tüm iş platformları bot koruması kullanıyor
  - Kariyer.net → PerimeterX CAPTCHA
  - Indeed → Cloudflare CAPTCHA
  - LinkedIn → CAPTCHA/Rate limit
- **ÇÖZÜM:** WebView yaklaşımı benimsendi
  - Gerçek platform siteleri uygulama içinde açılacak
  - Kullanıcı gerçek sitede arama yapacak

## 🗓️ 7. Gün - WebView ve API
- PlatformWebViewScreen.kt oluşturuldu (5 platform desteği)
- intent:// scheme hatası düzeltildi
- Dashboard'a gerçek platform butonları eklendi
- APK v2.0.0 build edildi

## 🗓️ 8. Gün - NVIDIA AI Entegrasyonu (v3.0.0)
- NVIDIA NIM API servisi oluşturuldu (NvidiaNimService.kt)
- AiAssistantScreen.kt oluşturuldu (4 sekme)
- AI Asistan navigasyona eklendi
- Build başarılı, APK hazır: /sdcard/Download/JobHunter.apk

---

# 🔑 ÖNEMLİ BİLGİLER

## API Anahtarı Durumu
- **NVIDIA NIM:** `YOUR_NVIDIA_API_KEY_HERE` → Henüz girilmedi
- **SMTP (Gmail):** Henüz yapılandırılmadı
- **Kullanıcı:** Gmail kullanıyor, Google ile giriş yapıyor

## Build Komutları
```bash
cd /mnt/sdcard/FreebuffProject/JobHunter
chmod +x gradlew
bash gradlew assembleDebug --no-daemon
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## APK Konumları
```
/sdcard/Download/JobHunter.apk                    ← Son build (18MB)
/mnt/sdcard/FreebuffProject/JobHunter/app/build/outputs/apk/debug/app-debug.apk
```

## Bilinen Sorunlar
1. ❌ Kariyer.net'te intent:// hatası (uygulama yönlendirmesi)
2. ❌ Scraping çalışmıyor (CAPTCHA engeli) → WebView ile çözüldü
3. ❌ NVIDIA API anahtarı henüz girilmedi
4. ❌ SMTP ayarları henüz yapılandırılmadı
5. ⚠️ Dashboard'daki arama sonuçları halen uydurma veri gösterebilir

---

# 📊 İSTATİSTİKLER

| Metrik | Değer |
|--------|-------|
| Toplam Kotlin Dosyası | 51 |
| Servis Sayısı | 12 |
| Screen Sayısı | 13 |
| ViewModel Sayısı | 8 |
| DAO Sayısı | 6 |
| Model Sayısı | 5 |
| Build Başarı | ✅ Son build başarılı |
| APK Boyutu | 18 MB |
| Toplam Build Denemesi | ~15+ |
| Düzeltilen Hata | 13+ |

---

# ⏭️ SIRADAKİ ADIMLAR

1. 🔑 **NVIDIA API anahtarını gir** → build.nvidia.com'dan al
2. 📧 **SMTP ayarlarını yap** → Gmail uygulama şifresi al
3. 🔧 **Kariyer.net intent:// hatasını düzelt** → WebView intercept
4. 📋 **Toplu başvuru akışını tamamla** → Seç → Ön yazı → Gönder
5. 🎨 **UI/UX iyileştirmeleri** → Daha modern tasarım
6. 🧪 **Test raporları yaz** → Her özellik için test

---

# 💬 KULLANICI BİLGİLERİ

- **Dil:** Türkçe (İngilizce bilmiyor)
- **Telefon:** Xiaomi 15T Pro
- **Gmail:** Google ile giriş yapıyor
- **Laptop:** Serviste (şu an kullanamıyor)
- **Termux:** Telefonunda kurulu

---

*Bu dosya "hatırla" dendiğinde okunacak ana referans dosyasıdır.*
