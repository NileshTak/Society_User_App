package com.nil_projects_society_user_app

import android.app.Application

import uk.co.chrisjenx.calligraphy.CalligraphyConfig


public class App : Application()
{
    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("Exo-Bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}