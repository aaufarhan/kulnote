//package com.example.kulnote.data.model
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import androidx.room.ForeignKey
//import com.example.kulnote.data.model.MataKuliahEntity
//
//@Entity(
//    tableName = "jadwal_kelas",
//    foreignKeys = [
//        ForeignKey(
//            entity = MataKuliahEntity::class,
//            parentColumns = ["id_matakuliah"],
//            childColumns = ["id_matakuliah_fk"], // Nama kolom FK di JadwalKelas
//            onDelete = ForeignKey.CASCADE // Opsi: Hapus Jadwal jika Mata Kuliah dihapus
//        )
//    ]
//)
//data class JadwalKelasEntity(
//    // Primary Key (PK) untuk jadwal
//    @PrimaryKey(autoGenerate = true)
//    val id_jadwal: Int = 0,
//
//    // Foreign Key (FK) yang menunjuk ke MataKuliahEntity
//    val id_matakuliah_fk: String,
//
//    val hari: String,
//    val jam_mulai: String,
//    val jam_selesai: String,
//    val gedung: String,
//    val ruangan: String
//)