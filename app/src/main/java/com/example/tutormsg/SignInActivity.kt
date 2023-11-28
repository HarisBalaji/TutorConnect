package com.example.tutormsg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutormsg.databinding.SigninActivityBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private lateinit var bind : SigninActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = SigninActivityBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.textView.setOnClickListener{
            val intent = Intent(this,SignUpActivity::class.java)
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

        val sharedPreferences = getSharedPreferences("tutor", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Check if the user is already signed in
        val isSignedIn = sharedPreferences.getBoolean("IS_SIGNED_IN", false)
        if (isSignedIn) {
            // User is signed in, go directly to MainActivity
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            val name = sharedPreferences.getString("NAME","")
            intent.putExtra("name",name)
            startActivity(intent)
            finish() // Finish this activity to prevent going back
        }else {
            bind.button.setOnClickListener {
                val name = bind.emailEt.text.toString()
                val email = bind.emailEt.text.toString()
                val password = bind.passET.text.toString()
                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    val user = User(name, email, password)
                    val authApi = retrofit.create(AuthApi::class.java)
                    val call = authApi.signin(user)
                    call.enqueue(object : Callback<TokenResponse> {
                        override fun onResponse(
                            call: Call<TokenResponse>, response: Response<TokenResponse>
                        ) {
                            if (response.isSuccessful) {
                                val token = response.body()?.token
                                editor.putBoolean("IS_SIGNED_IN", true)
                                editor.putString("NAME", name)
                                editor.apply()
                                val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                intent.putExtra("name", name)
                                startActivity(intent)
                                finish()
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Login Successful!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (response.code() == 401) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "User not registered...",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (response.code() == 402) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Invalid Credentials!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Login Failed!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                            Log.e("hari", "onFailure: ${t.message}", t)
                            Toast.makeText(this@SignInActivity, "Network error", Toast.LENGTH_LONG)
                                .show()
                        }
                    })
                }else{
                    Toast.makeText(this@SignInActivity,"Empty Fields not allowed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}