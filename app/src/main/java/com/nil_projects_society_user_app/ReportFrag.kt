package com.nil_projects_society_user_app

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent

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
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.stfalcon.imageviewer.StfalconImageViewer
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_records_layout.*
import kotlinx.android.synthetic.main.custom_records_layout.view.*

import java.text.SimpleDateFormat
import java.util.*


class ReportFrag : Fragment() {

    lateinit var recyclerview_xml_reportfrag : RecyclerView
    var currentUserWing : String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_report, container, false)

        recyclerview_xml_reportfrag = view.findViewById<RecyclerView>(R.id.recyclerview_xml_reportfrag)

        var mAuth = FirebaseAuth.getInstance()
        var userid = mAuth.currentUser!!.uid

        var db = FirebaseFirestore.getInstance()
        db.collection("FlatUsers")
            .whereEqualTo("UserID", userid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                documentSnapshot.documents.forEach {
                    val city = it.toObject(UserSocietyClass::class.java)
                    if (city != null) {
                        currentUserWing = city.Wing
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("SocietyFirestore", "Error getting documents.", exception)
            }

        fetchRecords()

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

    inner class FetchRecordItem(var Finalrecord : RecordClass) : Item<ViewHolder>()
    {
        override fun getLayout(): Int {
            return R.layout.custom_records_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.date_custom_record.text = Finalrecord.date
            viewHolder.itemView.wing_spinner_value_status.text = Finalrecord.wing
            Glide.with(activity).load(Finalrecord.imageUrl).into(viewHolder.itemView.record_img_xml)


            viewHolder.itemView.setOnClickListener {
                var int = Intent(activity,FUllScreenImage :: class.java)
                int.data = Finalrecord.imageUrl.toUri()
                int.putExtra("msg",Finalrecord.date)
                startActivity(int)
            }
        }
    }
}