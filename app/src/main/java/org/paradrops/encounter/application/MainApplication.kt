package org.paradrops.encounter.application

import android.app.Application
import android.content.Context

class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: MainApplication

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }
}
