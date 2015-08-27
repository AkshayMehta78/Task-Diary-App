package com.taskdiary.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.taskdiary.activity.CreateTaskActivity;
import com.taskdiary.activity.HomeActivity;
import com.taskdiary.activity.R;
import com.taskdiary.activity.SplashActivity;
import com.taskdiary.activity.ViewTaskDetails;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Reminder;
import com.taskdiary.model.Task;
import com.taskdiary.receiver.AlarmReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Created by akshaymehta on 05/08/15.
 */
public class Utils {



    public static void startHomeActivity(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void addNewCategory(final Activity activity) {
        final DatabaseHelper db = new DatabaseHelper(activity);
        new MaterialDialog.Builder(activity)
                .title(R.string.add_category)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .theme(Theme.LIGHT)
                .input(R.string.input_hint, R.string.prefill_input, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().isEmpty()) {
                            if (!db.isCategoryExist(input.toString())) {
                                db.addCategory(input.toString());
                                Utils.showToast(activity, activity.getString(R.string.category_added));
                            } else
                                Utils.showToast(activity, activity.getString(R.string.category_exist));

                        } else
                            Utils.showToast(activity, activity.getString(R.string.new_empty_category));
                    }
                }).show();
    }

    public static Bitmap retrieveContactPhoto(String contactID, Activity activity) {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(activity.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            assert inputStream != null;
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return photo;

    }

    public static String getContactID(Uri uriContact, Activity activity) {

        // getting contacts ID
        Cursor cursorID = activity.getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        String contactID = "";
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();
        return contactID;
    }

    public static String retrieveContactName(String contactID, Activity activity) {
        String contactName = null;

        Cursor cursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                ContactsContract.Contacts._ID + " = ?", new String[]{contactID}, null);

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        return contactName;
    }

    public static Spannable getSpannableString(String string, int end) {
        Spannable wordtoSpan = new SpannableString(string);
        wordtoSpan.setSpan(new ForegroundColorSpan(Color.GREEN), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), 0, end, 0);
        return wordtoSpan;
    }

    public static ArrayList<String> formatStringToArray(String contactIDs) {
        String contacts = contactIDs.replace("[", "");
        String formatedContacts = contacts.replace("]", "");
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(formatedContacts.split(",")));
        if (list != null)
            return list;
        else
            return new ArrayList<String>();

    }

    public static String getContactNumber(String contactID, Context context) {
        String contactNumber = "";
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        return contactNumber;
    }

    public static void showCallDialog(Activity activity, String contactNo) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactNo));
        activity.startActivity(intent);
    }

    public static void sendSMSDialog(Activity activity, String contactNo, Task taskDetails) {
        Uri uri = Uri.parse("smsto:" + contactNo);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
//        it.putExtra(taskDetails.getTitle(), taskDetails.getDesc());
        it.putExtra("sms_body",taskDetails.getTitle()+": "+taskDetails.getDesc());
        activity.startActivity(it);
    }

    public static String getTime(int hr, int min) {
        Time tme = new Time(hr, min, 0);//seconds by default set to zero
        Format formatter;
        formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(tme);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(Constant.DATEFORMAT);
        Calendar cal = Calendar.getInstance();
        String currentDate = dateFormat.format(cal.getTime());
        return currentDate;
    }

    public static void setReminderToTask(ArrayList<Reminder> list, int taskId, Activity activity) {
        for (int i = 0; i < list.size(); i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.MONTH, 8);
            calendar.set(Calendar.YEAR, 2015);
            calendar.set(Calendar.DAY_OF_MONTH, 9);
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            calendar.set(Calendar.MINUTE,35);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.AM_PM, Calendar.PM);
            calendar.set(2015,7,9,18,45,0);
            if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            Intent myIntent = new Intent(activity, AlarmReceiver.class);
            myIntent.putExtra(DatabaseHelper.KEY_TASKID, taskId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, list.get(i).getId(), myIntent, 0);
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(activity.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + calendar.getTimeInMillis(), pendingIntent);
        }

    }

    public static boolean isDateValid(String selectedDate) {
        boolean flag= false;
        String currentDate = getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATEFORMAT);
        try {
            Date currDate = sdf.parse(currentDate);
            Date selDate = sdf.parse(selectedDate);
            if(selDate.equals(currDate) || selDate.after(currDate))
                flag = true;
            else
                flag = false;
        } catch (ParseException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }
    
    public static Date getDateFromString(String date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATEFORMAT);
        Date dateObj = null;
        try {
            dateObj = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj;
    }

    public static ArrayList<String> getUniqueContacts(ArrayList<String> result) {
        ArrayList<String> list =new ArrayList<String>();
        for(int i=0;i<result.size();i++)
        {
            String contactIDS = result.get(i);
            ArrayList<String> contactIDsArray = Utils.formatStringToArray(contactIDS);
            for(int j=0;j<contactIDsArray.size();j++)
            {
                if(!list.contains(contactIDsArray.get(j).trim()))
                    list.add(contactIDsArray.get(j).trim());
            }
        }
        Collections.sort(list);
        return list;
    }

    public static String retrieveContactEmail(String contactid, Context context) {
        String email="";
        Cursor emailCur = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contactid}, null);
        while (emailCur.moveToNext()) {
            // This would allow you get several email addresses
            // if the email addresses were stored in an array
            email = emailCur.getString(
                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            String emailType = emailCur.getString(
                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
        }
        emailCur.close();
        return email;
    }

    public static void showEmailDialog(Activity activity, String emailId, Task taskDetails) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailId});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.email_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, taskDetails.getTitle()+":"+taskDetails.getDesc());
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public static void showEmailDialog(Activity activity, String emailId, String message) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailId});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.email_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        activity.startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public static void sendSMSDialog(Activity activity, String contactNo, String message) {
        Uri uri = Uri.parse("smsto:" + contactNo);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body",message);
        activity.startActivity(it);
    }
}
