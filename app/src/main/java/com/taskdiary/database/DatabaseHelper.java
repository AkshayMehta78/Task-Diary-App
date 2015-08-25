package com.taskdiary.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.taskdiary.model.Reminder;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 06/08/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static SQLiteDatabase db;
    // Logcat tag
    public static final String LOG = "DatabaseHelper";
    // Database Version
    public static final int DATABASE_VERSION =1;
    // Database Name
    public static final String DATABASE_NAME = "TaskDiary";

    // Table Names
    public static final String TABLE_CATEGORY = "category";
    public static final String TABLE_TASK = "task";
    public static final String TABLE_REMINDER = "reminder";


    // Common column names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";

    //Task column names
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "desc";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_CONTACTS_ID = "contacts_IDS";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_DELETED = "deleted";

    //Reminder column names
    public static final String KEY_TASKID = "task_id";
    public static final String KEY_TIME = "time";


    // Table Create Statements
    // Todo table create statement
    public static final String CREATE_TABLE_CATEGORY= "CREATE TABLE "
            + TABLE_CATEGORY + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_NAME + " TEXT)";

    public static final String CREATE_TABLE_TASK= "CREATE TABLE "
            + TABLE_TASK + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_TITLE + " TEXT," + KEY_DESC + " TEXT," + KEY_CATEGORY + " TEXT," + KEY_PRIORITY + " TEXT," + KEY_TYPE + " TEXT,"+ KEY_CONTACTS_ID+ " TEXT," + KEY_DATE + " TEXT," + KEY_COMPLETED + " TEXT," + KEY_DELETED + " TEXT)";

    public static final String CREATE_TABLE_REMINDER= "CREATE TABLE "
            + TABLE_REMINDER + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_TASKID + " INTEGER," + KEY_DATE + " TEXT,"+KEY_TIME+" TEXT, "+ KEY_COMPLETED + " TEXT)";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_TASK);
        Log.e(LOG, CREATE_TABLE_REMINDER);
        db.execSQL(CREATE_TABLE_REMINDER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void addDefaultCategories(Context context,ArrayList<String> category_list)
    {
        for(int i=0;i<category_list.size();i++)
        {
            addCategory(category_list.get(i));
        }
        Constant.setCategory(context, true);
    }

    public void addCategory(String category) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME,category);
        db.insert(TABLE_CATEGORY, null, values);
    }

    public String[] getAllCategories()
    {

        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " ORDER BY " + KEY_ID + " DESC";;
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        int i=0;
        String[] default_list = new String[c.getCount()];
        if (c.moveToFirst()) {
            do {
                default_list[i]=c.getString(c.getColumnIndex(KEY_NAME));
                i++;
            } while (c.moveToNext());
        }

        return default_list;

    }


    private void addReminderList(ArrayList<Reminder> list, long taskId) {
        for(int i = 0;i<list.size();i++)
        {
                ContentValues values = new ContentValues();
                values.put(KEY_TASKID, taskId);
                values.put(KEY_DATE, list.get(i).getDate());
                values.put(KEY_TIME, list.get(i).getTime());
                values.put(KEY_COMPLETED, 0);
                db.insert(TABLE_REMINDER, null, values);
        }
    }

    private boolean isExist(long taskId, Reminder reminder) {
        boolean flag=false;
        ArrayList<Reminder> remindersList = getAllTaskReminders((int)taskId);
        for(int i = 0;i<remindersList.size();i++)
        {
            if(remindersList.get(i).getDate().equalsIgnoreCase(reminder.getDate()) || remindersList.get(i).getCompleted().equalsIgnoreCase(Constant.COMPLETE))
                {
                    flag= true;
                }else
                {
                    deleteARemindersOfTask((int)taskId,remindersList.get(i).getDate());
                }
        }
        return flag;
    }

    public ArrayList<Task> getAllTask(String status) {
        ArrayList<Task> result = new ArrayList<Task>();
        String selectQuery ="";
        if(status.equalsIgnoreCase(Constant.COMPLETE)) {
            String Ids = getAllCompletedDayIds();
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE " + KEY_ID+" IN "+Ids+" AND "+KEY_DELETED+"=0  ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";
        }else if(status.equalsIgnoreCase(Constant.INCOMPLETE))
        {
            String Ids = getAllCurrentDayIds();
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_ID+" IN "+Ids+" AND "+KEY_DELETED+"=0   ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";
        }else if(status.equalsIgnoreCase(Constant.PENDING))
        {
            String Ids = getAllPreviousDayIds();
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_ID+" IN "+Ids+" AND "+KEY_DELETED+"=0  ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";
        }
        else if(status.equalsIgnoreCase(Constant.ALL) || status.equalsIgnoreCase(""))
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_DELETED+"='0'  ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";

        Log.e("query",selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Task item = new Task();
                item.setId(c.getString(c.getColumnIndex(KEY_ID)));
                item.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
                item.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));
                item.setCategory(c.getString(c.getColumnIndex(KEY_CATEGORY)));
                item.setPriority(c.getString(c.getColumnIndex(KEY_PRIORITY)));
                item.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                item.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                item.setCompleted(c.getString(c.getColumnIndex(KEY_COMPLETED)));
                item.setDeleted(c.getString(c.getColumnIndex(KEY_DELETED)));
                item.setContactIDs(c.getString(c.getColumnIndex(KEY_CONTACTS_ID)));
                boolean taskstatus =  getTaskStatus(c.getString(c.getColumnIndex(KEY_ID)));
                item.setTaskStatus(taskstatus);
                if(status.equalsIgnoreCase("") && taskstatus)
                    result.add(item);
                else if(status.equalsIgnoreCase(Constant.COMPLETE)||status.equalsIgnoreCase(Constant.INCOMPLETE)||status.equalsIgnoreCase(Constant.PENDING)||status.equalsIgnoreCase(Constant.ALL))
                    result.add(item);
            } while (c.moveToNext());
        }
        return result;
    }

    private boolean getTaskStatus(String taskId) {
        boolean flag=true;
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE " + KEY_TASKID + "='"+taskId+"'";
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                Log.e("completed",c.getString(c.getColumnIndex(KEY_COMPLETED)));
                if(c.getString(c.getColumnIndex(KEY_COMPLETED)).equals("0")) {
                    flag = false;
                }
            } while (c.moveToNext());
        }

    return flag;
    }

    private String getAllCompletedDayIds() {
        String Ids="(";
        String currentDate = Utils.getCurrentDate();
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE "+KEY_COMPLETED+"='1'";
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                if(c.isLast())
                {
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID));
                }
                else
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID))+",";
            } while (c.moveToNext());
        }
        return Ids+")";
    }

    private String getAllPreviousDayIds() {
        String Ids="(";
        String currentDate = Utils.getCurrentDate();
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE " + KEY_DATE + "<'"+currentDate+"' AND "+ KEY_COMPLETED+"='0'";
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                if(c.isLast())
                {
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID));
                }
                else
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID))+",";
            } while (c.moveToNext());
        }
        return Ids+")";

    }

    private String getAllCurrentDayIds() {
        String Ids="(";
        String currentDate = Utils.getCurrentDate();
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE " + KEY_DATE + "='"+currentDate+"' AND "+KEY_COMPLETED+"='0'";
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                if(c.isLast())
                {
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID));
                }
                else
                    Ids = Ids+c.getInt(c.getColumnIndex(KEY_TASKID))+",";
            } while (c.moveToNext());
        }
        return Ids+")";
    }

    public void updateTask(String id, String value, String status) {
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETED, value);
        if(status.equalsIgnoreCase(Constant.INCOMPLETE))
            db.update(TABLE_REMINDER, values, KEY_DATE + "='" + Utils.getCurrentDate()+"' AND "+ KEY_TASKID+"="+id, null);
        else if(status.equalsIgnoreCase(Constant.PENDING))
            db.update(TABLE_REMINDER, values, KEY_DATE + "<'" + Utils.getCurrentDate()+"' AND "+ KEY_TASKID+"="+id, null);
        else
            db.update(TABLE_REMINDER, values, KEY_DATE + "<='" + Utils.getCurrentDate()+"' AND "+ KEY_TASKID+"="+id, null);
    }

    public void deleteTask(String id) {
        ContentValues values = new ContentValues();
        values.put(KEY_DELETED,Constant.DELETED);
        int rowsUpdated = db.update(TABLE_TASK, values, KEY_ID + "=" + id, null);
    }

    public boolean isCategoryExist(String s) {
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " WHERE " + KEY_NAME + "='"+s+"'";
        Cursor c = db.rawQuery(selectQuery, null);
        if(c.getCount()>0)
            return true;
        else
            return false;

    }

    public Task getTaskDetails(int taskID) {
        String selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE " + KEY_ID + "="+taskID;
        Task item = new Task();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
                item.setId(c.getString(c.getColumnIndex(KEY_ID)));
                item.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
                item.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));
                item.setCategory(c.getString(c.getColumnIndex(KEY_CATEGORY)));
                item.setPriority(c.getString(c.getColumnIndex(KEY_PRIORITY)));
                item.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                item.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                item.setCompleted(c.getString(c.getColumnIndex(KEY_COMPLETED)));
                item.setDeleted(c.getString(c.getColumnIndex(KEY_DELETED)));
                item.setContactIDs(c.getString(c.getColumnIndex(KEY_CONTACTS_ID)));
        }
        return item;
    }

    public ArrayList<Reminder> getAllTaskReminders(int taskID) {
        ArrayList<Reminder> result = new ArrayList<Reminder>();
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER + " WHERE " + KEY_TASKID + "="+taskID;
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Reminder item = new Reminder();
                item.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                item.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                item.setTime(c.getString(c.getColumnIndex(KEY_TIME)));
                item.setCompleted(c.getString(c.getColumnIndex(KEY_COMPLETED)));
                result.add(item);
            } while (c.moveToNext());
        }
        return result;
    }

    public void deleteCategory(CharSequence text) {
        db.execSQL("DELETE FROM " + TABLE_CATEGORY + " WHERE " + KEY_NAME + "='" + text + "'");
    }

    public void updateCategory(CharSequence oldtext, String newtext) {
        db.execSQL("UPDATE " + TABLE_CATEGORY + " SET "+KEY_NAME+"='"+newtext+"' WHERE " + KEY_NAME + "='" + oldtext + "'");
    }
    public long addTask(Task item) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DESC,item.getDesc());
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_PRIORITY, item.getPriority());
        values.put(KEY_TYPE, item.getType());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_COMPLETED, Constant.INCOMPLETE);
        values.put(KEY_DELETED, Constant.NOTDELETED);
        if(item.getType().equalsIgnoreCase("Group"))
            values.put(KEY_CONTACTS_ID, item.getContactIDs());
        long taskId=db.insert(TABLE_TASK, null, values);
        if(item.getList().size()>0)
            addReminderList(item.getList(),taskId);
        return taskId;
    }
    public void updateTaskDetails(Task item, int taskID) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DESC,item.getDesc());
        values.put(KEY_CATEGORY, item.getCategory());
        values.put(KEY_PRIORITY, item.getPriority());
        values.put(KEY_TYPE, item.getType());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_COMPLETED, Constant.INCOMPLETE);
        values.put(KEY_DELETED, Constant.NOTDELETED);
        if(item.getType().equalsIgnoreCase("Group"))
            values.put(KEY_CONTACTS_ID, item.getContactIDs());
        db.update(TABLE_TASK, values, KEY_ID + "=" + taskID, null);
        deleteAllRemindersOfTask(taskID);
        if(item.getList().size()>0)
            addReminderList(item.getList(),taskID);
        Log.e("Task Data", values.toString());
    }

    private void deleteAllRemindersOfTask(int taskID) {
        db.execSQL("DELETE FROM " + TABLE_REMINDER + " WHERE " + KEY_TASKID + "=" + taskID+" AND "+KEY_COMPLETED+"!='"+Constant.COMPLETE+"'");
    }
    private void deleteARemindersOfTask(int taskID,String date) {
        db.execSQL("DELETE FROM " + TABLE_REMINDER + " WHERE " + KEY_TASKID + "=" + taskID+" AND "+KEY_DATE+"='"+date+"'");
    }
    public ArrayList<Task> getAllTaskByCategory(String status, String category) {
        ArrayList<Task> result = new ArrayList<Task>();
        String selectQuery = "SELECT  * FROM " + TABLE_TASK + " ORDER BY " + KEY_ID + " DESC";
        if(status.equalsIgnoreCase(Constant.COMPLETE)) {
            String Ids = getAllCompletedDayIds();
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_ID+" IN "+Ids+" AND "+KEY_DELETED+"='0' AND "+KEY_CATEGORY+"='"+category+"' ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";
        }else if(status.equalsIgnoreCase(Constant.INCOMPLETE))
        {
            String Ids = getAllCurrentDayIds();
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_ID+" IN "+Ids+" AND "+KEY_DELETED+"='0' AND "+KEY_CATEGORY+"='"+category+"' ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";
        }else
        {
            selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "+KEY_DELETED+"='0' AND "+KEY_CATEGORY+"='"+category+"' ORDER BY " + KEY_ID + " DESC";
        }

        Log.e("query",selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Task item = new Task();
                item.setId(c.getString(c.getColumnIndex(KEY_ID)));
                item.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
                item.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));
                item.setCategory(c.getString(c.getColumnIndex(KEY_CATEGORY)));
                item.setPriority(c.getString(c.getColumnIndex(KEY_PRIORITY)));
                item.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                item.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                item.setCompleted(c.getString(c.getColumnIndex(KEY_COMPLETED)));
                item.setDeleted(c.getString(c.getColumnIndex(KEY_DELETED)));
                item.setContactIDs(c.getString(c.getColumnIndex(KEY_CONTACTS_ID)));
                result.add(item);
            } while (c.moveToNext());
        }
        return result;
    }

    public ArrayList<Task> getAllTaskPersonWise(String status, Activity activity) {
        ArrayList<Task> result = new ArrayList<Task>();
        ArrayList<String> contactIds = getAllContacts();
        for(int i=0;i<contactIds.size();i++)
        {
            String selectQuery ="";
            String Ids = getAllPreviousDayIds();
            selectQuery = "SELECT  DISTINCT id,title,desc,category,priority,type,date,completed,deleted FROM " + TABLE_TASK + " WHERE "+KEY_ID+" IN "+Ids+" AND "+KEY_TYPE+"='Group' AND "+KEY_CONTACTS_ID+" like '%"+contactIds.get(i)+"%' AND "+KEY_DELETED+"=0  ORDER BY CASE WHEN "+KEY_PRIORITY+"='High' THEN 1 WHEN "+KEY_PRIORITY+"='Medium' THEN 2 WHEN "+KEY_PRIORITY+"='Low' THEN 3 END ASC";

            Log.e("query",selectQuery);
            Cursor c = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    Task item = new Task();
                    item.setId(c.getString(c.getColumnIndex(KEY_ID)));
                    item.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
                    item.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));
                    item.setCategory(c.getString(c.getColumnIndex(KEY_CATEGORY)));
                    item.setPriority(c.getString(c.getColumnIndex(KEY_PRIORITY)));
                    item.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
                    item.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                    item.setCompleted(c.getString(c.getColumnIndex(KEY_COMPLETED)));
                    item.setDeleted(c.getString(c.getColumnIndex(KEY_DELETED)));
                    item.setContactIDs(contactIds.get(i));
                    item.setContactName(Utils.retrieveContactName(contactIds.get(i),activity));
                    result.add(item);
                } while (c.moveToNext());
            }
        }
        return result;
    }

    private ArrayList<String> getAllContacts() {
        ArrayList<String> result = new ArrayList<String>();
        String selectQuery = "SELECT  DISTINCT "+ KEY_CONTACTS_ID +" FROM " + TABLE_TASK + " WHERE "+ KEY_TYPE +"='Group' ORDER BY " + KEY_ID + " DESC";
        Log.e("query",selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                result.add(c.getString(c.getColumnIndex(KEY_CONTACTS_ID)));
            } while (c.moveToNext());
        }

        ArrayList<String> listOfContacts = Utils.getUniqueContacts(result);
        return listOfContacts;
    }
}
