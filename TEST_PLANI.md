# 🧪 JobHunter Test Planı

## 🎯 Test Amaçları
1. Tüm ekranların doğru çalıştığını doğrula
2. Yeni özelliklerin (İş Arama, Belge Yönetimi) çalıştığını kontrol et
3. E-posta gönderim sisteminin çalıştığını test et
4. Hata durumlarını kontrol et

---

## 📋 Test Senaryoları

### Senaryo 1: Uygulama İlk Açılış ✅
**Beklenen:** Dashboard ekranı açılmalı
1. Uygulamayı aç
2. "🔍 JobHunter" başlığını kontrol et
3. İstatistiklerin "0" gösterdiğini doğrula
4. Bottom nav'ın göründüğünü kontrol et

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 2: Profil Oluşturma ✅
**Beklenen:** Profil bilgileri kaydedilmeli
1. "Profilim" ekranına git (bottom nav)
2. "✏️ Düzenle" butonuna bas
3. Ad: `Ahmet`, Soyad: `Yılmaz` gir
4. Email: `ahmet@email.com` gir
5. Telefon: `+90 555 123 4567` gir
6. "Yetenekler" bölümüne git
7. "➕" butonuna bas
8. "Kotlin" yaz ve ekle
9. "Java" yaz ve ekle
10. "Kaydet" butonuna bas

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 3: Belge Yükleme (YENİ) ✅
**Beklenen:** CV ve sertifika yüklenmeli
1. Ayarlar ekranına git
2. "📁 Belgelerim" bölümüne git (henüz yoksa atla)
3. Veya doğrudan belge yönetim ekranını aç
4. "➕ Belge Ekle" butonuna bas
5. "📄 Özgeçmiş" seçeneğini tıkla
6. Demo CV'yi yükle
7. "🏆 Sertifika" seçeneğini tıkla
8. Demo sertifikayı yükle
9. Varsayılan olarak işaretle

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 4: Akıllı İş Arama (YENİ) ✅
**Beklenen:** Kariyer.net ve diğer kaynaklardan sonuçlar gelmeli
1. "🔍 İş Ara" ekranına git (bottom nav)
2. Arama çubuğuna `yapay zeka` yaz
3. "🔍" butonuna bas
4. "🏢 Kariyer.net" ve "🔍 Google Jobs" kaynaklarının seçili olduğunu doğrula
5. Sonuçların geldiğini kontrol et
6. En az 5 iş ilanı görmeyi bekle

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 5: İş İlanı Seçme (YENİ) ✅
**Beklenen:** İlanlar seçilebilmeli
1. İş arama sonuçlarında
2. İlk 3 ilanın checkbox'ını işaretle
3. "📋 3 iş ilanı seçildi" yazısının göründüğünü doğrula
4. "📎 Ekler" butonunun göründüğünü kontrol et
5. "🚀 Toplu Başvuru Yap" butonunun göründüğünü kontrol et

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 6: Toplu Başvuru Akışı (YENİ) ✅
**Beklenen:** Her ilan için AI ile farklı ön yazı oluşturulmalı
1. 3 iş ilanı seçiliyken
2. "📎 Ekler" butonuna bas
3. CV ve sertifika checkbox'larını işaretle
4. "🚀 Toplu Başvuru Yap" butonuna bas
5. Gmail uygulamasının açıldığını gör
6. Alıcı, konu ve içeriğin hazır olduğunu doğrula
7. "Gönder" butonuna bas
8. Bir sonraki ilan için Gmail'in açıldığını gör
9. Tüm ilanlar için tekrarla

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 7: Popüler Arama (YENİ) ✅
**Beklenen:** Popüler arama butonları çalışmalı
1. İş arama ekranında
2. "🔥 Popüler Aramalar" bölümünü bul
3. "backend developer" butonuna tıkla
4. Arama otomatik yapılmalı
5. Sonuçlar gelmeli

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 8: Part Time Filtresi (YENİ) ✅
**Beklenen:** Part time işler filtrelenmeli
1. İş arama ekranında
2. "⏰ Part Time Aramalar" bölümünü bul
3. "part time yazılım" butonuna tıkla
4. Sonuçlarda "Part Time" etiketinin olduğunu kontrol et

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 9: E-posta Gönderimi (Cihaz Uygulaması) ✅
**Beklenen:** E-posta uygulaması açılmalı
1. "📧 E-posta" ekranına git
2. "Alıcı E-posta": `test@example.com` gir
3. "Konu": `Test Başvurusu` yaz
4. "İçerik": `Bu bir test e-postasıdır` yaz
5. "📤 Gönder" butonuna bas
6. Gmail uygulamasının açıldığını gör
7. Bilgilerin doğru doldurulduğunu doğrula
8. "Gönder" butonuna bas

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 10: Başvuru Takibi ✅
**Beklenen:** Başvurular listelenmeli
1. "📋 Başvurular" ekranına git
2. İstatistiklerin göründüğünü kontrol et
3. Filtre chip'lerinin çalıştığını test et
4. "➕ Yeni Başvuru" butonunun göründüğünü doğrula

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 11: Hata Durumu Testi ✅
**Beklenen:** Hatalar doğru gösterilmeli
1. İş arama ekranında
2. Boş arama yap (hiçbir şey yazmadan "Ara" butonuna bas)
3. "Lütfen bir arama kelimesi girin" mesajının geldiğini gör
4. Çok uzun bir arama yap
5. Sonuçların hala geldiğini kontrol et

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

### Senaryo 12: Ayarlar Ekranı ✅
**Beklenen:** Tüm ayarlar görünmeli
1. "⚙️ Ayarlar" ekranına git
2. "📧 Gmail Hesabı" bölümünü kontrol et
3. "🤖 AI Ayarları" bölümünü kontrol et
4. "💼 LinkedIn Entegrasyonu" bölümünü kontrol et
5. "ℹ️ Hakkında" bölümünü kontrol et

**Durum:** ⬜ Başarılı / ⬜ Başarısız

---

## 📊 Test Sonuçları Özeti

| Senaryo | Durum | Not |
|---------|-------|-----|
| 1. İlk Açılış | ⬜ | |
| 2. Profil Oluşturma | ⬜ | |
| 3. Belge Yükleme | ⬜ | YENİ |
| 4. Akıllı İş Arama | ⬜ | YENİ |
| 5. İlan Seçme | ⬜ | YENİ |
| 6. Toplu Başvuru | ⬜ | YENİ |
| 7. Popüler Arama | ⬜ | YENİ |
| 8. Part Time Filtresi | ⬜ | YENİ |
| 9. E-posta Gönderimi | ⬜ | |
| 10. Başvuru Takibi | ⬜ | |
| 11. Hata Durumu | ⬜ | |
| 12. Ayarlar | ⬜ | |

---

## 🐛 Tespit Edilen Hatalar

| # | Hata | Ekran | Öncelik | Durum |
|---|------|-------|---------|-------|
| 1 | | | | ⬜ Açık |
| 2 | | | | ⬜ Açık |
| 3 | | | | ⬜ Açık |

---

## 📝 Test Notları

```
Test Tarihi: ___________
Test Eden: ___________
Cihaz: ___________
Android Versiyonu: ___________

Notlar:
_________________________________
_________________________________
_________________________________
```

---

## ✅ Test Sonrası Yapılacaklar

1. ⬜ Tüm hataları kaydet
2. ⬜ Kritik hataları düzelt
3. ⬜ Yeniden test et
4. ⬜ GitHub'a push et
5. ⬜ APK oluştur
