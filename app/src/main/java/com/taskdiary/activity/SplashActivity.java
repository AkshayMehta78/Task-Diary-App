package com.taskdiary.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.splunk.mint.Mint;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import java.util.ArrayList;


public class SplashActivity extends Activity {

    private ArrayList<String> defaultList;
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Mint.initAndStartSession(SplashActivity.this, "125ddbf4");

        db=new DatabaseHelper(this);
        defaultList = new ArrayList<String>();
        if(!Constant.getCategory(this)) {
            addDefaultCategory();
            db.addDefaultCategories(this,defaultList);
        }

        /*
           Splash screen waiting
        */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Utils.startHomeActivity(SplashActivity.this);
            }
        }, 1500);
    }

    private void addDefaultCategory() {
        defaultList.add("Personal");
        defaultList.add("Professional");
        defaultList.add("General");
        defaultList.add("Family");
    }
}
