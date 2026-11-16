# 📧 E-posta Sistemi - Nasıl Çalışır?

## 🎯 Özet
**OAuth veya API key GEREKMEZ!** Cihazınızın kendi e-posta uygulamasını (Gmail, Outlook vb.) kullanır.

---

## 🔄 Akış

```
┌─────────────────────────────────────────────────────────────┐
│  1. JobHunter'da "Toplu Başvuru Yap" butonuna bas         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Her iş ilanı için AI ile farklı ön yazı oluşturulur   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  3. CV + sertifika ekleri hazırlanır                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  4. Cihazın Gmail uygulaması açılır                       │
│     - Alıcı: ✅ hazır                                      │
│     - Konu: ✅ hazır                                       │
│     - İçerik: ✅ hazır (AI ile yazılmış)                  │
│     - Ekler: ✅ hazır (CV + sertifikalar)                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  5. Sende sadece "Gönder" butonuna bas! 🚀               │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Avantajları

| Özellik | OAuth ile | Cihaz Uygulaması ile |
|---------|-----------|----------------------|
| Kurulum süresi | 15-30 dakika | 0 dakika (hemen çalışır) |
| API key gerekli | ✅ Evet | ❌ Hayır |
| Gmail hesabı gerekli | ✅ Evet | ❌ Hayır (her e-posta uygulaması çalışır) |
| Güvenlik | Token yönetimi | Cihazın kendi güvenliği |
| Ek desteği | Sınırlı | ✅ Tam destek |
| Toplu gönderim | Otomatik | Yarım otomatik (her e-posta için onay) |

---

## 📱 Kullanım

### Tek E-posta Gönderimi
1. **E-posta** ekranına git
2. Alıcı, konu ve içeriği doldur
3. **"Gönder"** butonuna bas
4. Gmail uygulaması açılır
5. **"Gönder"** butonuna bas

### Toplu Başvuru
1. **İş Arama** ekranına git
2. "yapay zeka" yaz ve ara
3. 20 ilan seç (checkbox)
4. **📎 Ekler** butonuna bas → CV ve sertifikaları seç
5. **🚀 Toplu Başvuru Yap** butonuna bas
6. Her ilan için ayrı Gmail penceresi açılır
7. Her birinde **"Gönder"** butonuna bas

---

## 🔧 Teknik Detay

### Nasıl Çalışıyor?
```kotlin
// Cihaz uygulamasını açar (OAuth gerektirmez)
val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:")
    putExtra(Intent.EXTRA_EMAIL, arrayOf("hr@techcorp.com"))
    putExtra(Intent.EXTRA_SUBJECT, "Ahmet Yılmaz - İş Başvurusu")
    putExtra(Intent.EXTRA_TEXT, "AI ile yazılmış ön yazı...")
    putExtra(Intent.EXTRA_STREAM, cvUri)  // CV eki
}
context.startActivity(Intent.createChooser(intent, "E-posta gönder"))
```

### Ek Dosyalar Nasıl Eklenir?
```kotlin
// PDF, DOC, resim dosyaları otomatik eklenir
val cvUri = FileProvider.getUriForFile(context, authority, cvFile)
val sertifikaUri = FileProvider.getUriForFile(context, authority, sertifikaFile)

intent.putParcelableArrayListExtra(
    Intent.EXTRA_STREAM, 
    arrayListOf(cvUri, sertifikaUri)
)
```

---

## ⚠️ Dikkat Edilmesi Gerekenler

1. **Her e-posta için onay:** Cihaz uygulaması açıldığında sen "Gönder" butonuna basmalısın
2. **Ek boyutu:** Bazı e-posta sağlayıcıları 25MB üzeri ekleri kabul etmez
3. **İzinler:** İlk kullanımda dosya erişimi izni istenebilir

---

## 🆘 Sorun Yaşarsan

| Sorun | Çözüm |
|-------|-------|
| Gmail açılmıyor | Cihazında Gmail uygulaması kurulu mu? |
| Ekler eklenmiyor | Dosya erişim iznini kontrol et |
| Gönderilemedi | İnternet bağlantısını kontrol et |

---

## 🎯 Sonuç

**Artık OAuth'a ihtiyacın yok!** 
- Cihazının Gmail uygulamasını kullan
- Ekleri otomatik ekle
- Sadece "Gönder" butonuna bas
- Bitti! 🚀

---

**Hazırsan uygulamayı test edelim! 💪**
