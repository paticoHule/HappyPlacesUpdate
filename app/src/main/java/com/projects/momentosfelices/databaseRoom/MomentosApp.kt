package com.projects.momentosfelices.databaseRoom

import android.app.Application

class MomentosApp:Application() {

    val db by lazy {
        MomentosDatabase.getInstance(this)
    }
}