package com.example.guardianangel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MenstruationTable.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "cycletable"
        private const val COLUMN_NAME = "id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_NAME INTEGER PRIMARY KEY AUTOINCREMENT,
                PERIOD_LENGTH INTEGER,
                CYCLE_LENGTH INTEGER,
                LAST_PERIOD_DATE TEXT)
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database schema upgrades here if needed
    }
}
