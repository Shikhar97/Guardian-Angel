package com.example.guardianangel.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Date

class SQLiteHelper(context: Context, factory: SQLiteDatabase.CursorFactory?, private val tableName: String = TABLE_NAME) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tableName")
        onCreate(db)
    }

    private fun createTable(db: SQLiteDatabase) {
        Log.d("Create table", "Executing create table")
        val createTable = "CREATE TABLE IF NOT EXISTS $tableName (" +
                "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$SLEEP_WELLNESS_COL INTEGER, " + // Assuming it's stored as an integer (0 or 1)
                "$WAKEUP_PREFERENCE_COL TEXT, " + // Assuming it's a text column
                "$SLEEP_TIME_COL DATETIME" +
                ")"
        println(createTable)
        db.execSQL(createTable)
    }

    fun addData(dbModel: DBModel) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(SLEEP_WELLNESS_COL, if (dbModel.SLEEP_WELLNESS) 1 else 0) // Convert Boolean to INTEGER
        values.put(WAKEUP_PREFERENCE_COL, dbModel.WAKEUP_PREFERENCE)
        values.put(SLEEP_TIME_COL, dbModel.SLEEP_TIME?.time)

        println(values)

        // Check if the table exists, and if not, create it
        if (!isTableExists(db, tableName)) {
            createTable(db)
        }

        db.insert(tableName, null, values)
    }

    fun updateData(dbModel: DBModel) {
        val db = this.writableDatabase
        val values = ContentValues()

        // Set the values to be updated
        values.put(SLEEP_WELLNESS_COL, if (dbModel.SLEEP_WELLNESS) 1 else 0) // Convert Boolean to INTEGER
        values.put(WAKEUP_PREFERENCE_COL, dbModel.WAKEUP_PREFERENCE)
        values.put(SLEEP_TIME_COL, dbModel.SLEEP_TIME?.time)

        println(values)

        // Update SLEEP_WELLNESS_COL and WAKEUP_PREFERENCE_COL in the last row
        db.update(tableName, values, "$ID = (SELECT MAX($ID) FROM $tableName)", null)

        Log.d("Update values", values.toString())

        // Check if the table exists, and if not, create it
        if (!isTableExists(db, tableName)) {
            createTable(db)
        }
    }

    fun getLatestData(): DBModel? {
        val db = this.readableDatabase
        if (!isTableExists(db, tableName)) {
            createTable(db)
        }
        val query = "SELECT * FROM $tableName WHERE $ID = (SELECT MAX($ID) FROM $tableName)"
        val cursor: Cursor = db.rawQuery(query, null)

        return if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(ID)
            val enableSleepWellnessIndex = cursor.getColumnIndex(SLEEP_WELLNESS_COL)
            val wakeupPreferenceIndex = cursor.getColumnIndex(WAKEUP_PREFERENCE_COL)
            val sleepTimeIndex = cursor.getColumnIndex(SLEEP_TIME_COL)

            if (idIndex != -1 && enableSleepWellnessIndex != -1 && wakeupPreferenceIndex != -1) {
                val enableSleepWellness = cursor.getInt(enableSleepWellnessIndex) == 1
                val wakeupPreference = cursor.getString(wakeupPreferenceIndex)

                // Assuming SLEEP_TIME_COL is stored as a timestamp (Long)
                val sleepTimeTimestamp = cursor.getLong(sleepTimeIndex)
                val sleepTime = if (sleepTimeTimestamp > 0) Date(sleepTimeTimestamp) else null

                DBModel(SLEEP_WELLNESS = enableSleepWellness, WAKEUP_PREFERENCE = wakeupPreference, SLEEP_TIME = sleepTime)
            } else {
                null
            }
        } else {
            null
        }
    }


    fun isEntryExists(): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $tableName"
        val cursor = db.rawQuery(query, null)
        val entryExists = cursor.moveToFirst()
        cursor.close()
        return entryExists
    }

    // This method is for adding data in our database

    private fun isTableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName))
        val tableExists = cursor.moveToFirst()
        cursor.close()
        return tableExists
    }

    // below method is to get
    // all data from our database
    fun getName(): Cursor? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $tableName", null)

    }

    fun deleteTable(tableName: String) {
        val db = this.readableDatabase
        db.execSQL("DELETE FROM $tableName")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.version = oldVersion
    }

    companion object{
        private const val DATABASE_VERSION = 5

        private const val DATABASE_NAME = "GUARDIAN_ANGEL"
        private const val TEST_DATABASE_NAME = "TEST_GUARDIAN_ANGEL"

        const val TABLE_NAME = "USER_SETTINGS"
        const val TEST_TABLE_NAME = "TEST_USER_SETTINGS"

        const val ID = "ID"
        const val SLEEP_WELLNESS_COL = "SLEEP_WELLNESS"
        const val WAKEUP_PREFERENCE_COL = "WAKEUP_PREFERENCE"
        const val SLEEP_TIME_COL = "SLEEP_TIME"
    }
}