package com.absensi.jamaah

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

@Entity(tableName = "jamaah")
data class Jamaah(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val kelas: String,
    val alamat: String
)

@Entity(tableName = "absensi")
data class Absensi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jamaahId: Int,
    val tanggal: String, // Format: YYYY-MM-DD
    val waktuShalat: String, // Subuh, Dzuhur, Ashar, Maghrib, Isya
    val status: String, // Mengikuti, Ijin, Tidak Mengikuti, Telat
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AppDao {
    @Query("SELECT * FROM jamaah ORDER BY nama ASC")
    fun getAllJamaah(): Flow<List<Jamaah>>

    @Insert
    suspend fun insertJamaah(jamaah: Jamaah)

    @Delete
    suspend fun deleteJamaah(jamaah: Jamaah)

    @Query("SELECT * FROM absensi WHERE tanggal = :tanggal AND waktuShalat = :waktuShalat")
    suspend fun getAbsensiByTanggalWaktu(tanggal: String, waktuShalat: String): List<Absensi>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsensiList(absensi: List<Absensi>)

    @Query("SELECT * FROM absensi")
    fun getAllAbsensi(): Flow<List<Absensi>>

    @Query("DELETE FROM jamaah")
    suspend fun clearJamaah()

    @Query("DELETE FROM absensi")
    suspend fun clearAbsensi()
}

@Database(entities = [Jamaah::class, Absensi::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absensi_database"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Dummy Data
                        Executors.newSingleThreadExecutor().execute {
                            val dao = getDatabase(context).appDao()
                            val dummies = listOf(
                                Jamaah(nama = "Ahmad", kelas = "A", alamat = "Pondok 1"),
                                Jamaah(nama = "Muhammad Rizky", kelas = "A", alamat = "Pondok 2"),
                                Jamaah(nama = "Fajar", kelas = "B", alamat = "Pondok 1"),
                                Jamaah(nama = "Ilham", kelas = "C", alamat = "Pondok 3"),
                                Jamaah(nama = "Bagas", kelas = "B", alamat = "Pondok 2")
                            )
                            dummies.forEach { 
                                val query = "INSERT INTO jamaah (nama, kelas, alamat) VALUES ('${it.nama}', '${it.kelas}', '${it.alamat}')"
                                db.execSQL(query)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
