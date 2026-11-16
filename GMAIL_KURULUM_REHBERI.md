# 📧 Gmail OAuth Kurulum Rehberi

## 🎯 Ne Yapacağız?
JobHunter uygulamasından doğrudan e-posta gönderebilmek için Gmail API'yi yapılandıracağız.

## ⏱️ Tahmini Süre: 10-15 dakika

---

## Adım 1: Google Cloud Console'a Git

1. **https://console.cloud.google.com** adresine git
2. Google hesabınla giriş yap (kanka hep google ile giriş yapıyorsun ya, aynen öyle)

---

## Adım 2: Yeni Proje Oluştur

1. Sol üstteki proje seçiciye tıkla
2. **"New Project"** (Yeni Proje) butonuna bas
3. Proje adı: **`JobHunter`** yaz
4. **"Create"** butonuna bas
5. Proje oluşunca otomatik olarak o projeye geç

---

## Adım 3: Gmail API'yi Etkinleştir

1. Sol menüden **"APIs & Services"** → **"Library"** tıkla
2. Arama çubuğuna **"Gmail API"** yaz
3. Gmail API'yi bul ve tıkla
4. **"Enable"** (Etkinleştir) butonuna bas
5. Tamamlandı!

---

## Adım 4: OAuth Consent Screen Oluştur

1. **"APIs & Services"** → **"OAuth consent screen"** tıkla
2. **User Type:** **"External"** seç → **"Create"** bas
3. Aşağıdaki bilgileri doldur:

| Alan | Değer |
|------|-------|
| App name | `JobHunter` |
| User support email | `senin@gmail.com` (kendi e-postan) |
| Developer contact | `senin@gmail.com` (kendi e-postan) |

4. **"Save and Continue"** bas
5. **Scopes** ekranında **"Add or Remove Scopes"** tıkla
6. Şu scope'u ekle:
   ```
   https://www.googleapis.com/auth/gmail.send
   ```
7. **"Update"** → **"Save and Continue"** bas
8. **Test Users** ekranında:
   - **"Add Users"** tıkla
   - Kendi Gmail adresini yaz: `senin@gmail.com`
   - **"Add"** bas
9. **"Save and Continue"** bas
10. **"Back to Dashboard"** bas

---

## Adım 5: Credentials (Kimlik Bilgileri) Oluştur

1. **"APIs & Services"** → **"Credentials"** tıkla
2. Üstte **"Create Credentials"** butonuna bas
3. **"OAuth client ID"** seç
4. **Application type:** **"Android"** seç
5. Aşağıdaki bilgileri doldur:

| Alan | Değer |
|------|-------|
| Name | `JobHunter Android` |
| Package name | `com.jobhunter.app` |
| SHA-1 certificate fingerprint | (aşağıdaki komutu çalıştır) |

### SHA-1 Parmak İzi Nasıl Bulunur?

**Eğer laptop'un olsaydı şu komutu çalıştıracaktık:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
```

**Ama telefonda alternatif yol:**
1. Google'da "SHA-1 fingerprint finder android" ara
2. Veya uygulama ilk açıldığında log'da görünür

**Şimdilik şunu yap:**
- **Debug** için: `AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD` (örnek)
- Gerçek fingerprint'i telefonda test ederken buluruz

6. **"Create"** butonuna bas
7. **ÖNEMLİ:** Açılan ekranda şunları görürsün:

```
Client ID: xxxxxxx.apps.googleusercontent.com
Client Secret: GOCSPX-xxxxxxxxx
```

**🚨 BU BİLGİLERİ KOPYALA VE BANA GÖSTER!**

---

## Adım 6: Bana Vermen Gerekenler

Sadece şu iki bilgiyi bana yaz:

```
Client ID: [buraya yapıştır]
Client Secret: [buraya yapıştır]
```

Ben de bunları uygulamaya yerleştiririm!

---

## ⚠️ Önemli Notlar

1. **Test modu:** Uygulamanız "test" modunda olacak, sadece sizin e-posta adresinize gönderim yapabilir
2. **Publish:** Gerçek kullanıma almak için "Publish App" butonuna basman gerekecek (1 hafta sürebilir)
3. **Güvenlik:** Client Secret'ı kimseyle paylaşma!

---

## 🔍 Doğrulama

Kurulumu bitirdikten sonra:
1. Uygulamayı aç
2. Ayarlar → Gmail Hesabı → Bağla
3. Gmail adresini gir
4. "Bağla" butonuna bas
5. Google giriş sayfası açılacak
6. Giriş yap ve izni onayla
7. ✅ "Gmail Bağlı" yazısı görünmeli!

---

## 🆘 Sorun Yaşarsan

| Sorun | Çözüm |
|-------|-------|
| "Redirect URI mismatch" | Package name'i kontrol et |
| "Access denied" | Test user listesini kontrol et |
| "API not enabled" | Gmail API'yi etkinleştirdiğinden emin ol |
| "Invalid client" | Client ID'yi doğru kopyaladığından emin ol |

---

**Hazır olunca bana Client ID ve Client Secret'ı gönder, hemen yerleştireyim! 💪**
