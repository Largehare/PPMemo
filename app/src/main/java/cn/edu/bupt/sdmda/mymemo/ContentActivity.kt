package cn.edu.bupt.sdmda.mymemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import cn.edu.bupt.sdmda.mymemo.databinding.ActivityContentBinding

class ContentActivity : AppCompatActivity() {
    var binding: ActivityContentBinding? = null

    // ID of current Content, default is -1
    var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        readInfoFromIntent()
    }


    private fun readInfoFromIntent() {
        // get Intent
        // If Extra of Intent is null, means that we pressed "add" button
        // or we click item in the listview, so we should get data from intent
        if (intent.extras != null) {
            binding?.run {
                actContentTitle.setText(
                    intent.extras?.getString(
                        MemoContract.MemoTable.COLUMN_NAME_TITLE
                    )
                )
                actContentContent.setText(
                    intent.extras!!.getString(
                        MemoContract.MemoTable.COLUMN_NAME_CONTENT
                    )
                )
            }
            id = intent.extras!!.getInt(MemoContract.MemoTable.COLUMN_NAME_ID)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.content_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                // prepare to return data to MainActivity
                // put data into an Intent
                val intent = Intent()
                binding?.run {
                    intent.putExtra(
                        MemoContract.MemoTable.COLUMN_NAME_TITLE,
                        "${actContentTitle.text}"
                    )
                    intent.putExtra(
                        MemoContract.MemoTable.COLUMN_NAME_CONTENT,
                        "${actContentContent.text}"
                    )
                    intent.putExtra(
                        MemoContract.MemoTable.COLUMN_NAME_MODTIME,
                        System.currentTimeMillis()
                    )
                    // if id == -1 means this is a new note
                    // or we modified an old one, so we put _ID in Intent
                    if (-1 != id) {
                        intent.putExtra(MemoContract.MemoTable.COLUMN_NAME_ID, id)
                    }
                    // set result and finish this activity
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        return true
    }

    companion object {
        fun startForResult(act: AppCompatActivity, code: Int, bundle: Bundle? = null) {
            val intent = Intent(act, ContentActivity::class.java)
            bundle?.let { intent.putExtras(it) }
            act.startActivityForResult(intent, code)
        }
    }
}

