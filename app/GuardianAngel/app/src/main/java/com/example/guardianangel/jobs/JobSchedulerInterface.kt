package com.example.guardianangel.jobs

interface JobSchedulerInterface {
    fun scheduleDailyJob(timeInterval: Long = 0L)
    fun cancelDailyJob()
}