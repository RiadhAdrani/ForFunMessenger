package com.example.app.registerlogin

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.widget.Toast
import com.example.app.R
import com.example.app.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize

import kotlinx.android.synthetic.main.content_main.*
import java.util.*



class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            performRegister()
        } 
        login_text_register.setOnClickListener {
            Log.d("RegisterActivity","Show something in logs")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        insert_image_button.setOnClickListener {
            Log.d("RegisterActivity","Try to show photo")

            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectPhotoUri: Uri?= null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data !=null){
            Log.d("RegisterActivity", "Photo was Selected" )

            selectPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectPhotoUri)


            SelectPhoto_CircularImageView.setImageBitmap(bitmap)

            insert_image_button.alpha = 0f

            //insert_image_button
            // val bitmapDrawable = BitmapDrawable(bitmap)
            //   insert_image_button.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister(){
        val email = mail_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val username = username_edittext_register.text.toString()

        Log.d("RegisterActivity","Email is:" +email);
        Log.d("RegisterActivity","Username is:" +username);
        Log.d("RegisterActivity", "Password is :$password")

        if (email.isEmpty()|| password.isEmpty()){
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
        else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    // else if successful
                    Log.d("RegisterActivity", "Successfully created user with uid : ${it.result.user.uid} ")
               uploadImageToFirebaseStorage()
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create Account", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if ( selectPhotoUri == null)return // selectedPhotoUri

        val filename = UUID.randomUUID().toString()
        val ref= FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectPhotoUri!!) // selectedPhotoUri

            .addOnSuccessListener{
                Log.d("RegisterActivity","Image Uploaded ")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity","File Location $it")

                    saveUserToFirebaseDataBase(it.toString())
                }
            }

            .addOnFailureListener{
              // do some logging
            }
    }

    private fun saveUserToFirebaseDataBase(profileImageUrl:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)


        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Fucking , Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to save user to firebase databaese")
            }
    }
}

@Parcelize
 class User(val uid:String, val username:String, val profileimageUrl:String): Parcelable{
    constructor():this("","","")
}


