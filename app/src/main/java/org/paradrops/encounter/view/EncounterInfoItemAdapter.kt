package org.paradrops.encounter.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.paradrops.encounter.R

class EncounterInfoItemAdapter(
        context: Context,
        resId: Int,
        items: List<EncounterInfo>
) : ArrayAdapter<EncounterInfo>(context, resId, items) {
    companion object {
        fun create(context: Context, items: List<EncounterInfo>) : EncounterInfoItemAdapter {
            return EncounterInfoItemAdapter(context, R.layout.view_encounter_info_item, items)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view !is EncounterInfoItemView) {
            view = EncounterInfoItemView.inflate(context)
        }

        view.initView(getItem(position))
        return view
    }
}