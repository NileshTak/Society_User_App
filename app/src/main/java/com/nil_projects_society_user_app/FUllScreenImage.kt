package com.nil_projects_society_user_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.github.chuross.flinglayout.FlingLayout
import kotlin.Unit
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView


class FUllScreenImage : AppCompatActivity() {

    lateinit var fullscreenimg : PhotoView
    lateinit var ivBackArrow : Button
    lateinit var tvMsg : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        ivBackArrow = findViewById<Button>(R.id.ivBackArrow)
        tvMsg = findViewById<TextView>(R.id.tvFullPic)
        fullscreenimg = findViewById<PhotoView>(R.id.fullscreen_image)

        val window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(FUllScreenImage@this, android.R.color.black))

        ivBackArrow.setOnClickListener {
            onBackPressed()
        }

        val bundle: Bundle? = intent.extras

        bundle?.let {
            val msg = bundle.getString("msg")
            if(msg.isNotEmpty())
            {
                tvMsg.text = msg
            }
        }

        var img = intent
        if(img != null)
        {
            var imgUri = img.data
            if(imgUri != null && fullscreenimg != null)
            {
                Glide.with(this).load(imgUri).into(fullscreenimg)
            }
        }
    }
}
