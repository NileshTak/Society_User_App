package com.nil_projects_society_user_app

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Pair
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_add__complaint.*
import kotlinx.android.synthetic.main.custom_complaint_layout.view.*


class ComplaintsFrag : Fragment() {

    lateinit var btn_addComp: Button
    lateinit var recyclerlist_complaint : RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_complaints, container, false)

        btn_addComp = view.findViewById<Button>(R.id.btn_addComplaint)
        recyclerlist_complaint = view.findViewById<RecyclerView>(R.id.complaints_recyclerview)

        fetchComplaintOnFirebase()
        btn_addComp.setOnClickListener {
            var int = Intent(activity, Add_Complaint::class.java)
            startActivity(int)
        }

        return view
    }

    private fun fetchComplaintOnFirebase()
    {
        var db = FirebaseFirestore.getInstance()
        var userid = FirebaseAuth.getInstance().currentUser!!.uid

                db.collection("FlatUsers").document(userid)
                    .collection("Complaints")
                    .get()
                .addOnSuccessListener {
                    val adapter = GroupAdapter<ViewHolder>()
                    val city = it.toObjects(ComplaintClass::class.java)
                    for (document in city) {
                        if (document != null) {
                            adapter.add(customLayoutComplaint(document))
                        }

                        runAnimation(recyclerlist_complaint,2)
                        recyclerlist_complaint.adapter = adapter
                        recyclerlist_complaint.adapter!!.notifyDataSetChanged()
                        recyclerlist_complaint.scheduleLayoutAnimation()
                    }
                    }.addOnFailureListener {
                           Toast.makeText(context,it.toString(), Toast.LENGTH_LONG).show()
                    }
            }

    private fun runAnimation(recyclerlist_complaint: RecyclerView?, type : Int) {
        var context = recyclerlist_complaint!!.context
        lateinit var controller : LayoutAnimationController

        if(type == 2)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_right)

        recyclerlist_complaint.layoutAnimation = controller
    }

    inner class customLayoutComplaint(var FinalComplaintList : ComplaintClass) : Item<ViewHolder>()
    {
        override fun getLayout(): Int {
            return R.layout.custom_complaint_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.headline_complaint.text = FinalComplaintList.CompheadLine
            viewHolder.itemView.headline_complaint_1.text = FinalComplaintList.CompheadLine
            viewHolder.itemView.Details_complaint.text = FinalComplaintList.CompDetails
            viewHolder.itemView.process_complaint.text = FinalComplaintList.CompProcess
            viewHolder.itemView.date_complaint.text = FinalComplaintList.CompUpdatedDate

            if(FinalComplaintList.CompProcess == "Under Process")
            {
                viewHolder.itemView.problem_logo.setBackgroundResource(R.drawable.processing)
                viewHolder.itemView.img_display_success.setBackgroundResource(R.drawable.processing)
            }
            else{
                viewHolder.itemView.problem_logo.setBackgroundResource(R.drawable.checkmark)
                viewHolder.itemView.img_display_success.setBackgroundResource(R.drawable.checkmark)
            }

            viewHolder.itemView.setOnClickListener {
                viewHolder.itemView.folding_cell_complaint.toggle(false)
            }
        }
    }
}


class ComplaintClass(val CompheadLine : String,val CompDetails : String,val CompUpdatedDate : String,val CompProcess : String)
{
    constructor() : this("","","","")
}
