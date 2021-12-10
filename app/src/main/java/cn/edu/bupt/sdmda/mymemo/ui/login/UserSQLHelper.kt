package cn.edu.bupt.sdmda.mymemo.ui.login

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cn.edu.bupt.sdmda.mymemo.MemoContract
import java.util.ArrayList
import java.util.HashMap

class UserSQLHelper(ctx: Context) :
    SQLiteOpenHelper(ctx, MemoContract.DATABASE_NAME, null, MemoContract.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.run {
            execSQL(MemoContract.UserTable.SQL_CREATE_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.run {
            execSQL(MemoContract.UserTable.SQL_DELETE_ENTRIES)
            onCreate(this)
        }
    }
    private fun readSQL(): MutableList<Map<String, Any>> {
        val ret: MutableList<Map<String, Any>> = ArrayList()
        val db: SQLiteDatabase = this.readableDatabase

        // the column we need
        val projection = arrayOf(
            MemoContract.UserTable.COLUMN_NAME_USERID,
            MemoContract.UserTable.COLUMN_NAME_PWD,
        )
        // how to order the result
        val sortOrder: String = "${MemoContract.UserTable.COLUMN_NAME_USERID} DESC"
        // query and get cursor
        // with can close the resource automatically
        this.readableDatabase.use { d ->
            d.query(
                MemoContract.UserTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
            ).use { c ->
                // iterate all data and add them to "data"
                while (c.moveToNext()) {
                    val tempData: MutableMap<String, Any> = HashMap()
                    tempData[MemoContract.UserTable.COLUMN_NAME_USERID] =
                        c.getString(c.getColumnIndex(MemoContract.UserTable.COLUMN_NAME_USERID))
                    tempData[MemoContract.UserTable.COLUMN_NAME_PWD] =
                        c.getString(c.getColumnIndex(MemoContract.UserTable.COLUMN_NAME_PWD))
                    ret.add(tempData)
                }
            }
        }
        return ret
    }
    fun addUser(t: String?, c: String?, createtime: Long) {
        // construct the key-value data to insert into the database
        val values = ContentValues()
        values.put(MemoContract.UserTable.COLUMN_NAME_USERID, t)
        values.put(MemoContract.UserTable.COLUMN_NAME_PWD, c)
        values.put(MemoContract.UserTable.COLUMN_NAME_CREATETIME, createtime)
        // INSERT INTO MemoContract.MemoTable.TABLE_NAME (values.KEYS) VALUES (values.VALUES)
        this.writableDatabase.use {
            it.insert(MemoContract.UserTable.TABLE_NAME, null, values)
        }
    }
}