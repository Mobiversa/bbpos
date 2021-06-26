package com.mobiversa.ezy2pay.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.network.response.ContactPojo


class PeopleAdapter(
    var mContext: Context,
    var resource: Int,
    var items: List<ContactPojo>
) :
    ArrayAdapter<ContactPojo?>(mContext, resource, items) {
    var tempItems = ArrayList(items)
    var suggestions: ArrayList<ContactPojo> = ArrayList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.list_contact, parent, false)
        }
        val ContactPojo = items[position]
        if (ContactPojo != null) {
            val lblName = view!!.findViewById<View>(R.id.contact_name_txt) as TextView
            lblName.text = ContactPojo.name
            val lblNum = view!!.findViewById<View>(R.id.contact_number_txt) as TextView
            lblNum.text = ContactPojo.phone
        }
        return view!!
    }

    override fun getFilter(): Filter {
        return nameFilter
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    private var nameFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String? {
            return (resultValue as ContactPojo).phone?.replace(" ","")
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                suggestions.clear()
                for (ContactPojo in tempItems) {
                   if (ContactPojo.phone!= null){
                       if (ContactPojo.phone!!.contains(constraint.toString()) ||
                           ContactPojo.name!!.toLowerCase().contains(constraint.toString().toLowerCase())
                       ) {
                           suggestions.add(ContactPojo)
                       }
                   }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            } else {
                FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filterList: ArrayList<ContactPojo?>? = results?.values as? ArrayList<ContactPojo?>
            if (results != null && results.count > 0) {
                clear()
                for (ContactPojo in filterList!!) {
                    add(ContactPojo)
                    notifyDataSetChanged()
                }
            }
        }
    }

    init {
        // this makes the difference.
    }
}