package com.nil_projects_society_user_app

import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SignUp_Mobile : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var disImg : ImageView

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@SignUp_Mobile, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            getWindow().setStatusBarColor(Color.TRANSPARENT)
        }
        setContentView(R.layout.activity_sign_up__mobile)
        disImg = findViewById<ImageView>(R.id.gifTextView)
       // Glide.with(this@SignUp_Mobile).asGif().load(R.drawable.apart).into(disImg)
        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()

        val click = findViewById<Button>(R.id.clickbtn)
        click.setOnClickListener { startActivity(Intent(this@SignUp_Mobile, Authentication::class.java)) }
    }
}
