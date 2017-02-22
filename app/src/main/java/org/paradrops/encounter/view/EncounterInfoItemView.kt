package org.paradrops.encounter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_encounter_info_item.view.*
import org.paradrops.encounter.R

class EncounterInfoItemView : LinearLayout {
    companion object {
        fun inflate(context: Context) : EncounterInfoItemView {
            return LayoutInflater.from(context).inflate(R.layout.view_encounter_info_item, null) as EncounterInfoItemView
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    fun initView(encounterInfo: EncounterInfo) {
        nameTextView.text = encounterInfo.name
        dateTextView.text = encounterInfo.date
    }
}