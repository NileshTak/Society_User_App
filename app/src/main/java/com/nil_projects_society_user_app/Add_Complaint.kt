package com.nil_projects_society_user_app

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.tapadoo.alerter.Alerter
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_add__complaint.*
import kotlinx.android.synthetic.main.custom_complaint_layout.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Add_Complaint : AppCompatActivity() {

    lateinit var dialog_submitted: AlertDialog
    lateinit var btnAddImage : ImageView
    lateinit var currentdate : String
    lateinit var img_select_camera : ImageView
    var imageUri  : Uri? = null
    var FinalUri : Uri? = null
    lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add__complaint)
        img_select_camera = findViewById<ImageView>(R.id.img_select_camera)
        btnAddImage = findViewById<ImageView>(R.id.btnAddImage)
        supportActionBar!!.title = "Add Complaint"
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currentdate = sdf.format(Date())

        val bundle: Bundle? = intent.extras
        if(bundle != null)
        {
            var uri = bundle!!.getString("ImageUri")
            imageUri = uri.toUri()
            Log.d("CameraUri",imageUri.toString())
         //   Toast.makeText(this@Add_Complaint,imageUri.toString(),Toast.LENGTH_LONG).show()
            Glide.with(this@Add_Complaint).load(imageUri).into(img_select_camera)

            compress(imageUri!!)
        }

        submit_complaint_btn.setOnClickListener {
            checkFields()
        }

        btnAddImage.setOnClickListener {
            askCameraPermission()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun compress(imageUri: Uri) {
        var auxFile = File(imageUri.path)
        var compressedImageFile = Compressor(this).compressToFile(auxFile)
        Log.d("CameraUri",compressedImageFile.toString())

        FinalUri = Uri.fromFile(compressedImageFile)
        Log.d("CameraUri",FinalUri.toString())
    }


    private fun askCameraPermission() {
        askPermission(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE){
            var int = Intent(UpdateReport@this,Camera2APIScreen::class.java)
            startActivity(int)
        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this@Add_Complaint)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if(e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                }
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }


    private fun updateComplaintOnFirebase(FlatNum : String,UserID : String,Mobile : String,WingName : String,ComplaintImg : String)
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
                items.put("ComplaintImg",ComplaintImg)
                items.put("CompheadLine", addcomplaint_headline.text.toString())
                items.put("CompUpdatedDate", currentdate)
                items.put("CompUserMobileNo", Mobile)
                items.put("CompWingName", WingName)
                items.put("CompProcess", "Under Process")

                    db.collection("FlatUsers").document(userid)
                        .collection("Complaints").document(addcomplaint_headline.text.toString())
                        .set(items).addOnSuccessListener {
                            val inflater = getLayoutInflater()
                            val alertLayout = inflater.inflate(R.layout.compliaintsubmitted_dialog, null)
                            var Successimg = alertLayout.findViewById<ImageView>(R.id.successImg)
                            Glide.with(this).asGif().load(R.drawable.successgif).into(Successimg)
                            val show = AlertDialog.Builder(this@Add_Complaint)
                            show.setView(alertLayout)
                            show.setCancelable(false)
                            dialog_submitted = show.create()
                            progressDialog.dismiss()
                            dialog_submitted.show()
                            showAlert()
                            Handler().postDelayed(
                                {
                                    var int = Intent(this@Add_Complaint,MainActivity :: class.java)
                                    startActivity(int)
                                },
                                4050
                            )
                            sendFCMtoUsers()
                        }.addOnFailureListener {
                  //              exception: java.lang.Exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                        }
                }
            }

    private fun showAlert() {
        Alerter.create(this@Add_Complaint)
            .setTitle("Complaint Box")
            .setIcon(R.drawable.complain)
            .setDuration(4000)
            .setText("Complaint has been sent to Higher Authorities. WIll be updated soon..:)")
            .setBackgroundColorRes(R.color.colorAccent)
            .show()
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
                        sendNotificationID = "manassociety2006@gmail.com"
                        Log.d("OneSignal App",sendNotificationID )

                        try {
                            val jsonResponse: String

                            val url = URL("https://onesignal.com/api/v1/notifications")
                            val con = url.openConnection() as HttpURLConnection
                            con.setUseCaches(false)
                            con.setDoOutput(true)
                            con.setDoInput(true)

                            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            con.setRequestProperty("Authorization", "Basic NzY1N2E5MGEtM2JjZi00MWU3LTg5ZjYtNjg5Y2Y4Nzg2ZTk0")
                            con.setRequestMethod("POST")

                            val strJsonBody = ("{"
                                    + "\"app_id\": \"1a84ca5e-eedd-4f38-9475-8e8c0e78bdfd\","

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

    private fun UploadImgtoFirebase() {
        Log.d("SocietyLogs","Uri is Uplod"+imageUri.toString())
        if(FinalUri == null)
        {
            progressDialog.dismiss()
            Toast.makeText(applicationContext,"Please Select Valid Image & Valid Data",Toast.LENGTH_LONG).show()
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/ComplaintImages/$filename")

        ref.putFile(FinalUri!!)
            .addOnSuccessListener {
          //      Toast.makeText(applicationContext,"Image Uploaded",Toast.LENGTH_LONG).show()
                Log.d("SocietyLogs","Image uploaded")
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()

                    fetchFlatNum(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }

    private fun checkFields() {
        if(addcomplaint_headline.text.isEmpty())
        {
            addcomplaint_headline.error = "Please Fill Correct Details"
        }
//        else if(addcomplaint_details.text.isEmpty())
//        {
//            addcomplaint_details.error = " Please Fill Correct Details"
//        }
        else{
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Wait a Sec....Updating New Complaint")
            progressDialog.setCancelable(false)
            progressDialog.show()
            UploadImgtoFirebase()
        }
    }

    private fun fetchFlatNum(ImageUri: String) {
        var db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("FlatUsers")
            .whereEqualTo("UserID", userid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.documents.forEach {
                    var city = it.toObject(UserSocietyClass :: class.java)

                     updateComplaintOnFirebase(city!!.FlatNo,userid,city!!.MobileNumber,city!!.Wing,ImageUri)
                }
            }
    }
}

