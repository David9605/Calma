package com.calmaceleste.app

import android.app.Application
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.Scheduler

class CalmaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)
        Scheduler.createChannel(this)
        Scheduler.scheduleAll(this)
    }
}
