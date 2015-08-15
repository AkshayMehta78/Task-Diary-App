package com.taskdiary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

/**
 * Created by akshaymehta on 09/08/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private int taskID;

    @Override
    public void onReceive(Context context, Intent intent) {

        taskID=intent.getIntExtra(DatabaseHelper.KEY_TASKID,0);
        Utils.showToast(context,taskID+"");
    }
}