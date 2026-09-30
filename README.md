# True Darkness

> **Status:** ✅ Berhasil di-build dan berjalan di Minecraft 1.21.5 (Fabric).
> Jar hasil build: `build/libs/truedarkness-1.0.0.jar`

Mod Fabric untuk Minecraft **1.21.5**.

Mod ini membuat malam hari dan tempat-tempat gelap (gua, ruangan tanpa cahaya,
dll.) terasa **jauh lebih gelap** dibanding vanilla - mendekati hitam pekat,
bukan gelap "abu-abu" seperti biasanya. Area yang disinari torch, lava,
glowstone, dsb. tetap menyala normal seperti biasa.

**Bulan dan bintang di malam hari tetap terlihat seperti biasa**, karena
langit (matahari/bulan/bintang) digambar lewat render pass yang sepenuhnya
terpisah dari sistem pencahayaan blok, jadi mod ini tidak menyentuhnya sama
sekali.

## Cara kerja
Setiap frame, Minecraft menghitung sebuah tekstur "lightmap" 16x16 piksel:
satu piksel untuk setiap kombinasi (cahaya blok, cahaya langit) yang mungkin.
Warna tiap blok/entity di layar tinggal diambil dari tekstur ini sesuai level
cahayanya.

`LightmapTextureManagerMixin` menyisipkan kode tepat setelah vanilla selesai
menghitung tekstur itu, tapi sebelum di-upload ke GPU, lalu menggelapkan tiap
piksel dengan kurva gamma:
- Piksel gelap/redup (cahaya minim/tidak ada) → dihancurkan mendekati hitam.
- Piksel paling terang (siang hari terik) → nyaris tidak berubah.

Efek Night Vision tetap dihormati: saat pemain punya efek itu, penggelapan
otomatis dimatikan sehingga Night Vision tetap bekerja seperti vanilla.

## Pengaturan (konstanta di `TrueDarknessClient`)
```java
DARKNESS_GAMMA = 3.2f          // makin besar = makin gelap malam/tempat gelap
RESPECT_NIGHT_VISION = true    // matikan penggelapan saat kena Night Vision
```
Ubah `DARKNESS_GAMMA` sesuai selera:
- `1.0f` = sama seperti vanilla (tidak ada perubahan)
- `~2.0f` = gelap sedang
- `~3.0f - 4.0f` = sangat gelap / nyaris hitam total di tempat tanpa cahaya

## Build
```
./gradlew build      # -> build/libs/truedarkness-1.0.0.jar
./gradlew runClient
```


## Credits
Made by **Geord**. GitHub: https://github.com/clainenxx
Licensed under MIT (see `LICENSE`).
