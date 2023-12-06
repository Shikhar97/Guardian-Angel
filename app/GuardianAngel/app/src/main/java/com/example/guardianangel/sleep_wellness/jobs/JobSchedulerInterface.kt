package com.example.guardianangel.sleep_wellness.jobs

interface JobSchedulerInterface {
    fun scheduleDailyJob(timeInterval: Long = 0L)
    fun cancelDailyJob()
}