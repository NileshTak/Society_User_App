package com.nil_projects_society_user_app

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_user_profile__pic.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class UserProfile_Pic : AppCompatActivity() {

    lateinit var spinner_city : Spinner
    lateinit var spinner_societyname : Spinner
    lateinit var spinner_buildingwing : Spinner
    lateinit var spinner_flat : Spinner
    lateinit var btn_addflat : Button
    lateinit var spinner_relation : Spinner
    lateinit var spin_cityvalue : String
    lateinit var spin_societyvalue : String
    lateinit var spin_wingvalue : String
    lateinit var spin_flatvalue : String
    lateinit var spin_relationvalue : String
    lateinit var progressDialog: ProgressDialog
    lateinit var username :String
    lateinit var useremail :String
    var contactno : String? = null
    lateinit var existingFlats : ArrayList<String>


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile__pic)
        supportActionBar!!.title = "User Details"


        val bundle : Bundle? = intent.extras
        username = bundle!!.getString("name")
        useremail = bundle!!.getString("email")

        existingFlats = arrayListOf<String>()
        spinner_city = findViewById<Spinner>(R.id.spinner_city)
        spinner_societyname = findViewById<Spinner>(R.id.spinner_society_name)
        spinner_buildingwing = findViewById<Spinner>(R.id.spinner_building_wing)
        spinner_flat = findViewById<Spinner>(R.id.spinner_flatnum)
        btn_addflat = findViewById<Button>(R.id.btn_add_flat)
        spinner_relation = findViewById<Spinner>(R.id.spinner_relation)

        contactno = FirebaseAuth.getInstance().currentUser!!.phoneNumber

        btn_addflat.setOnClickListener {
            fetchExistingFlatsfromFirebase()
        }

        val optionsCity = arrayOf("Select City", "Pune")

        spinner_city.adapter = ArrayAdapter<String>(this,R.layout.spinner_item,optionsCity)
        spinner_city.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(this@UserProfile_Pic,"Please Select City", Toast.LENGTH_LONG).show()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          //      Toast.makeText(this@UserProfile_Pic,"Selected City is : "+optionsCity.get(position), Toast.LENGTH_LONG).show()
                if(optionsCity.get(position) == "Pune")
                {
                    spinner_societyname.visibility = View.VISIBLE

                    val optionsSocietyName = arrayOf("Select Society", "SMART HOUSING SOCIETY")

                    spinner_societyname.adapter = ArrayAdapter<String>(this@UserProfile_Pic,R.layout.spinner_item,optionsSocietyName)
                    spinner_societyname.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            Toast.makeText(this@UserProfile_Pic,"Please Select Society Name", Toast.LENGTH_LONG).show()
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //            Toast.makeText(this@UserProfile_Pic,"Selected Society is : "+optionsSocietyName.get(position), Toast.LENGTH_LONG).show()
                            if(optionsSocietyName.get(position) == "SMART HOUSING SOCIETY")
                            {
                                spinner_buildingwing.visibility = View.VISIBLE

                                val optionsWing = arrayOf("Select Building", "Madhumalti Building","Aboli Building",
                                    "Nishigandha Building","Sayali Building","Sonchafa Building","Row House")

                                spinner_buildingwing.adapter = ArrayAdapter<String>(this@UserProfile_Pic,R.layout.spinner_item,optionsWing)
                                spinner_buildingwing.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                        Toast.makeText(this@UserProfile_Pic,"Please Select Building Wing", Toast.LENGTH_LONG).show()
                                    }

                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //                    Toast.makeText(this@UserProfile_Pic,"Selected Wing is : "+optionsWing.get(position), Toast.LENGTH_LONG).show()
                                        when(optionsWing.get(position))
                                        {
                                            "Select Building" -> {
                                                spinner_flat.visibility = View.INVISIBLE
                                            }
                                            "Madhumalti Building" -> {
                                                spinner_flat.visibility = View.VISIBLE

                                                val optionsFlat = arrayListOf<String>(
                                                    "Select Flat",
                                                    "101",
                                                    "102",
                                                    "103",
                                                    "104",
                                                    "107",
                                                    "108",
                                                    "203",
                                                    "204",
                                                    "205",
                                                    "206",
                                                    "207",
                                                    "208",
                                                    "303",
                                                    "304",
                                                    "306",
                                                    "307",
                                                    "308",
                                                    "401",
                                                    "402",
                                                    "403",
                                                    "404",
                                                    "406",
                                                    "407",
                                                    "408",
                                                    "501",
                                                    "502",
                                                    "503",
                                                    "504",
                                                    "505",
                                                    "506",
                                                    "601",
                                                    "602",
                                                    "603",
                                                    "606",
                                                    "607",
                                                    "608",
                                                    "105-06",
                                                    "201-02",
                                                    "301-02",
                                                    "305-405",
                                                    "507-08",
                                                    "604-05"
                                                )
                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }
                                            "Row House" -> {
                                                spinner_flat.visibility = View.VISIBLE
                                                val optionsFlat = listOf<String>("Select Row House", "1","2","3","4","5","6","7","8","9","10",
                                                    "11","12","111")

                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }
                                            "Aboli Building" -> {
                                                spinner_flat.visibility = View.VISIBLE
                                                val optionsFlat = listOf<String>("Select Flat", "101","102","103","104",
                                                    "201","202","203","204","301","302",
                                                    "303","304","401","402","403","404","501","502","503","504","601",
                                                    "602","603","604","701","702","703","704")

                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }
                                            "Nishigandha Building" -> {
                                                spinner_flat.visibility = View.VISIBLE
                                                val optionsFlat = listOf<String>("Select Flat", "101","102","103","104",
                                                    "201","202","203/04","301","302","303","304","401","402","403","404",
                                                    "501","502","503","504","601","602","603","604")

                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }
                                            "Sayali Building" -> {
                                                spinner_flat.visibility = View.VISIBLE
                                                val optionsFlat = listOf<String>("Select Flat", "101","102","103","104","105","106","107","108",
                                                    "201","202","203","204","205","206","207","208","303","304","305","306","307","308","401","402","403","404",
                                                    "405","406","407","408",
                                                    "501","502","503","504","506","507","508","601","602","607","608",
                                                    "301/302","505/508","603/604","605/606")

                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }
                                            "Sonchafa Building" -> {
                                                spinner_flat.visibility = View.VISIBLE
                                                val optionsFlat = listOf<String>("Select Flat", "101","102","103","104",
                                                    "201","202","203","204","303","304","301","302","401","402","403","404",
                                                    "501","502","503","504","601","602","603","604")

                                                flatPassData(optionsFlat,optionsWing.get(position))
                                            }

                                        }

                                        spin_wingvalue = optionsWing.get(position)
                                    }
                                }
                            }
                            else
                            {
                                spinner_buildingwing.visibility = View.INVISIBLE
                            }
                            spin_societyvalue = optionsSocietyName.get(position)
                        }
                    }
                }
                else{
                    spinner_societyname.visibility = View.INVISIBLE
                }
                spin_cityvalue = optionsCity.get(position)
            }
        }
    }

    private fun flatPassData(arrayOfFlats : List<String>,wingName : String) {
        spinner_flat.adapter = ArrayAdapter<String>(this@UserProfile_Pic,R.layout.spinner_item,arrayOfFlats)
        spinner_flat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(this@UserProfile_Pic,"Please Select Flat Number", Toast.LENGTH_LONG).show()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            //    Toast.makeText(this@UserProfile_Pic,"Selected Flat is : "+arrayOfFlats.get(position), Toast.LENGTH_LONG).show()
                if(arrayOfFlats.get(position) != "Select Flat")
                {
                    linear_radio.visibility = View.VISIBLE

                    val optionsRelation = arrayOf("Owner", "Rental","Tenants")

                    spinner_relation.adapter = ArrayAdapter<String>(this@UserProfile_Pic,R.layout.spinner_item,optionsRelation)
                    spinner_relation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            Toast.makeText(this@UserProfile_Pic,"Please Select Relation", Toast.LENGTH_LONG).show()
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            spin_relationvalue = optionsRelation.get(position)

                        }
                    }
                }
                spin_flatvalue = arrayOfFlats.get(position)

            }
        }
    }

    @Suppress("DEPRECATION")
    private fun SaveSocietyToFireBase() {
        progressDialog.setMessage("Adding your Society Details")
        var mAuth = FirebaseAuth.getInstance()
        var userID = mAuth.currentUser!!.uid


        val ref = FirebaseDatabase.getInstance().getReference("/Users/$userID")
        val refStore = FirebaseFirestore.getInstance()

        val userAuth = "Pending"

        val items = HashMap<String, Any>()
        items.put("UserName", username)
        items.put("UserID", userID)
        items.put("UserEmail", useremail)
        items.put("City", spin_cityvalue)
        items.put("SocietyName", spin_societyvalue)
        items.put("Wing", spin_wingvalue)
        items.put("FlatNo", spin_flatvalue)
        items.put("UserRelation", spin_relationvalue)
        items.put("userAuth",userAuth)
        items.put("MobileNumber",contactno!!)
        items.put("AlternateMobile","")
        items.put("Profile_Pic_url","https://firebasestorage.googleapis.com/v0/b/notifyapp-58ee3.appspot.com/o/ProfPics%2Fuser_000.png?alt=media&token=f9437bc5-1e64-4755-9116-1cc1b2887b51")

        refStore.collection("FlatUsers").document(userID).set(items).addOnSuccessListener {
//           var intent = Intent(this,MainActivity :: class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            refStore.collection("FlatUsers").document(userID)
                .collection("PaidMonths").document("FirstDoc")
                .set(items).addOnSuccessListener {

                }
                .addOnFailureListener{

                }



        }.addOnFailureListener {
      //          exception: java.lang.Exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
        }

        val usersociety = UserSocietyClass(userID,"",username,useremail,spin_cityvalue,spin_societyvalue,spin_wingvalue,"",spin_flatvalue,spin_relationvalue,userAuth,contactno!!)

        ref.setValue(usersociety)
            .addOnSuccessListener {
                progressDialog.dismiss()
            //    startActivity(intent)
                Toast.makeText(this, "Profile Created Successfully :)", Toast.LENGTH_LONG).show()

                var intent = Intent(this,MainActivity :: class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this,"Failed to Save User Details",Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchExistingFlatsfromFirebase()
    {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Checking Details")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var db = FirebaseFirestore.getInstance()
        db.collection("FlatUsers")
            .whereEqualTo("FlatNo",spin_flatvalue)
            .whereEqualTo("Wing",spin_wingvalue)
            .get()
            .addOnSuccessListener {

                if(it.isEmpty) {
                    SaveSocietyToFireBase()

                }else {

                         progressDialog.dismiss()
                        AlertDialog.Builder(this@UserProfile_Pic)
                            .setMessage("User Alreay Exist!! Please Change Your Details")
                            .setPositiveButton("Ok") { dialog, which ->

                                dialog.cancel()

                            }
                            .show()
                 }
            }

            .addOnFailureListener { exception ->
                Log.w("SocietyFirestore", "Error getting documents.", exception)
            }
    }

}
