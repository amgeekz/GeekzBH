# LimitlessCharge ⚡🔋
**Utilitas Pengontrol Pengisian Daya & Bypass Batas Arus Sistem Android (Root)**

LimitlessCharge adalah aplikasi utilitas Android modern yang dirancang untuk perangkat Android yang di-root guna mengontrol proses pengisian daya secara presisi, memantau kesehatan baterai, meningkatkan batas arus pengisian dasar, dan melindungi baterai dari panas berlebih.

---

## 🎨 Logat & Desain Visual
Aplikasi ini hadir dengan antarmuka pengguna berbasis **Jetpack Compose** dengan desain yang sangat bersih (*clean*), bertema gelap intens (*Deep Space/Dark Charcoal*), dengan aksen cyan berpendar (*Glow Cyan*) untuk mengesankan kekuatan teknologi pengisian instan yang kuat tanpa batas.

---

## 🚀 Fitur Utama

### 1. Charger Limits Bypass Mod (Bypass Batas Arus)
*   **Melampaui Batas Standar Pabrik:** Melewati kontrol bawaan pabrikan ponsel (misalnya batas quick charger Xiaomi standar 2840mA) untuk memaksa daya pengisian hingga **4000mA** secara aman.
*   **Modifikasi Node Kernel:** Berinteraksi langsung dengan node pengisian daya sistem seperti `charge_current_max` dan `fast_charge` untuk membuka potensi maksimal pengisian daya baterai Anda.

### 2. Batas Pengisian Pintar (State-of-the-art Limit Screen)
*   Atur batas pengisian daya otomatis (misalnya menghentikan pengisian pada persentase **80%**) untuk memperpanjang umur sel baterai secara drastis (*battery longevity*).
*   Memiliki logika deteksi cerdas untuk mengembalikan kontrol kelistrikan kembali ke standar saat mode bypass dimatikan.

### 3. Pemantau Kondisi Baterai Presisi (Advanced Diagnostics)
Visualisasi waktu nyata untuk berbagai indikator fisik baterai:
*   **Arus Pengisian (mA):** Memantau masuk/keluar arus listrik secara instan.
*   **Daya Aktif (Watt):** Menghitung pengiriman daya secara real-time.
*   **Suhu Baterai (°C):** Pemantauan termal konstan untuk mencegah panas berlebih.
*   **Informasi Lanjutan:** Siklus Baterai (*Cycle Count*), Teknologi Baterai, Nilai Kapasitas Maksimal Pembacaan, Hambatan Tegangan (mV), dan Status Kesehatan.

### 4. Pusat Kontrol Kesehatan & Efisiensi (Settings Panel)
*   **Thermal Cutoff:** Menghentikan proses pengisian secara otomatis jika suhu mencapai tingkat kritis.
*   **CPU Power Saving:** Menurunkan clock CPU secara dinamis selama pengisian daya berlangsung untuk mengurangi panas tambahan yang dihasilkan prosesor demi pengisian yang lebih efisien dan dingin.
*   **Battery Calibration:** Membantu sistem mengkalibrasi ulang file statistik baterai (`batterystats.bin`) agar persentase baterai terbaca lebih akurat.

---

## 🛠️ Persyaratan Sistem
Aplikasi ini memerlukan akses administratif tingkat sistem untuk dapat menulis ke node kernel:
*   **Root Access:** Berjalan optimal menggunakan Magisk, KernelSU, atau APatch.
*   **Sistem File `/sys` yang Dapat Ditulis:** Memerlukan akses ke file kernel power supply seperti:
    *   `/sys/class/power_supply/battery/charging_enabled`
    *   `/sys/class/power_supply/battery/input_suspend`
    *   `/sys/class/power_supply/battery/fast_charge`
    *   `/sys/class/power_supply/battery/charge_current_max`

---

## 🔧 Pengembangan & Build
Aplikasi dirancang dengan teknologi pengembangan Android terbaru:
*   **Bahasa Pemrograman:** Kotlin 100% dengan modern Jetpack Compose.
*   **Konfigurasi Gradle:** Kotlin DSL (`.gradle.kts`) dengan Gradle Tooling versi terbaru.
*   **CI/CD:** Pipelines pengujian terintegrasi menggunakan GitHub Actions untuk perilisan berkas `.apk` secara otomatis.
