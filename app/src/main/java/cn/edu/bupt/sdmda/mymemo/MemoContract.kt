package cn.edu.bupt.sdmda.mymemo

import android.provider.BaseColumns
import android.provider.FontsContract

class MemoContract {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "memo.db"
        const val TEXT_TYPE = " TEXT"
        const val COMMA_SEP = " ,"
    }

    class MemoTable {
        companion object {
            const val TABLE_NAME = "memo"
            const val COLUMN_NAME_ID = "id"
            const val COLUMN_NAME_MODTIME = "modtime"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_CONTENT = "content"
            val SQL_CREATE_TABLE = "CREATE TABLE " +
                    "$TABLE_NAME (" +
                    "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT${COMMA_SEP}" +
                    "$COLUMN_NAME_MODTIME ${TEXT_TYPE}${COMMA_SEP} " +
                    "$COLUMN_NAME_TITLE ${TEXT_TYPE}${COMMA_SEP} " +
                    "$COLUMN_NAME_CONTENT $TEXT_TYPE" +
                    ")"
            const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
        }

    }
}

