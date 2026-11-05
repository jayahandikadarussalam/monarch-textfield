# Monarch Text Field

Plugin Joget DX8 yang menggabungkan Text Field dan Hidden Field. Nama palette
`Shadow Text Field` dan class `id.co.itasoft.monarch.plugin.MonarchTextField`
dipertahankan agar konfigurasi form lama tetap dapat digunakan.

## Perubahan 8.0.1

- Preview hidden menggunakan `data-cbuilder-element-invisible`, sama dengan Hidden
  Field bawaan Joget DX8. Warna, overlay ikon, dan ukuran mengikuti tema builder;
  CSS abu-abu gelap khusus sudah dihapus.
- Saat form dijalankan, mode hidden hanya merender satu `input type="hidden"`,
  tanpa wrapper, label, atau script pengubah teks.
- Pilihan Hidden Field tersedia di halaman utama properti. Label, placeholder,
  ukuran, batas karakter, Letter Case, Disable Symbols, dan readonly label
  disembunyikan dari editor saat tidak berlaku untuk mode hidden.
- Atribut input dibersihkan, `size`/`maxlength` kosong dihilangkan, dan input error
  dihubungkan ke pesan error melalui ARIA. Tampilan readonly mengikuti status
  efektif Joget, termasuk readonly dari parent dan Always Use Default Value.
- Disable Symbols bekerja tanpa memilih Letter Case. Event dibatasi ke instance
  render masing-masing agar field/subform tidak saling memengaruhi. Script numeric
  dibagikan antar-field dan parameter JavaScript di-escape.
- Nilai yang diketik tidak tertimpa default saat form ditampilkan ulang setelah
  validasi gagal. Validasi numeric berulang mempertahankan nilai asli untuk
  ditampilkan kembali, dan default numeric tidak tertukar dengan nilai request.
- Penyimpanan numeric Euro menggunakan titik desimal (`1.234,56` menjadi
  `1234.56`). Nilai readonly tidak dinormalisasi ulang. Cache validasi memakai weak
  keys agar request yang sudah selesai tidak terus ditahan oleh plugin.
- Build menggunakan UTF-8 dan Java release 11, dengan versi build plugin tetap.

## Perilaku pengaturan

- Data Priority tetap memiliki tiga pilihan: default lebih dahulu, data tersimpan
  lebih dahulu, dan selalu gunakan default. Input pengguna yang sudah dikirim
  dipertahankan saat redisplay, kecuali Always Use Default Value.
- Letter Case dan Disable Symbols berlaku pada input teks yang dapat diedit.
  Disable Symbols mempertahankan perilaku ASCII lama: huruf A-Z/a-z, angka, dan
  whitespace. Opsi ini dilewati ketika numeric formatting aktif agar pemisah
  desimal dan mata uang tidak terhapus.
- Untuk angka pada hidden field dan Always Use Default Value, gunakan bentuk
  database seperti `1234.56` agar tidak ambigu. Bentuk Euro dengan koma desimal
  seperti `1.234,56` juga didukung. `1.234` pada kedua mode ini dianggap angka
  desimal, bukan pemisah ribuan.
- ID field dan nama properti lama tetap sama; form lama tidak perlu dibuat ulang.

## Build dan verifikasi

```sh
mvn clean verify
```

JDK 11 atau lebih baru dan dependency Joget `8.0-SNAPSHOT` harus tersedia di Maven.
Hasil build: `target/monarch-textfield-8.0.1.jar`.

Pengujian otomatis mencakup template FreeMarker, metadata builder, escaping,
readonly, default, request submission, serta format dan penyimpanan numeric.
Pengujian ini belum menggantikan pemeriksaan pada server Joget dengan tema Anda.

Setelah upload JAR melalui Manage Plugins, reload Form Builder dan cek:

1. Hidden aktif: `plant_name` memakai overlay dan layout hidden bawaan seperti
   `field6`; toolbar edit/pindah/hapus tetap bekerja.
2. Hidden nonaktif: `parent_id` tampil sebagai text field mengikuti tema form.
3. Jalankan form: hidden tidak menyisakan ruang kosong dan nilainya tetap tersimpan.
4. Edit record lama, kirim form dengan error di field lain, lalu periksa nilai,
   readonly, default, dan numeric sesuai konfigurasi form.

Penanda preview mengikuti implementasi resmi [Form Builder Joget DX8](https://github.com/jogetworkflow/jw-community/blob/8.0-SNAPSHOT/wflow-consoleweb/src/main/webapp/js/fbuilder.core.js)
dan [CSS helper canvas](https://github.com/jogetworkflow/jw-community/blob/8.0-SNAPSHOT/wflow-consoleweb/src/main/webapp/builder/editor-helpers.css).
