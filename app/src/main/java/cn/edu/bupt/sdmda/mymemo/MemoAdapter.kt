package cn.edu.bupt.sdmda.mymemo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MemoAdapter(val ctx: Context, val s: MemoSQLHelper, val l: Int, val userId: String) : BaseAdapter() {

    class ViewHolder {
        var vTitle: TextView? = null
        var vModTime: TextView? = null
    }

    var data: MutableList<Map<String, Any>> = mutableListOf()

    init {
        data = readSQL()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Map<String, Any> {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh: ViewHolder
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(ctx).inflate(l, parent, false)
            vh = ViewHolder()
            vh.vTitle = view.findViewById(R.id.memotitle)
            vh.vModTime = view?.findViewById(R.id.modtime)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }
        vh.vTitle?.text = "${data[position][MemoContract.MemoTable.COLUMN_NAME_TITLE]}"
        // Get timestamp from data and format it
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        vh.vModTime?.text =
            df.format(Timestamp("${data[position][MemoContract.MemoTable.COLUMN_NAME_MODTIME]}".toLong()))
        return view
    }

    private fun readSQL(): MutableList<Map<String, Any>> {
        val ret: MutableList<Map<String, Any>> = ArrayList()
        val db: SQLiteDatabase = s.readableDatabase

        // the column we need
        val projection = arrayOf(
            MemoContract.MemoTable.COLUMN_NAME_ID,
            MemoContract.MemoTable.COLUMN_NAME_MODTIME,
            MemoContract.MemoTable.COLUMN_NAME_TITLE,
            MemoContract.MemoTable.COLUMN_NAME_CONTENT
        )
        // how to order the result
        val sortOrder: String = "${MemoContract.MemoTable.COLUMN_NAME_MODTIME} DESC"
        // query and get cursor
        // with can close the resource automatically
        //query用法介绍：https://blog.csdn.net/scorplopan/article/details/6303559?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-4.opensearchhbase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-4.opensearchhbase
        s.readableDatabase.use { d ->
            d.query(
                MemoContract.MemoTable.TABLE_NAME,
                projection,
                "userid=?",
                arrayOf("$userId"),
                null,
                null,
                sortOrder
            ).use { c ->
                // iterate all data and add them to "data"
                while (c.moveToNext()) {
                    val tempData: MutableMap<String, Any> = HashMap()
                    tempData[MemoContract.MemoTable.COLUMN_NAME_TITLE] =
                        c.getString(c.getColumnIndex(MemoContract.MemoTable.COLUMN_NAME_TITLE))
                    tempData[MemoContract.MemoTable.COLUMN_NAME_CONTENT] =
                        c.getString(c.getColumnIndex(MemoContract.MemoTable.COLUMN_NAME_CONTENT))
                    tempData[MemoContract.MemoTable.COLUMN_NAME_MODTIME] =
                        c.getString(c.getColumnIndex(MemoContract.MemoTable.COLUMN_NAME_MODTIME))
                    tempData[MemoContract.MemoTable.COLUMN_NAME_ID] =
                        c.getInt(c.getColumnIndex(MemoContract.MemoTable.COLUMN_NAME_ID))
                    ret.add(tempData)

                }
            }
        }
        return ret
    }

    fun addMemo(t: String?, c: String?, modtime: Long) {
        // construct the key-value data to insert into the database
        val values = ContentValues()
        values.put(MemoContract.MemoTable.COLUMN_NAME_TITLE, t)
        values.put(MemoContract.MemoTable.COLUMN_NAME_USERID, userId)
        values.put(MemoContract.MemoTable.COLUMN_NAME_CONTENT, c)
        values.put(MemoContract.MemoTable.COLUMN_NAME_MODTIME, modtime)
        // INSERT INTO MemoContract.MemoTable.TABLE_NAME (values.KEYS) VALUES (values.VALUES)
        s.writableDatabase.use {
            it.insert(MemoContract.MemoTable.TABLE_NAME, null, values)
        }
    }

    fun updateMemo(id: Int, t: String?, c: String?, modtime: Long) {
        // construct the key-value data to update the database
        val values = ContentValues()
        values.put(MemoContract.MemoTable.COLUMN_NAME_TITLE, t)
        values.put(MemoContract.MemoTable.COLUMN_NAME_CONTENT, c)
        values.put(MemoContract.MemoTable.COLUMN_NAME_MODTIME, modtime)
        val whereClause: String = MemoContract.MemoTable.COLUMN_NAME_ID.toString() + " = ?"
        val whereArgs = arrayOf(id.toString() + "")
        // UPDATE MemoContract.MemoTable.TABLE_NAME SET values.KEYS=values.VALUES WHERE whereClause=whereArgs
        s.writableDatabase.use {
            it.update(MemoContract.MemoTable.TABLE_NAME, values, whereClause, whereArgs)
        }
    }


    fun deleteData(position: Int) {
        // get the id of data first
        val id = data[position][MemoContract.MemoTable.COLUMN_NAME_ID] as Int
        val db: SQLiteDatabase = s.getWritableDatabase()
        // use id to find that row
        val whereClause: String = MemoContract.MemoTable.COLUMN_NAME_ID.toString() + " = ?"
        // NOTE: convert id from int to string
        val whereArgs = arrayOf(id.toString() + "")
        // DELETE FROM MemoContract.MemoTable.TABLE_NAME WHERE selections=selectionArgs
        db.delete(MemoContract.MemoTable.TABLE_NAME, whereClause, whereArgs)
        // remove the data in the list
        data.removeAt(position)
        db.close()
    }

    fun freshData() {
        data = readSQL()
    }
}