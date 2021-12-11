package cn.edu.bupt.sdmda.mymemo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception

class MemoSQLHelper(ctx: Context) :
    SQLiteOpenHelper(ctx, MemoContract.DATABASE_NAME, null, MemoContract.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.run {
            try {
                execSQL(MemoContract.MemoTable.SQL_CREATE_TABLE)
            }catch (e: Exception){
                execSQL(MemoContract.MemoTable.SQL_DELETE_ENTRIES)
                execSQL(MemoContract.MemoTable.SQL_CREATE_TABLE)
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.run {
            execSQL(MemoContract.MemoTable.SQL_DELETE_ENTRIES)
            onCreate(this)
        }
    }

}