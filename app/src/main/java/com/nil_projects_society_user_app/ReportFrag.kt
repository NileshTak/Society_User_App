package com.nil_projects_society_user_app

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_records_layout.*
import kotlinx.android.synthetic.main.custom_records_layout.view.*
import kotlinx.android.synthetic.main.fragment_report.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class ReportFrag : Fragment() {

    lateinit var img_select_camera: Button
    lateinit var spinner_wing : Spinner
    lateinit var recyclerview_xml_reportfrag : RecyclerView
    val REQUEST_PERM_WRITE_STORAGE = 102
    lateinit var datePickerdialog : DatePickerDialog
    private val CAPTURE_PHOTO = 104
    internal var imagePath: String? = ""
    var formate = SimpleDateFormat("dd MMM, yyyy",Locale.US)
    lateinit var progressDialog: ProgressDialog
    var counter : Long = 0
    var spin_value : String = "Wing"
    var currentUserWing : String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        var mAuth = FirebaseAuth.getInstance()
        var userid = mAuth.currentUser!!.uid
        val refWing = FirebaseDatabase.getInstance().getReference("/Users/$userid")
        refWing.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
              val wingData = p0.getValue(UserSocietyClass :: class.java)
                currentUserWing = wingData!!.wing
            }
        })

        fetchRecords()

        val view = inflater.inflate(R.layout.fragment_report, container, false)

        recyclerview_xml_reportfrag = view.findViewById<RecyclerView>(R.id.recyclerview_xml_reportfrag)

        return view
    }

    private fun fetchRecords() {
        val ref = FirebaseDatabase.getInstance().getReference("/RecordsDates")

        var recordsorder = ref.orderByChild("counter")
        ref.addListenerForSingleValueEvent(object : ValueEventListener
        {
            val adapter = GroupAdapter<ViewHolder>()

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    recordsorder
                    val record = it.getValue(RecordClass::class.java)

                    if (record != null && record.wing == currentUserWing) {
                        adapter.add(FetchRecordItem(record))
                    }
                }
                recyclerview_xml_reportfrag.adapter = adapter
                }
            })
        }
    }



class RecordClass(val id : String,val date: String,val imageUrl : String,val counter : String,val wing : String)
{
    constructor() : this("","","","","")
}

class FetchRecordItem(var Finalrecord : RecordClass) : Item<ViewHolder>()
{
    override fun getLayout(): Int {
        return R.layout.custom_records_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.date_custom_record.text = Finalrecord.date
        viewHolder.itemView.wing_spinner_value_status.text = Finalrecord.wing
        Picasso.get().load(Finalrecord.imageUrl).into(viewHolder.itemView.record_img_xml)

    }
}