package org.paradrops.encounter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.paradrops.encounter.data.EncounterInfoDataStore
import org.paradrops.encounter.eventbus.FoundEvent
import org.paradrops.encounter.view.EncounterInfoItemAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView.adapter = EncounterInfoItemAdapter.create(this, EncounterInfoDataStore().get())
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFoundEvent(event: FoundEvent) {
        listView.adapter = EncounterInfoItemAdapter.create(this, EncounterInfoDataStore().get())
    }
}
