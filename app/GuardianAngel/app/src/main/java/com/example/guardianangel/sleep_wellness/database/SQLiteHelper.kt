package com.example.guardianangel.sleep_wellness.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                "$SLEEP_TIME_COL DATETIME, " +
                "$ALARM_TIME_COL DATETIME, " +
                "$DAILY_JOB_TIME_COL DATETIME " +
                ")"
        println(createTable)
        db.execSQL(createTable)
    }

    fun addData(dbModel: DBModel) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(SLEEP_WELLNESS_COL, if (dbModel.SLEEP_WELLNESS) 1 else 0) // Convert Boolean to INTEGER
        values.put(WAKEUP_PREFERENCE_COL, dbModel.WAKEUP_PREFERENCE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val sleepTimeString = dbModel.SLEEP_TIME?.time.let { dateFormat.format(it) }
        values.put(SLEEP_TIME_COL, sleepTimeString)

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

        // Check if the table exists, and if not, create it
        if (!isTableExists(db, tableName)) {
            createTable(db)
        }

        // Set the values to be updated
//        values.put(SLEEP_WELLNESS_COL, if (dbModel.SLEEP_WELLNESS) 1 else 0) // Convert Boolean to INTEGER
//        values.put(WAKEUP_PREFERENCE_COL, dbModel.WAKEUP_PREFERENCE)
        values.put(SLEEP_WELLNESS_COL, if (dbModel.SLEEP_WELLNESS) 1 else 0)

        if (!dbModel.WAKEUP_PREFERENCE.isNullOrEmpty()) {
            values.put(WAKEUP_PREFERENCE_COL, dbModel.WAKEUP_PREFERENCE)
        }

        val hourFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val sleepTimeString = dbModel.SLEEP_TIME?.time.let { hourFormat.format(it) }
        values.put(SLEEP_TIME_COL, sleepTimeString)

//        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//        val sleepTimeString = dbModel.SLEEP_TIME?.time.let { dateFormat.format(it) }
//        values.put(SLEEP_TIME_COL, sleepTimeString)

//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        val alarmTimeString = dbModel.ALARM_TIME?.time.let { dateFormat.format(it) }
//        values.put(ALARM_TIME_COL, alarmTimeString)
//        val dailyJobTimeString = dbModel.DAILY_JOB_TIME?.time.let { dateFormat.format(it) }
//        values.put(DAILY_JOB_TIME_COL, dailyJobTimeString)

        println("Update values: $values")

        // Update SLEEP_WELLNESS_COL and WAKEUP_PREFERENCE_COL in the last row
        db.update(tableName, values, "$ID = (SELECT MAX($ID) FROM $tableName)", null)
    }
    fun updateAlarmTime(alarmTime: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        if (alarmTime == 0L) {
            values.putNull(ALARM_TIME_COL)
        }
        else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val alarmTimeString = alarmTime.let { dateFormat.format(it) }
            values.put(ALARM_TIME_COL, alarmTimeString)
        }
        Log.d("Update values", values.toString())

        db.update(tableName, values, "$ID = (SELECT MAX($ID) FROM $tableName)", null)
    }

    fun updateDailyJobTime(dailyJobTime: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dailyJobTimeString = dailyJobTime.let { dateFormat.format(it) }

        values.put(DAILY_JOB_TIME_COL, dailyJobTimeString)
        Log.d("Update values", values.toString())

        db.update(tableName, values, "$ID = (SELECT MAX($ID) FROM $tableName)", null)
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
            val alarmTimeIndex = cursor.getColumnIndex(ALARM_TIME_COL)

            if (idIndex != -1 && enableSleepWellnessIndex != -1 && wakeupPreferenceIndex != -1) {
                val enableSleepWellness = cursor.getInt(enableSleepWellnessIndex) == 1
                val wakeupPreference = cursor.getString(wakeupPreferenceIndex)

                val sleepTime = cursor.getString(sleepTimeIndex)?.let { parseDateTime(it) }

                val alarmTime = cursor.getString(alarmTimeIndex)?.let { parseDateTime(it) }

                DBModel(SLEEP_WELLNESS = enableSleepWellness, WAKEUP_PREFERENCE = wakeupPreference, SLEEP_TIME = sleepTime, ALARM_TIME = alarmTime)
            } else {
                null
            }
        } else {
            null
        }
    }

    // Function to parse DATETIME string to Date
    private fun parseDateTime(dateTimeString: String): Date? {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.parse(dateTimeString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
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

    fun deleteLastRow() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $tableName WHERE $ID = (SELECT MAX($ID) FROM $tableName)")
    }


    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.version = oldVersion
    }

    companion object{
        private const val DATABASE_VERSION = 6

        private const val DATABASE_NAME = "GUARDIAN_ANGEL"
        private const val TEST_DATABASE_NAME = "TEST_GUARDIAN_ANGEL"

        const val TABLE_NAME = "USER_SETTINGS"
        const val TEST_TABLE_NAME = "TEST_USER_SETTINGS"

        const val ID = "ID"
        const val SLEEP_WELLNESS_COL = "SLEEP_WELLNESS"
        const val WAKEUP_PREFERENCE_COL = "WAKEUP_PREFERENCE"
        const val SLEEP_TIME_COL = "SLEEP_TIME"
        const val ALARM_TIME_COL = "ALARM_TIME"
        const val DAILY_JOB_TIME_COL = "DAILY_JOB_TIME"
    }
}