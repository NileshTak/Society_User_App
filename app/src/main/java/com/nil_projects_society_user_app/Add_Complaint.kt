package com.nil_projects_society_user_app

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add__complaint.*
import kotlinx.android.synthetic.main.custom_complaint_layout.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Add_Complaint : AppCompatActivity() {

    lateinit var dialog_submitted: AlertDialog
    lateinit var currentdate : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add__complaint)
        supportActionBar!!.title = "Add Complaint"
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currentdate = sdf.format(Date())

        submit_complaint_btn.setOnClickListener {
            checkFields()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun updateComplaintOnFirebase(FlatNum : String,UserID : String,Mobile : String,WingName : String)
    {
        var db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("FlatUsers")
            .whereEqualTo("UserID", userid)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                val items = HashMap<String, Any>()
                items.put("CompUserID",UserID)
                items.put("CompFlatNum", FlatNum)
                items.put("CompheadLine", addcomplaint_headline.text.toString())
                items.put("CompDetails", addcomplaint_details.text.toString())
                items.put("CompUpdatedDate", currentdate)
                items.put("CompUserMobileNo", Mobile)
                items.put("CompWingName", WingName)
                items.put("CompProcess", "Under Process")

                    db.collection("FlatUsers").document(userid)
                        .collection("Complaints").document(addcomplaint_headline.text.toString())
                        .set(items).addOnSuccessListener {
                            val inflater = getLayoutInflater()
                            val alertLayout = inflater.inflate(R.layout.compliaintsubmitted_dialog, null)
                            val show = AlertDialog.Builder(this@Add_Complaint)
                            show.setView(alertLayout)
                            show.setCancelable(false)
                            dialog_submitted = show.create()
                            dialog_submitted.show()
                            Handler().postDelayed(
                                {
                                    var int = Intent(this@Add_Complaint,MainActivity :: class.java)
                                    startActivity(int)
                                },
                                3000
                            )
                            sendFCMtoUsers()
                        }.addOnFailureListener {
                                exception: java.lang.Exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                        }
                }
            }

    private fun sendFCMtoUsers() {

        AsyncTask.execute {
            val SDK_INT = android.os.Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
                StrictMode.setThreadPolicy(policy)
                var sendNotificationID: String

                //This is a Simple Logic to Send Notification different Device Programmatically....

                        sendNotificationID = "admin@gmail.com"
                        Log.d("OneSignal App",sendNotificationID)

                        try {
                            val jsonResponse: String

                            val url = URL("https://onesignal.com/api/v1/notifications")
                            val con = url.openConnection() as HttpURLConnection
                            con.setUseCaches(false)
                            con.setDoOutput(true)
                            con.setDoInput(true)

                            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            con.setRequestProperty("Authorization", "Basic Y2Q3ODRhYTUtMjA4ZC00NTZjLTg3MDktMzEwNjJkOWMwMTRi")
                            con.setRequestMethod("POST")

                            val strJsonBody = ("{"
                                    + "\"app_id\": \"69734071-08a8-4d63-a7ab-adda8e2197f0\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"NotificationID\", \"relation\": \"=\", \"value\": \"" + sendNotificationID + "\"}],"

                                    + "\"data\": {\"foo\": \"bar\"},"
                                    + "\"contents\": {\"en\": \"Alert!! Got New Complaint in Complaint Box\"}"
                                    + "}")


                            println("strJsonBody:\n$strJsonBody")

                            val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
                            con.setFixedLengthStreamingMode(sendBytes.size)

                            val outputStream = con.getOutputStream()
                            outputStream.write(sendBytes)

                            val httpResponse = con.getResponseCode()
                            println("httpResponse: $httpResponse")

                            if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                                val scanner = Scanner(con.getInputStream(), "UTF-8")
                                jsonResponse = if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                                scanner.close()
                            } else {
                                val scanner = Scanner(con.getErrorStream(), "UTF-8")
                                jsonResponse = if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                                scanner.close()
                            }
                            println("jsonResponse:\n$jsonResponse")

                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
            }
        }
    }


    private fun checkFields() {
        if(addcomplaint_headline.text.isEmpty())
        {
            addcomplaint_headline.error = "Please Fill Correct Details"
        }
        else if(addcomplaint_details.text.isEmpty())
        {
            addcomplaint_details.error = " Please Fill Correct Details"
        }
        else{
            fetchFlatNum()
        }
    }

    private fun fetchFlatNum() {
        var db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("FlatUsers")
            .whereEqualTo("UserID", userid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.documents.forEach {
                    var city = it.toObject(UserSocietyClass :: class.java)

                     updateComplaintOnFirebase(city!!.FlatNo,userid,city!!.MobileNumber,city!!.wing)
                }
            }
    }
}

