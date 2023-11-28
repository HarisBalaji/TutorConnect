package com.example.tutormsg

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Log.DEBUG
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutormsg.BuildConfig.DEBUG
import com.example.tutormsg.databinding.SignupActivityBinding
import com.google.android.material.tabs.TabLayout.TabGravity
import okhttp3.OkHttpClient

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {
    private lateinit var bind: SignupActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = SignupActivityBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as needed
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://tutor-mark-msg-api.onrender.com/api/") // Replace with your server's IP
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bind.button.setOnClickListener {
            val name = bind.nameET.text.toString()
            val email = bind.emailEt.text.toString()
            val password = bind.passET.text.toString()
            val conPass = bind.confirmPassEt.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && conPass.isNotEmpty()) {
                if (email.endsWith("@tce.edu")) {
                    if (password == conPass) {
                        val user = User(name, email, password)
                        val authApi = retrofit.create(AuthApi::class.java)
                        val call = authApi.signup(user)
                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                    intent.putExtra("name", name)
                                    startActivity(intent)
                                    finish()
                                    Toast.makeText(this@SignUpActivity, "Success", Toast.LENGTH_LONG).show()
                                } else if (response.code() == 409) {
                                    Toast.makeText(this@SignUpActivity, "Email already registered...", Toast.LENGTH_LONG).show()
                                } else if (response.code() == 410) {
                                    Toast.makeText(this@SignUpActivity, "Username not available...", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this@SignUpActivity, "Internal Server Error", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Log.e("hari", "onFailure: ${t.message}", t)
                                Toast.makeText(this@SignUpActivity, "Network Error...", Toast.LENGTH_LONG).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@SignUpActivity, "Password doesn't match", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "Email Domain not allowed", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@SignUpActivity, "Empty fields not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}