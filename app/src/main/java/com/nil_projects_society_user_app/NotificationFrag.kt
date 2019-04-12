package com.nil_projects_society_user_app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_notification.view.*
import kotlinx.android.synthetic.main.custom_records_layout.view.*


class NotificationFrag : Fragment() {

    lateinit var recyclerview_xml_notifrag : RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_notification, container, false)

        recyclerview_xml_notifrag = view.findViewById<RecyclerView>(R.id.recyclerview_xml_notifrag)

        fetchNotifications()

        return view
    }



    private fun fetchNotifications() {
        val ref = FirebaseDatabase.getInstance().getReference("/Notifications")
        ref.addListenerForSingleValueEvent(object : ValueEventListener
        {
            val adapter = GroupAdapter<ViewHolder>()

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val notifi = it.getValue(AddNotifiClass::class.java)

                    if (notifi != null) {
                        adapter.add(FetchNotificationItem(notifi))
                    }
                }
                recyclerview_xml_notifrag.adapter = adapter
            }
        })
    }
}


class FetchNotificationItem(var Finalnotifi : AddNotifiClass) : Item<ViewHolder>()
{

    override fun getLayout(): Int {
        return R.layout.custom_notification
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.noti_text_view.text = Finalnotifi.noti
        Picasso.get().load(Finalnotifi.imageUrl).into(viewHolder.itemView.noti_img_xml)
    }
}


class AddNotifiClass(val id : String,val noti : String,val imageUrl : String)
{
    constructor() : this("","","")
}