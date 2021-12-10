package cn.edu.bupt.sdmda.mymemo

import android.app.Activity
import android.content.Intent
import android.icu.number.IntegerWidth
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import cn.edu.bupt.sdmda.mymemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var binding: ActivityMainBinding? = null
    private var sqlHelper: MemoSQLHelper? = null
    private var memoAdapter: MemoAdapter? = null

    private val requestCodeAdd = 1
    private val requestCodeMod = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        initSQL()
        initView()
    }

    private fun initSQL() {
        sqlHelper = MemoSQLHelper(this)
    }

    private fun initView() {
        binding?.run {
            sqlHelper?.let {
                memoAdapter = MemoAdapter(this@MainActivity, it, R.layout.memo_item)
            }
            listview.adapter = memoAdapter
            listview.onItemClickListener = this@MainActivity
            registerForContextMenu(listview)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menuInflater.inflate(R.menu.main_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        when (item.itemId) {
            R.id.listview_delete -> {
                memoAdapter?.run {
                    deleteData(info.position)
                    notifyDataSetChanged()
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                // Start ContentActivity to add a new note
                ContentActivity.startForResult(this, requestCodeAdd)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Get result from ContentActivity
        if (resultCode != RESULT_OK) {
            return
        }
        // Get title, content, modtime from Intent
        data?.run {
            extras?.run {
                val title = getString(MemoContract.MemoTable.COLUMN_NAME_TITLE)
                val content = getString(MemoContract.MemoTable.COLUMN_NAME_CONTENT)
                val modtime = getLong(MemoContract.MemoTable.COLUMN_NAME_MODTIME)
                memoAdapter?.run {
                    when (requestCode) {
                        requestCodeAdd -> {
                            // if it is a new note, just add it
                            addMemo(title, content, modtime)
                        }
                        requestCodeMod -> {
                            // if it is a modification of old one, get id and update it
                            val id = getInt(MemoContract.MemoTable.COLUMN_NAME_ID)
                            updateMemo(id, title, content, modtime)
                        }
                    }
                    freshData()
                    notifyDataSetChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val bundle = Bundle()
        memoAdapter?.getItem(position)?.let {
            bundle.putString(
                MemoContract.MemoTable.COLUMN_NAME_TITLE,
                "${it[MemoContract.MemoTable.COLUMN_NAME_TITLE]}"
            )
            bundle.putString(
                MemoContract.MemoTable.COLUMN_NAME_CONTENT,
                "${it[MemoContract.MemoTable.COLUMN_NAME_CONTENT]}"
            )
            bundle.putInt(
                MemoContract.MemoTable.COLUMN_NAME_ID,
                it[MemoContract.MemoTable.COLUMN_NAME_ID] as Int
            )
            ContentActivity.startForResult(this, requestCodeMod,bundle)
        }
    }
}