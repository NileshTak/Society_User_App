package com.nil_projects_society_user_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chaos.view.PinView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.shuhart.stepview.StepView
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.processing_dialog.view.*
import kotlinx.android.synthetic.main.profile_create_dialog.view.*
import org.w3c.dom.Text
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*
import java.util.concurrent.TimeUnit


class Authentication : AppCompatActivity() {

    private var currentStep = 0
    lateinit var layout1: LinearLayout
    lateinit var layout2: LinearLayout
    lateinit var layout3: LinearLayout
    lateinit var stepView: StepView
    lateinit var dialog_verifying: AlertDialog
    lateinit var profile_dialog: AlertDialog
    lateinit var firebaseAuth: FirebaseAuth
    var contactno : String? = null
    lateinit var tvDidntGotCode : TextView
    lateinit var phoneNumber: String
    private var sendCodeButton: Button? = null
    private var verifyCodeButton: Button? = null
    private val signOutButton: Button? = null
    private var button3: Button? = null
    var context : Context = this

    private var phoneNum: EditText? = null
    private var verifyCodeET: PinView? = null
    private var phonenumberText: TextView? = null

    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth



    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        mAuth = FirebaseAuth.getInstance()

        layout1 = findViewById(R.id.layout1) as LinearLayout
        layout2 = findViewById(R.id.layout2) as LinearLayout
        layout3 = findViewById(R.id.layout3) as LinearLayout
        sendCodeButton = findViewById(R.id.submit1) as Button
        verifyCodeButton = findViewById(R.id.submit2) as Button
        button3 = findViewById(R.id.submit3) as Button
        tvDidntGotCode = findViewById<TextView>(R.id.tvDidntGotCode)
        firebaseAuth = FirebaseAuth.getInstance()
        phoneNum = findViewById(R.id.phonenumber) as EditText
        verifyCodeET = findViewById(R.id.pinView) as PinView
        phonenumberText = findViewById(R.id.phonenumberText) as TextView

        val window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(FUllScreenImage@this, R.color.md_blue_custom))

        stepView = findViewById(R.id.step_view) as StepView
        stepView.setStepsNumber(3)
        stepView.go(0, true)
        layout1.visibility = View.VISIBLE


        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                noInt()
            }
        },0,5000)



        sendCodeButton!!.setOnClickListener {
            sendCode()
        }

        tvDidntGotCode.setOnClickListener {
            Toast.makeText(this,"Sending Code Again",Toast.LENGTH_LONG).show()
            sendCode()
        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(Credential: PhoneAuthCredential) {
        //        Toast.makeText(applicationContext,"Verfication Process",Toast.LENGTH_SHORT).show()
                    val inflater = getLayoutInflater()
                    val alertLayout = inflater.inflate(R.layout.processing_dialog, null)
                    val show = AlertDialog.Builder(this@Authentication)
                    show.setView(alertLayout)
                    show.setCancelable(false)
                    dialog_verifying = show.create()
                    dialog_verifying.show()
                signInWithPhoneAuthCredential(Credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(applicationContext, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                Log.d("Failure",e.toString())
            }

            override fun onCodeSent(verificationId: String ,
                                    token: PhoneAuthProvider.ForceResendingToken ) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }



        verifyCodeButton!!.setOnClickListener {
            val verificationCode = verifyCodeET!!.text!!.toString()
            if (verificationCode.isEmpty()) {
                Toast.makeText(this@Authentication, "Enter verification code", Toast.LENGTH_SHORT).show()
            } else {

                val inflater = getLayoutInflater()
                val alertLayout = inflater.inflate(R.layout.processing_dialog, null)
                val show = AlertDialog.Builder(this@Authentication)
                show.setView(alertLayout)
                show.setCancelable(false)
                dialog_verifying = show.create()
                dialog_verifying.show()
                val handler = Handler()
                handler.postDelayed({
                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, verificationCode)
                    signInWithPhoneAuthCredential(credential)
                },5000)

            }
        }

        button3!!.setOnClickListener {
            if (currentStep < stepView.stepCount - 1) {
                currentStep++
                stepView.go(currentStep, true)
            } else {
                stepView.done(true)
            }
            val inflater = getLayoutInflater()
            val alertLayout = inflater.inflate(R.layout.profile_create_dialog, null)
            val show = AlertDialog.Builder(this@Authentication)
            show.setView(alertLayout)
            show.setCancelable(false)
            profile_dialog = show.create()
            profile_dialog.show()
            val handler = Handler()
            handler.postDelayed({
                checkUserAlreadyExistes()
            }, 5000)
        }
    }


    private fun noInt() {
        var netInfo : NetworkInfo? = null
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        netInfo = cm.activeNetworkInfo

        if(netInfo == null)
        {
            Alerter.create(this@Authentication)
                .setTitle("No Internet Connnection")
                .setIcon(R.drawable.noint)
                .setText("Please make sure your device is connected to Internet!!")
                .setBackgroundColorRes(R.color.colorAccent)
                .show()
        }
    }



    private fun checkUserAlreadyExistes() {
        val db = FirebaseFirestore.getInstance()
        contactno = mAuth.currentUser!!.phoneNumber
        Log.d("phone",contactno)
        db.collection("FlatUsers")
            .whereEqualTo("MobileNumber", contactno)
            .get()
            .addOnSuccessListener { documentSnapshot ->
               if(documentSnapshot.isEmpty)
               {
                   profile_dialog.dismiss()
                   val intent = Intent(this@Authentication, Profile_Details::class.java)
                   intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                   startActivity(intent)
                   finish()
               }
                else{
                   profile_dialog.dismiss()
                       Alerter.create(this@Authentication)
                           .setTitle("User")
                           .setIcon(R.drawable.noti)
                           .setDuration(4000)
                           .setText("Successfully Logged In!! :)")
                           .setBackgroundColorRes(R.color.colorAccent)
                           .show()
                   val intent = Intent(this@Authentication, MainActivity::class.java)
                   intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                   startActivity(intent)
                   finish()
               }
            }
            .addOnFailureListener { exception ->
                Log.w("SocietyFirestore", "Failed to Check is user exists or not", exception)
            }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    dialog_verifying.dismiss()
                    if (currentStep < stepView.stepCount - 1) {
                        currentStep++
                        stepView.go(currentStep, true)
                    } else {
                        stepView.done(true)
                    }
                    layout1.visibility = View.GONE
                    layout2.visibility = View.GONE
                    layout3.visibility = View.VISIBLE
                    // ...
                } else {

                    dialog_verifying.dismiss()
                    Toast.makeText(this@Authentication, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                    }
                }
            }
    }

    companion object {
        private val uniqueIdentifier: String? = null
        private val UNIQUE_ID = "UNIQUE_ID"
        private val ONE_HOUR_MILLI = (60 * 60 * 1000).toLong()
        private val TAG = "FirebasePhoneNumAuth"
    }


    private fun sendCode()
    {
        val phoneNumber = "+91" +phoneNum!!.text.toString()
        phonenumberText!!.text = phoneNumber

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNum!!.error = "Enter a Phone Number"
            phoneNum!!.requestFocus()
        } else if (phoneNumber.length < 10) {
            phoneNum!!.error = "Please enter a valid phone"
            phoneNum!!.requestFocus()
        } else {

            if (currentStep < stepView.stepCount - 1) {
                currentStep++
                stepView.go(currentStep, true)
            } else {
                stepView.done(true)
            }

            layout1.visibility = View.GONE
            layout2.visibility = View.VISIBLE

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this@Authentication, // Activity (for callback binding)
                mCallbacks
            )        // OnVerificationStateChangedCallbacks
        }
    }

}
