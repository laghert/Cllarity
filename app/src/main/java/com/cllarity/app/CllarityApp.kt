package com.cllarity.app

import android.app.Application

class CllarityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CllarityApp
            private set
    }
}
