package jp.ikigai.toolbox.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {
    companion object {
        const val DATABASE_NAME = "toolbox.db"
        const val DATABASE_VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            AppUpdateInfo.CREATE_TABLE_QUERY
        )
        db.execSQL(
            ServiceLog.CREATE_TABLE_QUERY
        )
        db.execSQL(
            AppSoundConfig.CREATE_TABLE_QUERY
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}
