package cn.edu.bupt.sdmda.mymemo.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.edu.bupt.sdmda.mymemo.MainActivity
import cn.edu.bupt.sdmda.mymemo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private var sqlHelper: UserSQLHelper? = null
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    @SuppressLint("Recycle")
    private fun initSQL() {
        sqlHelper = UserSQLHelper(this)
        try {
            sqlHelper!!.readableDatabase.query("user",null,null,null,null,null,null)
        } catch(e:Exception) {
            sqlHelper!!.onCreate(sqlHelper!!.readableDatabase) //第一次开启软件要创建表单
            Log.e("helper","table created")
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityLoginBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val remember = binding.checkbox
        initSQL()
        //初始化SharedPreferences文件实现记住密码功能


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
        val editor:SharedPreferences.Editor?  = initSharedPreferences(remember!!,username, password)
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
               password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
//            if (loginResult.success != null) {
//                updateUiWithUser(loginResult.success)
//            }
//            //检查数据库
            val queryR = sqlHelper?.readSQL()
            val _isMatch = queryR?.run {
                isMatch(this,username.text.toString(), password.text.toString())
            }
            if (_isMatch == true){
                rememberUserInfo(remember,editor,username.text.toString(),password.text.toString())
                startMainActivity(username.text.toString())
            }else{
                val _isRegistered =
                    queryR?.let { it1 -> addUser(it1,username.text.toString(), password.text.toString()) }
                if (_isRegistered == true){
                    showPwdError()
                }else{
                    showRegisterSuccess()
                    rememberUserInfo(remember,editor,username.text.toString(),password.text.toString())
                    //跳转
                    startMainActivity(username.text.toString())
                }
            }

        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }



    //----------------------------------------------------Function------------------------------------------------
    private fun rememberUserInfo(checkBox: CheckBox?,editor:SharedPreferences.Editor?,userId: String,userPwd: String) {
        if(checkBox!!.isChecked){
            editor?.putString("account",userId)
            editor?.putString("password",userPwd)
            editor?.putBoolean("isChecked",true)
            editor?.commit()
        }
        else {
            editor?.clear()?.commit()         //之前保存的账户密码进行清空
            //防止通过返回键再次进入登陆页面，此时登陆页面依然保留账户密码。
            //所以销毁活动，再次进入登陆页面时，需要重新输入账号密码。
        }

    }

    private fun initSharedPreferences(checkBox: CheckBox,username:EditText,password:EditText): SharedPreferences.Editor? {
        val sp = getSharedPreferences("PPMemo", MODE_PRIVATE)//获得SharedPreferences，并创建文件名为PPMemo
        val editor = sp.edit() //获得Editor对象，用于储存用户信息
        val _username = sp.getString("account",null)
        val _password = sp.getString("password",null)
        //如果之前记住过密码，直接先导入。
        if(_username !=null && _password !=null){

            username.setText(_username)
            password.setText(_password)
            checkBox.isChecked = sp.getBoolean("isChecked", false)
            loginViewModel.loginDataChanged(_username,_password)//检查输入框状态
        }
        return editor

    }

    //数据库中没有该用户就创建
    private fun addUser(ret: MutableList<Map<String, Any>>,userId:String,userPwd:String): Boolean {
        val _isRegistered = ret.run {
            for(mapItem in this){
                val _isRegistered = mapItem.containsValue(userId)
                if (_isRegistered) return@run true
            }
            false
        }
        return if (!_isRegistered){
            val createTime = System.currentTimeMillis()
            sqlHelper?.addUser(userId,userPwd,createTime)
            false
        }else{
            true
        }

    }
    //判断数据库中是否有该用户和输入框密码与设置密码是否相等
    private fun isMatch(ret: MutableList<Map<String, Any>>,userId:String,userPwd:String): Boolean {
        val _isMatch = ret.run {
            for(mapItem in this){
                val _isRegistered = mapItem.containsValue(userId)
                if (_isRegistered && mapItem.containsValue(userPwd)) return@run true
            }
            false
        }
        return  _isMatch
    }
    private fun startMainActivity(userId: String){
        setResult(Activity.RESULT_OK)
        //跳转到MainActivity
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("userId",userId)
        startActivity(intent)
        //Complete and destroy login activity once successful
        finish()
    }

    private fun showRegisterSuccess(){
        Toast.makeText(applicationContext, "注册成功", Toast.LENGTH_LONG).show()
    }
    private fun showPwdError(){
        Toast.makeText(applicationContext, "密码错误", Toast.LENGTH_LONG).show()
    }
    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}