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
        @ColumnInfo(name = "first_name") val firstName: String?,
        @ColumnInfo(name = "last_name") val lastName: String?,
        @ColumnInfo(name = "age") val age: Int?,
        @ColumnInfo(name = "gender") val gender: String?,
        @ColumnInfo(name = "weight") val weight: Float?,
        @ColumnInfo(name = "height") val height: Float?,
        @ColumnInfo(name = "blood_group") val bloodGroup: String?,
        @ColumnInfo(name = "allergies") val allergy: String?,
        @ColumnInfo(name = "medical_cond") val medicalCond: String?,
        @ColumnInfo(name = "medication") val medic: String?,

    )

    @Dao
    interface UsersDao {
        @Query("SELECT * FROM User")
        fun getAll(): Flow<List<User>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insert(vararg users: User)

        @Query("DELETE FROM User")
        suspend fun deleteAll()

        @Query("SELECT allergies FROM User")
        fun getAllergies(): Flow<List<String>>

        @Query("SELECT medical_cond FROM User")
        fun getMedicalConditions(): Flow<List<String>>

        @Query("SELECT * FROM User WHERE first_name = :name")
        fun getUserByName(name: String): Flow<List<User>>
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



