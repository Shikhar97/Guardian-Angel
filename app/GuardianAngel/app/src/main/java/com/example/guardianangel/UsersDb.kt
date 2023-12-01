package com.example.guardianangel


import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UsersDb {
    @Entity
    data class User(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") val uid: Long = 0L,
        @ColumnInfo(name = "first_name") val heartRate: String?,
        @ColumnInfo(name = "last_name") val respRate: String?,
        @ColumnInfo(name = "age") val nausea: Float?,
        @ColumnInfo(name = "gender") val headache: String?,
        @ColumnInfo(name = "weight") val diarrhea: Float?,
        @ColumnInfo(name = "height") val soarThroat: Float?,
        @ColumnInfo(name = "blood_group") val fever: String?,
        @ColumnInfo(name = "allergies") val muscleAche: String?,
        @ColumnInfo(name = "medical_cond") val lossOfSmellTaste: String?,
        @ColumnInfo(name = "medication") val cough: String?,

    )

    @Dao
    interface UsersDao {
        @Query("SELECT * FROM User")
        fun getAll(): Flow<List<User>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insert(vararg users: User)

        @Query("DELETE FROM User")
        suspend fun deleteAll()
    }

    @Database(entities = [User::class], version = 1, exportSchema = false)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): UsersDao

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        val wordDao = database.userDao()
                        // Delete all content here.
                        wordDao.deleteAll()
                    }
                }
            }
        }

        companion object {
            // Singleton prevents multiple instances of database opening at the
            // same time.
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
                // if the INSTANCE is not null, then return it,
                // if it is, then create the database
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "health.db"
                    )
                        .addCallback(AppDatabaseCallback(scope))
                        .build()
                    INSTANCE = instance
                    // return instance
                    instance
                }
            }
        }
    }
}



