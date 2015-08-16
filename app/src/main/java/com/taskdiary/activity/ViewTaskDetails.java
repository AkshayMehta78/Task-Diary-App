package com.taskdiary.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.taskdiary.adapter.ReminderAdapter;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Reminder;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by akshaymehta on 08/08/15.
 */
public class ViewTaskDetails extends AppCompatActivity implements View.OnClickListener,View.OnLongClickListener {

    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private EditText labelTextView, descriptionTextView, dateTextView;
    private TextView selectCategoryTextView, selectedCatgeoryTextView, selectPriorityTextView, selectedPriorityTextView, selectTypeTextView, selectedTypeTextView;
    private DatabaseHelper db;
    private Calendar myCalendar = Calendar.getInstance();

    private ImageView contactImage1, contactImage2, contactImage3;
    private TextView contactName1, contactName2, contactName3;
    private LinearLayout groupLayout;

    private Toolbar mToolbar;
    private ArrayList<String> contactIDs;
    private int taskID;
    private Task taskDetails;
    private int id = 0;

    //Reminders
    private ArrayList<Reminder> remindersList;
    private ReminderAdapter rAdapter;
    private ListView reminderslistView;
    private Uri uriContact;
    private String contactID;
    private TextView addReminderTextView;

    private ScrollView scrollableView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewtask);

        getWidgetReferences();
        setWidgetEvents();
        initialization();
        setupToolbar();

        taskID = Integer.parseInt(getIntent().getExtras().getString(DatabaseHelper.KEY_ID));
        taskDetails = db.getTaskDetails(taskID);

        fillTaskData();
        setReminders();
        scrollableView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Log.v("PARENT", "PARENT TOUCH");
                reminderslistView.requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        reminderslistView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Log.v("CHILD", "CHILD TOUCH");
                // Disallow the touch request for parent scroll on touch of
                // child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    private void setReminders() {
        remindersList = db.getAllTaskReminders(taskID);
        rAdapter = new ReminderAdapter(ViewTaskDetails.this, remindersList, Constant.VIEW);
        reminderslistView.setAdapter(rAdapter);
    }

    private void fillTaskData() {

//        labelTextView.setText(Utils.getSpannableString("Title " + taskDetails.getTitle(), 6));
//        descriptionTextView.setText(Utils.getSpannableString("Description " + taskDetails.getDesc(), 12));
//        dateTextView.setText(Utils.getSpannableString("Date " + taskDetails.getDate(), 5));

        labelTextView.setText(taskDetails.getTitle());
        descriptionTextView.setText(taskDetails.getDesc());
        dateTextView.setText(taskDetails.getDate());

        selectedPriorityTextView.setText(taskDetails.getPriority());
        selectedCatgeoryTextView.setText(taskDetails.getCategory());
        selectedTypeTextView.setText(taskDetails.getType());

        if (taskDetails.getType().equalsIgnoreCase("Group")) {
            Log.e("group",taskDetails.getContactIDs());
            groupLayout.setVisibility(View.VISIBLE);
            contactIDs = Utils.formatStringToArray(taskDetails.getContactIDs());
            if (contactIDs.size() > 0) {
                int j = 0;
                for (int i = 0; i < contactIDs.size(); i++) {
                    String name = Utils.retrieveContactName(contactIDs.get(i).trim(), this);
                    Bitmap imageData = Utils.retrieveContactPhoto(contactIDs.get(i).trim(), this);
                    id = ++j;
                    setContactDetails(name, imageData);
                }
            }
        } else {
            groupLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_menu, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        if (v == contactImage1) {
            id = 1;
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
        }
        if (v == contactImage2) {
            id = 2;
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
        }
        if (v == contactImage3) {
            id = 3;
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
        }

        if (v == selectPriorityTextView) {
            openPriorityDialog();
        }
        if (v == selectCategoryTextView) {
            openCategoryDialog();
        }
        if (v == selectTypeTextView) {
            openTaskType();
        }


        if(v == addReminderTextView)
        {
            //    addReminderDialog();
            if(remindersList.size()==0) {
                new DatePickerDialog(ViewTaskDetails.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }else
            {
                openMultipleOptionForDate();
            }
        }
    }

    private void openShareDialog(final String contactid) {
        new MaterialDialog.Builder(this)
                .title(R.string.share_with)
                .items(R.array.shareItems)
                .theme(Theme.LIGHT)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            String emailId = Utils.retrieveContactEmail(contactid, getApplicationContext());
                            if (emailId!=null)
                                Utils.showEmailDialog(ViewTaskDetails.this, emailId,taskDetails);
                        } else if (which == 1) {
                            String contactNo = Utils.getContactNumber(contactid, getApplicationContext());
                            if (!contactNo.isEmpty())
                                Utils.sendSMSDialog(ViewTaskDetails.this, contactNo, taskDetails);
                            else
                                Utils.showToast(ViewTaskDetails.this, getString(R.string.no_number));
                        } else {
                            String contactNo = Utils.getContactNumber(contactid, getApplicationContext());
                            if (!contactNo.isEmpty())
                                Utils.showCallDialog(ViewTaskDetails.this, contactNo);
                            else
                                Utils.showToast(ViewTaskDetails.this, getString(R.string.no_number));
                        }
                        return true;
                    }
                })
                .show();

    }

    private void getWidgetReferences() {

        scrollableView  = (ScrollView) findViewById(R.id.scrollableView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        labelTextView = (EditText) findViewById(R.id.labelTextView);
        descriptionTextView = (EditText) findViewById(R.id.descriptionTextView);
        dateTextView = (EditText) findViewById(R.id.dateTextView);

        selectCategoryTextView = (TextView) findViewById(R.id.selectCategoryTextView);
        selectedCatgeoryTextView = (TextView) findViewById(R.id.selectedCatgeoryTextView);
        selectPriorityTextView = (TextView) findViewById(R.id.selectPriorityTextView);
        selectedPriorityTextView = (TextView) findViewById(R.id.selectedPriorityTextView);
        selectTypeTextView = (TextView) findViewById(R.id.selectTypeTextView);
        selectedTypeTextView = (TextView) findViewById(R.id.selectedTypeTextView);

        contactImage1 = (ImageView) findViewById(R.id.contactImage1);
        contactImage2 = (ImageView) findViewById(R.id.contactImage2);
        contactImage3 = (ImageView) findViewById(R.id.contactImage3);

        contactName1 = (TextView) findViewById(R.id.contactName1);
        contactName2 = (TextView) findViewById(R.id.contactName2);
        contactName3 = (TextView) findViewById(R.id.contactName3);

        groupLayout = (LinearLayout) findViewById(R.id.groupLayout);

        reminderslistView = (ListView) findViewById(R.id.reminderslistView);
        addReminderTextView = (TextView) findViewById(R.id.addReminderTextView);

    }

    private void setWidgetEvents() {
        selectCategoryTextView.setOnClickListener(this);
        selectPriorityTextView.setOnClickListener(this);
        selectTypeTextView.setOnClickListener(this);

        contactImage1.setOnClickListener(this);
        contactImage2.setOnClickListener(this);
        contactImage3.setOnClickListener(this);

        contactImage1.setOnLongClickListener(this);
        contactImage2.setOnLongClickListener(this);
        contactImage3.setOnLongClickListener(this);

        addReminderTextView.setOnClickListener(this);

    }

    private void initialization() {
        db = new DatabaseHelper(this);
        contactIDs = new ArrayList<String>();
        remindersList =new ArrayList<Reminder>();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setContactDetails(String name, Bitmap imageData) {
        try {
            if (id == 1) {
                if (imageData != null)
                    contactImage1.setImageBitmap(imageData);
                else
                    contactImage1.setImageResource(R.drawable.default_user);
                contactName1.setText(name);
            } else if (id == 2) {
                if (imageData != null)
                    contactImage2.setImageBitmap(imageData);
                else
                    contactImage2.setImageResource(R.drawable.default_user);
                contactName2.setText(name);
            } else if (id == 3) {
                if (imageData != null)
                    contactImage3.setImageBitmap(imageData);
                else
                    contactImage3.setImageResource(R.drawable.default_user);
                contactName3.setText(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openTaskType() {
        new MaterialDialog.Builder(this)
                .title(R.string.selectType)
                .items(R.array.typeofassign)
                .theme(Theme.LIGHT)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (text != null) {
                            selectedTypeTextView.setText(text);
                            if (text.toString().equalsIgnoreCase("Group")) {
                                groupLayout.setVisibility(View.VISIBLE);
                            } else {
                                groupLayout.setVisibility(View.GONE);
                            }
                        } else
                            Utils.showToast(ViewTaskDetails.this, getString(R.string.selectoption));

                        return true;
                    }
                })
                .show();
    }

    private void openCategoryDialog() {
        String list[] = db.getAllCategories();
        new MaterialDialog.Builder(this)
                .title(R.string.selectCategory)
                .items(list)
                .theme(Theme.LIGHT)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (text != null)
                            selectedCatgeoryTextView.setText(text);
                        else
                            Utils.showToast(ViewTaskDetails.this, getString(R.string.selectoption));

                        return true;
                    }
                })
                .neutralText(R.string.add)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        Utils.addNewCategory(ViewTaskDetails.this);
                    }
                })
                .show();
    }

    private void openPriorityDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.selectpriority)
                .items(R.array.priority)
                .theme(Theme.LIGHT)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (text != null)
                            selectedPriorityTextView.setText(text);
                        else
                            Utils.showToast(ViewTaskDetails.this, getString(R.string.selectoption));

                        return true;
                    }
                })
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.cancel:
                this.onBackPressed();
                return true;
            case R.id.save:
                if (validateTaskDetails()) {
                    addTaskToDatabase();
                    Utils.showToast(ViewTaskDetails.this, getString(R.string.task_updated));
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }


    private boolean validateTaskDetails() {
        boolean flag = true;
        if (labelTextView.getText().toString().isEmpty()) {
            Utils.showToast(this, getString(R.string.empty_title));
            flag = false;
            return flag;
        }
        if (selectedCatgeoryTextView.getText().toString().isEmpty()) {
            Utils.showToast(this, getString(R.string.empty_category));
            flag = false;
            return flag;
        }
        if (selectedPriorityTextView.getText().toString().isEmpty()) {
            Utils.showToast(this, getString(R.string.empty_priority));
            flag = false;
            return flag;
        }
        if (selectedTypeTextView.getText().toString().isEmpty()) {
            Utils.showToast(this, getString(R.string.empty_type));
            return flag;
        }
        if(selectedTypeTextView.getText().toString().equalsIgnoreCase("Group"))
        {
            if(contactIDs.size()==0)
            {
                Utils.showToast(this, getString(R.string.select_contact));
                flag = false;
                return flag;
            }
        }
        return flag;
    }

    private void addTaskToDatabase() {
        Task item = new Task();
        item.setTitle(labelTextView.getText().toString().trim());
        item.setDesc(descriptionTextView.getText().toString().trim());
        item.setCategory(selectedCatgeoryTextView.getText().toString());
        item.setPriority(selectedPriorityTextView.getText().toString());
        item.setType(selectedTypeTextView.getText().toString());
        item.setDate(Utils.getCurrentDate());
        if(selectedTypeTextView.getText().toString().equalsIgnoreCase("Group"))
        {
            item.setContactIDs(contactIDs.toString());
        }
        item.setList(remindersList);
        db.updateTaskDetails(item, taskID);

    }


    @Override
    public boolean onLongClick(View v) {
        if (v == contactImage1) {
            if (contactIDs.size()>=1)
                openShareDialog(contactIDs.get(0).trim());
        }
        if (v == contactImage2) {
            if (contactIDs.size()>=2)
                openShareDialog(contactIDs.get(1).trim());
        }
        if (v == contactImage3) {
            if (contactIDs.size()>=3)
                openShareDialog(contactIDs.get(2).trim());
        }

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d("Response", "Response: " + data.toString());
            uriContact = data.getData();
            contactID = Utils.getContactID(uriContact, this);
            String name = Utils.retrieveContactName(contactID, this);
            Bitmap imageData = Utils.retrieveContactPhoto(contactID, this);
            setContactDetails(name, imageData);
            if(contactIDs.size()<3)
                contactIDs.add(id-1,contactID);
            else
                contactIDs.set(id-1,contactID);
        }
    }


    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            if(view.isShown()) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // setDate();
                setSelectedDate();
            }
        }
    };

    private void setSelectedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATEFORMAT, Locale.US);
        String selectedDate = sdf.format(myCalendar.getTime());
        if(Utils.isDateValid(selectedDate)) {
            Reminder item = new Reminder();
            item.setDate(selectedDate);
            item.setTime("");
            remindersList.add(item);
            rAdapter.notifyDataSetChanged();
        }else
            Utils.showToast(this,getString(R.string.invalid_date));
    }

    private void openMultipleOptionForDate() {
        new MaterialDialog.Builder(this)
                .title(R.string.add_reminder)
                .items(R.array.date_options)
                .theme(Theme.LIGHT)
                .negativeText(R.string.cancel)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            openDateDialog(which);
                        } else if (which == 1) {
                            openDateDialog(which);
                        } else if (which == 2) {
                            new DatePickerDialog(ViewTaskDetails.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                        }
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void openDateDialog(final int value) {
        new MaterialDialog.Builder(this)
                .title(R.string.select_week)
                .items(R.array.week_options)
                .theme(Theme.LIGHT)
                .negativeText(R.string.cancel)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    public int days;

                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if(value==0)
                            days = Integer.parseInt(text.toString()) * 7;
                        else if(value==1)
                            days = Integer.parseInt(text.toString()) * 30;

                        String previousDate = remindersList.get(remindersList.size() - 1).getDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(Utils.getDateFromString(previousDate));
                        cal.add(Calendar.DATE,days);
                        myCalendar = cal;
                        setSelectedDate();
                    }
                }).show();
    }

    private void openWeekDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.select_week)
                .items(R.array.week_options)
                .theme(Theme.LIGHT)
                .negativeText(R.string.cancel)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        int days = Integer.parseInt(text.toString()) * 7;
                        String previousDate = remindersList.get(remindersList.size() - 1).getDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(Utils.getDateFromString(previousDate));
                        cal.add(Calendar.DATE,days);
                        myCalendar = cal;
                        setSelectedDate();
                    }
                }).show();
    }


}
