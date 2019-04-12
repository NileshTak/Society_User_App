package com.nil_projects_society_user_app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.w3c.dom.Text


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            startActivity(Intent(this, SignUp_Mobile::class.java))
        }

        fetchUserDataNAV()

        supportFragmentManager.beginTransaction().replace(R.id.frame_container,HomeFrag()).commit()
        supportActionBar!!.title = "Home"

        val log_out = findViewById<com.getbase.floatingactionbutton.FloatingActionButton>(R.id.log_out)
        log_out.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, SignUp_Mobile ::class.java))
            Toast.makeText(this, "Logged out Successfully :)", Toast.LENGTH_LONG).show()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val holder = findViewById<LinearLayout>(R.id.holder)

        val toggle = object : ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                //this code is the real player behind this beautiful ui
                // basically, it's a mathemetical calculation which handles the shrinking of
                // our content view

                val scaleFactor = 7f
                val slideX = drawerView.width * slideOffset

                holder.setTranslationX(slideX)
                holder.setScaleX(1 - slideOffset / scaleFactor)
                holder.setScaleY(1 - slideOffset / scaleFactor)

                super.onDrawerSlide(drawerView, slideOffset)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)// will remove all possible our aactivity's window bounds
        }

        drawer.addDrawerListener(toggle)

        drawer.setScrimColor(Color.TRANSPARENT)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun fetchUserDataNAV() {
        var navigationView = findViewById<NavigationView>(R.id.nav_view)
        var headerView = navigationView.getHeaderView(0)
        var navUsername = headerView.findViewById<TextView>(R.id.name_user_nav)
        var navUseremail = headerView.findViewById<TextView>(R.id.email_user_nav)

        mAuth = FirebaseAuth.getInstance()

        var userid = mAuth.currentUser?.uid

        val refUser = FirebaseDatabase.getInstance().getReference("/Users/$userid")

        refUser.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var userdata = p0.getValue(UserSocietyClass :: class.java)
                if(userdata != null)
                {
                    navUsername.text = userdata.name
                    navUseremail.text = userdata.email
                }
            }
        })
    }

override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when(id)
        {
            R.id.nav_home -> {
                loadHomeFrag(fragHome = HomeFrag())
            }
            R.id.nav_reports -> {
                loadReportFrag(fragReport = ReportFrag())
            }
//            R.id.nav_profile -> {
//
//            }
//            R.id.nav_complaints -> {
//
//            }
            R.id.nav_notification -> {
                loadNotificationFrag(fragNoti = NotificationFrag())
            }
//            R.id.nav_parking -> {
//
//            }
            R.id.nav_workers -> {
                loadWorkersFrag(fragWorkers = WorkersFrag())
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun loadHomeFrag(fragHome : HomeFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = "Home"
        fm.replace(R.id.frame_container,fragHome)
        fm.commit()
    }

    fun loadReportFrag(fragReport : ReportFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = "Reports"
        fm.replace(R.id.frame_container,fragReport)
        fm.commit()
    }

    fun loadWorkersFrag(fragWorkers : WorkersFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = "Workers"
        fm.replace(R.id.frame_container,fragWorkers)
        fm.commit()
    }

    fun loadNotificationFrag(fragNoti : NotificationFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = "Notifications"
        fm.replace(R.id.frame_container,fragNoti)
        fm.commit()
    }
}
