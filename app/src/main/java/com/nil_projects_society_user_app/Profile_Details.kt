package com.nil_projects_society_user_app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile__details.*

import androidx.core.content.ContextCompat
import android.view.WindowManager



@Suppress("DEPRECATION")
class Profile_Details : AppCompatActivity() {

    lateinit var nameeditText : EditText
    lateinit var emaileditText : EditText
    lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile__details)

        val window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(Profile_Details@this,  R.color.md_blue_custom))

        nameeditText = findViewById<EditText>(R.id.name_extended_edit_text)
        emaileditText = findViewById<EditText>(R.id.email_extended_edit_text)


        btn_SignUp.setOnClickListener {
            passUserDetails()
        }
    }

    private fun passUserDetails() {

      if (nameeditText.text.isEmpty())
        {
            nameeditText.error = "Please Enter Valid Name"
        }else if (emaileditText.text.isEmpty())
        {
            emaileditText.error = "Please Enter Valid Email"
        }else
        {
            var intent = Intent(this,UserProfile_Pic :: class.java)
            intent.putExtra("name",nameeditText.text.toString())
            intent.putExtra("email",emaileditText.text.toString())
            startActivity(intent)
        }
    }
}