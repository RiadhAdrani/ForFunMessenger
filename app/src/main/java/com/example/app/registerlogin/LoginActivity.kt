package com.example.app.registerlogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.app.R
import com.example.app.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            val name_log = username_login.text.toString()
            val pass_log = password_login.text.toString()

            Log.d("LoginActivity", " DEBUGGING ")

            if ( (name_log == null) && (pass_log == null) ) return@setOnClickListener

            Log.d("LoginActivity", "Logged username :" +name_log)
            Log.d("LoginActivity", "Logged with password :$pass_log")



            FirebaseAuth.getInstance().signInWithEmailAndPassword(name_log,pass_log)
                .addOnSuccessListener {
                    val intent_1 = Intent(this, LatestMessagesActivity::class.java)
                    startActivity(intent_1)
                }


        }

        back_to_registration.setOnClickListener {
            // val intent = Intent(this, LoginActivity::class.java)
            // startActivity(intent)

            val intent = Intent(this, RegisterActivity::class.java )
            startActivity(intent)
        }
    }



}