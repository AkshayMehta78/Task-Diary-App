package com.taskdiary.activity;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
