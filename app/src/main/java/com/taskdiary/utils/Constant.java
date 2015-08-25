package com.taskdiary.utils;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.taskdiary.database.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by akshaymehta on 06/08/15.
 */
public class Constant {
    public static final String DATEFORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DATEFORMAT = "yyyy/MM/dd";

    public static final DateFormat SDFDATEFORMAT_DISPLAY = new SimpleDateFormat(DATEFORMAT_DISPLAY);
    public static final DateFormat SDFDATEFORMAT = new SimpleDateFormat(DATEFORMAT);

    public static final String INCOMPLETE = "0";
    public static final String COMPLETE = "1";
    public static final String PENDING = "2";

    public static final String NOTDELETED = "0";
    public static final String DELETED = "1";
    //priority
    public static final String HIGH = "High";
    public static final String MEDIUM = "Medium";
    public static final String LOW = "Low";

    public static final int CREATE = 0;
    public static final int VIEW = 1;
    public static final int EDIT = 2;
    public static final String ALL = "All";


    public static String CATEGORY="category";


    // Preferences
    public static void setCategory(Context context,boolean flag) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(CATEGORY, flag).commit();
    }

    public static boolean getCategory(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(CATEGORY,false);
    }
}
