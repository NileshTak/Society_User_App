package com.nil_projects_society_user_app

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.onesignal.OneSignal
import com.tapadoo.alerter.Alerter
import de.hdodenhof.circleimageview.CircleImageView
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_edit_prof.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    lateinit var mAuth : FirebaseAuth
    var LoggedIn_User_phone: String? = null
  //  lateinit var waitReq : ImageView
    lateinit var warning_dialog: AlertDialog
//    lateinit var btnLogout : Button
    var netInfo : NetworkInfo? = null
    lateinit var tvNavTitle : TextView
    lateinit var ciNavProfImg : CircleImageView


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(applicationContext, Crashlytics())
        setContentView(R.layout.activity_main)

     //   btnLogout = findViewById<Button>(R.id.btnLogout)
        tvNavTitle = findViewById<TextView>(R.id.tvnavTitle)
        ciNavProfImg = findViewById<CircleImageView>(R.id.navProfImg)
     //   waitReq = findViewById<ImageView>(R.id.imgWait)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()
        var user = mAuth.currentUser

        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.custom_warning_layout, null)
        val show = AlertDialog.Builder(this@MainActivity)
        show.setView(alertLayout)
        show.setCancelable(false)
        warning_dialog = show.create()

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                noInt()
            }
        },0,5000)


        if (user != null) {
            OneSignal.setSubscription(true)
            LoggedIn_User_phone = user!!.phoneNumber
            disableNav()
            OneSignal.sendTag("NotificationID", LoggedIn_User_phone)
        }else{

            startActivity(Intent(this, Authentication::class.java))
            finish()
        }

//        btnLogout.setOnClickListener {
//                mAuth.signOut()
//            OneSignal.setSubscription(false)
//            Alerter.create(this@MainActivity)
//                .setTitle("User")
//                .setIcon(R.drawable.noti)
//                .setDuration(4000)
//                .setText("Successfully Loged Out!! :)")
//                .setBackgroundColorRes(R.color.colorAccent)
//                .show()
//                startActivity(Intent(this, SignUp_Mobile ::class.java))
//                Toast.makeText(this, "Logged out Successfully :)", Toast.LENGTH_LONG).show()
//        }

        askImpPermission()

        supportFragmentManager.beginTransaction().replace(R.id.frame_container,HomeFrag()).commit()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Home"

//        val log_out = findViewById<com.getbase.floatingactionbutton.FloatingActionButton>(R.id.log_out)
//        log_out.setOnClickListener {
//            mAuth.signOut()
//            startActivity(Intent(this, SignUp_Mobile ::class.java))
//            Toast.makeText(this, "Logged out Successfully :)", Toast.LENGTH_LONG).show()
//        }

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

    private fun noInt() {
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        netInfo = cm.activeNetworkInfo

        if(netInfo == null)
        {
            Alerter.create(this@MainActivity)
                .setTitle("No Internet Connnection")
                .setIcon(R.drawable.noint)
                .setText("Please make sure your device is connected to Internet!!")
                .setBackgroundColorRes(R.color.colorAccent)
                .show()
        }
    }

    private fun askImpPermission() {
        askPermission(
            Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_NETWORK_STATE){

        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                }
                // you need to open setting manually if you really need it
                e.goToSettings()
            }
        }
    }


    var flag : Boolean = false

    private fun disableNav() {

        var db = FirebaseFirestore.getInstance()
        var mob = mAuth.currentUser?.phoneNumber
        db.collection("FlatUsers")
            .whereEqualTo("MobileNumber", mob)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val city = documentSnapshot.toObjects(UserSocietyClass :: class.java)
                for (document in city) {
                    if(document!!.userAuth.equals("Pending") || document.userAuth.equals("Rejected"))
                    {
                        frame_container.visibility = View.GONE
                     //   waitReq.visibility = View.VISIBLE

                      flag = true

                        showDialog()
                        Glide.with(applicationContext).load(document.Profile_Pic_url).into(ciNavProfImg)
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        disableNav()
                    }
                    else{
                        flag = false
                        frame_container.visibility = View.VISIBLE
                    //    waitReq.visibility = View.GONE
                        warning_dialog.dismiss()
                        Glide.with(applicationContext).load(document.Profile_Pic_url).into(ciNavProfImg)
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        disableNav()


                        if (FirebaseAuth.getInstance().currentUser?.uid != null)
                        {
                            fetchUserDataNAV()
                        }

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("SocietyFirestore", "Error getting documents.", exception)
            }
    }




    private fun showDialog()
    {
        if (flag == true)
        {

            warning_dialog.show()
        }

    }


    private fun fetchUserDataNAV() {
        var navigationView = findViewById<NavigationView>(R.id.nav_view)
        var headerView = navigationView.getHeaderView(0)
        var navUsername = headerView.findViewById<TextView>(R.id.name_user_nav)
        var navUseremail = headerView.findViewById<TextView>(R.id.email_user_nav)
        var navProfPic = headerView.findViewById<CircleImageView>(R.id.profpic_user_nav)

        if(FirebaseAuth.getInstance().currentUser!!.uid.isNotEmpty())
        {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val db = FirebaseFirestore.getInstance()
            val Ref = db.collection("FlatUsers")
            Ref.whereEqualTo("UserID", userId)
                .get()
                .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val note = documentSnapshot.toObject<UserSocietyClass>(UserSocietyClass::class.java)
                      //  Picasso.get().load(note.Profile_Pic_url).into(navProfPic)
                        Glide.with(applicationContext).load(note.Profile_Pic_url).into(navProfPic)
                        navUseremail.text = note.UserEmail
                        navUsername.text = note.UserName
                    }
                })
        }
        else
        {
            Log.d("LogingOut","Logging Out")
        }

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
        menuInflater.inflate(R.menu.profile_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId)
        {
            R.id.view_prof ->
            {
                var int = Intent(this,EditProf :: class.java)
                startActivity(int)
            }
        }
        return super.onOptionsItemSelected(item)
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
            R.id.nav_complaints -> {
                loadComplaintFrag(fragComplaint = ComplaintsFrag())
            }
            R.id.nav_notification -> {
                loadNotificationFrag(fragNoti = NotificationFrag())
            }
            R.id.nav_maintainance -> {
                loadMaintainanceRecordFrag(fragMaintain = Maintainance_Records())
            }
            R.id.nav_workers -> {
                loadWorkersFrag(fragWorkers = WorkersFrag())
            }

            R.id.nav_logout ->
            {
                mAuth.signOut()
                OneSignal.setSubscription(false)

                val i = Intent(this@MainActivity, Authentication::class.java)

                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)

            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun loadMaintainanceRecordFrag(fragMaintain: Maintainance_Records)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Maintainance Records"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragMaintain)
        fm.commit()
    }

    fun loadHomeFrag(fragHome : HomeFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        tvNavTitle.text = "Home"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragHome)
        fm.commit()
    }

    fun loadReportFrag(fragReport : ReportFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Building Notice"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragReport)
        fm.commit()
    }

    fun loadComplaintFrag(fragComplaint : ComplaintsFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Complaint Box"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragComplaint)
        fm.commit()
    }

    fun loadWorkersFrag(fragWorkers : WorkersFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Workers"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragWorkers)
        fm.commit()
    }

    fun loadNotificationFrag(fragNoti : NotificationFrag)
    {
        val fm = supportFragmentManager.beginTransaction()
        supportActionBar!!.title = ""
        tvNavTitle.text = "Society Notice"
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame_container,fragNoti)
        fm.commit()
    }
}
