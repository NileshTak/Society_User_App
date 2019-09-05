package com.nil_projects_society_user_app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.diegodobelo.expandingview.ExpandingItem
import com.diegodobelo.expandingview.ExpandingList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_maintainance__records.*
import kotlinx.android.synthetic.main.fragment_maintainance__records.view.*
import kotlinx.android.synthetic.main.fragment_report.*
import java.text.SimpleDateFormat
import java.util.*

class Maintainance_Records : Fragment() {

    lateinit var mExpandingList: ExpandingList
    lateinit var db : FirebaseFirestore
    lateinit var selectedMonth : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maintainance__records, container, false)


        db = FirebaseFirestore.getInstance()

        mExpandingList = view.findViewById<ExpandingList>(R.id.expanding_list_main)

        fetchMaintainancepaidMonths()

        return view
    }

    private fun fetchMaintainancepaidMonths() {

        var flatNo : String
        var wingname : String
        var Amount : String
        var Fine : String

        var id = FirebaseAuth.getInstance().currentUser!!.uid

        var db = FirebaseFirestore.getInstance()
        db.collection("FlatUsers")
                .whereEqualTo("UserID", id)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                        documentSnapshot.documents.forEach {
                            val city = it.toObject(UserSocietyClass :: class.java)
                                db.collection("FlatUsers").document(id).collection("PaidMonths")
                                        .get()
                                        .addOnSuccessListener {
                                            it.documents.forEach {
                                                val monthsdata = it.toObject(months :: class.java)

                                                flatNo = city!!.FlatNo
                                                wingname = city!!.Wing
                                                Amount = monthsdata!!.Amount
                                                Fine = monthsdata!!.Fine

                                                if (monthsdata != null) {
                                                    //           adapter.add(FetchNotificationItem(monthsdata))

                                                    addItem(monthsdata.ReceiptNumber, arrayOf(monthsdata.MonthsPaid0,monthsdata.MonthsPaid1,monthsdata.MonthsPaid2,monthsdata.MonthsPaid3
                                                            ,monthsdata.MonthsPaid4,monthsdata.MonthsPaid5,monthsdata.MonthsPaid6
                                                            ,monthsdata.MonthsPaid7,monthsdata.MonthsPaid8,monthsdata.MonthsPaid9
                                                            ,monthsdata.MonthsPaid10,monthsdata.MonthsPaid11),flatNo,wingname,Amount,Fine,
                                                            R.color.md_pink_400, R.drawable.ic_ghost)
                                                }
                                            }
                                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("SocietyFirestore", "Error getting documents.", exception)
                }
    }

//    private fun fetchMaintainanceNotpaidMonths() {
//
//        var flatNo : String
//        var wingname : String
//
//        db.collection("FlatUsers")
//                .whereEqualTo("SocietyName", "SIDDHIVINAYAK MANAS CO-OP. HOUSING SOCIETY")
//                .get()
//                .addOnSuccessListener { documentSnapshot ->
//                    documentSnapshot.documents.forEach {
//                        val city = it.toObject(UserClass :: class.java)
//                        fetchMonthData(city!!.UserID,city.FlatNo,city.Wing)
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.w("SocietyFirestore", "Error getting documents.", exception)
//                }
//    }

//    private fun fetchMonthData(userid : String,flatNo : String,wingname: String)
//    {
//        db.collection("FlatUsers").document(userid).collection("PaidMonths")
//                .get()
//                .addOnSuccessListener {
//                    it.documents.forEach {
//                        val monthsdata = it.toObject(months :: class.java)
//
//                        if (monthsdata!!.MonthsPaid0 != selectedMonth && monthsdata.MonthsPaid1 != selectedMonth
//                                && monthsdata.MonthsPaid2 != selectedMonth && monthsdata.MonthsPaid3 != selectedMonth
//                                && monthsdata.MonthsPaid4 != selectedMonth && monthsdata.MonthsPaid5 != selectedMonth
//                                && monthsdata.MonthsPaid6 != selectedMonth && monthsdata.MonthsPaid7 != selectedMonth
//                                && monthsdata.MonthsPaid8 != selectedMonth && monthsdata.MonthsPaid9 != selectedMonth
//                                && monthsdata.MonthsPaid10 != selectedMonth && monthsdata.MonthsPaid11 != selectedMonth) {
//
//                            Log.d("Count",flatNo)
//
//                            addItem("Pending", arrayOf(""),flatNo,wingname,
//                                    R.color.md_pink_400, R.drawable.ic_ghost)
//
//                        }
//                    }
//                }
//    }


    private fun addItem(title: String, subItems: Array<String>,flatNo : String,wingname : String,
                        Amount : String,Fine : String,
                        colorRes: Int, iconRes: Int)


    {

        if (Amount.isNotEmpty())
        {
            //Let's create an item with R.layout.expanding_layout
            val item = mExpandingList!!.createNewItem(R.layout.expanding_layout)

            //If item creation is successful, let's configure it
            if (item != null) {
                item.setIndicatorColorRes(colorRes)
                item.setIndicatorIconRes(iconRes)
                //It is possible to get any view inside the inflated layout. Let's set the text in the item
                (item.findViewById(R.id.title) as TextView).text = title
                (item.findViewById(R.id.custom_flatno) as TextView).text = flatNo
                (item.findViewById(R.id.custom_societyname) as TextView).text = wingname
                if(Amount.isNotEmpty() && Fine.isNotEmpty())
                {
                    (item.findViewById(R.id.tvAmountFine) as TextView).text = "$Amount+$Fine"
                }
                else
                {
                    (item.findViewById(R.id.tvAmountFine) as TextView).text = "$Amount"
                }


                //We can create items in batch.
                item.createSubItems(subItems.size)
                for (i in 0 until item.subItemsCount) {
                    //Let's get the created sub item by its index
                    val view = item.getSubItemView(i)

                    //Let's set some values in
                    configureSubItem(item, view, subItems[i])
                }

//            item.findViewById<ImageView>(R.id.add_more_sub_items).setOnClickListener(View.OnClickListener {
//                showInsertDialog(object : OnItemCreated {
//                    override fun itemCreated(title: String) {
//                        val newSubItem = item.createSubItem()
//                        configureSubItem(item, newSubItem!!, title)
//                    }
//                })
//            })

//            item.findViewById<ImageView>(R.id.remove_item)
//                    .setOnClickListener(View.OnClickListener { mExpandingList!!.removeItem(item) })
            }
        }

    }

    private fun configureSubItem(item: ExpandingItem?, view: View, subTitle: String) {
        (view.findViewById(R.id.sub_title) as TextView).text = subTitle
        if(subTitle == "")
        {
            item!!.removeSubItem(view)
        }
//        view.findViewById<ImageView>(R.id.remove_sub_item)
//                .setOnClickListener(View.OnClickListener { item!!.removeSubItem(view) })
    }

//    private fun showInsertDialog(positive: OnItemCreated) {
//        val text = EditText(activity)
//        val builder = AlertDialog.Builder(activity)
//        builder.setView(text)
//        builder.setTitle("Enter Title")
//        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which -> positive.itemCreated(text.text.toString()) })
//        builder.setNegativeButton(android.R.string.cancel, null)
//        builder.show()
//    }

//    internal interface OnItemCreated {
//        fun itemCreated(title: String)
//    }

//    inner class FetchNotificationItem(var Finalnotifi: months) : Item<ViewHolder>() {
//
//        override fun getLayout(): Int {
//            return R.layout.custon_maintainancerecords
//        }
//
//        override fun bind(viewHolder: ViewHolder, position: Int) {
//            viewHolder.itemView.receipt_no_custom.text = Finalnotifi.ReceiptNumber
//        }
//    }
}

class months(val MonthsPaid0: String,val MonthsPaid1 : String,val MonthsPaid2 : String,val MonthsPaid3: String,val MonthsPaid4: String,
             val MonthsPaid5: String,val MonthsPaid6: String,val MonthsPaid7: String,val MonthsPaid8: String,
             val MonthsPaid9: String,val MonthsPaid10: String,val MonthsPaid11: String,
             val ReceiptNumber : String,val Amount : String,val Fine : String)
{
    constructor() : this("","","","","","",
            "","","","","","","","","")
}