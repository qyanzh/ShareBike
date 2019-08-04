package com.example.west2summer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView

class AutoCompleteAdapter(
    context: Context,
    private val resource: Int,
    val list: List<String>
) : ArrayAdapter<String>(context, resource, list) {
    var items: List<String>? = null

    override fun getFilter() = object : Filter() {

        override fun performFiltering(constraint: CharSequence?) = FilterResults()

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            clear()
            items?.let {
                for (item in items!!) {
                    add(item)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(resource, parent, false)
        }

        if (view is TextView) {
            view.text = list[position]
        }

        return view!!
    }


}
