package com.nil_projects_society_user_app

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.tapadoo.alerter.Alerter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_edit_prof.*
import kotlinx.android.synthetic.main.custom_profile_options.view.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

class EditProf : AppCompatActivity() {

    lateinit var recyclerview_profile_option : RecyclerView
    lateinit var progressDialog: ProgressDialog
    lateinit var edRelation : TextView
    lateinit var edAlternate : EditText
    lateinit var btnSave : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_prof)
        supportActionBar!!.title = "Profile"
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        recyclerview_profile_option = findViewById(R.id.recyclerview_option_profile) as RecyclerView
        edRelation = findViewById<TextView>(R.id.tvEditProfRelation)
        edAlternate = findViewById<EditText>(R.id.etAlternateMobile)
        btnSave = findViewById<Button>(R.id.btnEditProfSave)
        loadProfPic()

        btn_selectphoto_imageview_register.setOnClickListener {
            askGalleryPermission()
        }

        btnSave.setOnClickListener {
            var alternateNo = edAlternate.text.toString()
            if(alternateNo.length < 10 && alternateNo.length > 1)
            {
                edAlternate.error = "Please Enter valid Mobile Number"
                Alerter.create(this@EditProf)
                    .setTitle("Profile Details")
                    .setIcon(R.drawable.alert)
                    .setDuration(4000)
                    .setText("Failed to Update!! Please enter valid Alternate Mobile Number.. :)")
                    .setBackgroundColorRes(R.color.colorAccent)
                    .show()
            }
            else{
                progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Wait a Sec....Updating Profile Details")
                progressDialog.setCancelable(false)
                progressDialog.show()
                UploadPictoFirebase(alternateNo)
            }
        }
    }

    private fun askGalleryPermission() {
        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this@EditProf)
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


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadProfPic() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Profile Details")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val adapter = GroupAdapter<ViewHolder>()

        var userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val Ref = db.collection("FlatUsers")
        Ref.whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    val note = documentSnapshot.toObject<UserSocietyClass>(UserSocietyClass::class.java)
                    //Picasso.get().load(note.Profile_Pic_url).into(selectphoto_imageview_register)
                    Glide.with(this).load(note.Profile_Pic_url).into(selectphoto_imageview_register)
                    btn_selectphoto_imageview_register.alpha = 0f
                    name_title.text = note.UserName
                    edRelation.text = note.UserRelation

                    adapter.add(FetchProfileData(note))

                    progressDialog.dismiss()
                }
                recyclerview_profile_option.adapter = adapter
            })
    }

    inner class FetchProfileData(var Finaldata : UserSocietyClass) : Item<ViewHolder>()
    {
        override fun getLayout(): Int {
            return R.layout.custom_profile_options
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.EditProfSocietynameAns.text = Finaldata.SocietyName
            viewHolder.itemView.EditProfWingnameAns.text = Finaldata.wing
            viewHolder.itemView.EditProfFlatNoAns.text = Finaldata.FlatNo
            viewHolder.itemView.EditProfMobileAns.text = Finaldata.MobileNumber
            viewHolder.itemView.EditProfAlterMobileAns.text = Finaldata.AlternateMobile
            viewHolder.itemView.EditProfAuthenAns.text = Finaldata.userAuth
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            btn_selectphoto_imageview_register.alpha = 0f

            val uri = data.data
            compressImage(getRealPathFromURI(uri))
        }
    }


    fun getRealPathFromURI(contentUri: Uri): String {

        // can post image
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(contentUri,
            proj, // WHERE clause selection arguments (none)
            null, null, null)// Which columns to return
        // WHERE clause; which rows to return (all rows)
        // Order-by clause (ascending by name)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(column_index)
    }

    fun compressImage(filePath: String): String {

        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        val maxHeight = 1150.0f
        val maxWidth = 950.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()

            }
        }

        options.inSampleSize = calculateInSampleSize(options,
            actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inDither = false
        options.inScaled = false
        options.inPurgeable = true
        options.inInputShareable = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inTempStorage = ByteArray(16 * 1024)


        try {
            bmp = BitmapFactory.decodeFile(filePath, options)
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
                Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap)
        canvas.matrix = scaleMatrix
        canvas.drawBitmap(bmp, middleX - bmp.width / 2,
            middleY - bmp.height / 2, Paint(
                Paint.FILTER_BITMAP_FLAG)
        )

        bmp.recycle()

        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0)
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                scaledBitmap!!.width, scaledBitmap.height, matrix,
                true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var out: FileOutputStream? = null
        val filename = getFilename()
        try {
            out = FileOutputStream(filename)
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 95, out)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        Log.d("FileName", filename)

        return filename

    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }

    fun getFilename(): String {

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + "/SocietyApp/Society_App_ProfilePics")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        val OutletFname = "ProfileImg-$n.jpg"

        val file = File(myDir, OutletFname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            selectedPhotoUri = Uri.fromFile(file)
            Log.d("FileName", selectedPhotoUri.toString())

            out.flush()
            out.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (file.absolutePath)
    }

    private fun UploadPictoFirebase(Alternate : String) {

        if (selectedPhotoUri == null && Alternate.isEmpty())
        {
            progressDialog.dismiss()
            return
        }
        else{

            val filename = UUID.randomUUID().toString()
            if(selectedPhotoUri != null)
            {
                val ref = FirebaseStorage.getInstance().getReference("/ProfPics/$filename")

                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {
                        Log.d("SocietyUserApp", "Successfully uploaded image: ${it.metadata?.path}")

                        ref.downloadUrl.addOnSuccessListener {
                            saveProfileToFirebaseDatabase(it.toString(),Alternate)
                        }
                    }
                    .addOnFailureListener {
                        Log.d("SocietyUserApp", "Failed to upload image to storage: ${it.message}")
                    }
            }else{
                saveAlternateToFirebaseDatabase(Alternate)
            }
        }
    }

    private fun saveProfileToFirebaseDatabase(profileImageUrl: String,Alternate: String) {
        var userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val Ref = db.collection("FlatUsers")
        Ref.whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    val note = documentSnapshot.toObject<UserSocietyClass>(UserSocietyClass::class.java!!)

                    val bookRef = db.document("FlatUsers/" + documentSnapshot.id)
                    bookRef.update("Profile_Pic_url", profileImageUrl)
               //     Glide.with(this).load(note.Profile_Pic_url).into(selectphoto_imageview_register)
                    btn_selectphoto_imageview_register.alpha = 0f

                    if(Alternate.isNotEmpty())
                    {
                        saveAlternateToFirebaseDatabase(Alternate)
                    }
                    else{
                        progressDialog.dismiss()
                        showAlert()
                        loadProfPic()
                    }
                }
            })
    }

    private fun showAlert() {
        Alerter.create(this@EditProf)
            .setTitle("Profile Update")
            .setIcon(R.drawable.prof)
            .setDuration(4000)
            .setText("Profile Details Updated Successfully..!! :)")
            .setBackgroundColorRes(R.color.colorAccent)
            .show()
    }

    private fun saveAlternateToFirebaseDatabase(Alternate : String) {
        var userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val Ref = db.collection("FlatUsers")
        Ref.whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    val note = documentSnapshot.toObject<UserSocietyClass>(UserSocietyClass::class.java!!)

                    val bookRef = db.document("FlatUsers/" + documentSnapshot.id)
                    bookRef.update("AlternateMobile",Alternate)
             //       Glide.with(this).load(note.Profile_Pic_url).into(selectphoto_imageview_register)

                    btn_selectphoto_imageview_register.alpha = 0f

                    showAlert()
                    progressDialog.dismiss()
                    edAlternate.setText("")
                    loadProfPic()
                }
            })
            .addOnFailureListener {
                Alerter.create(this@EditProf)
                    .setTitle("Profile Details")
                    .setIcon(R.drawable.alert)
                    .setDuration(2000)
                    .setText("Failed to Update!! Please Try after some time!!")
                    .setBackgroundColorRes(R.color.colorAccent)
                    .show()
            }
    }
}
