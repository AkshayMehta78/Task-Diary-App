package com.taskdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.taskdiary.adapter.TaskAdapter;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Reminder;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 15/08/15.
 */
public class ViewAllTaskActivity extends AppCompatActivity{
    private View mView;
    private ListView taskListView;
    private ArrayList<Task> result;
    private DatabaseHelper db;
    private TextView emptyTextView;
    private TaskAdapter adapter;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewall_task);

        getWidgetReferences();
        setWidgetEvents();
        initialization();
        setupToolbar();
        setUpTaskList();

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewAllTaskActivity.this, ViewTaskDetails.class);
                i.putExtra(DatabaseHelper.KEY_ID, result.get(position).getId());
                startActivity(i);
            }
        });
    }
    private void getWidgetReferences() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        taskListView = (ListView) findViewById(R.id.taskListView);
        emptyTextView = (TextView) findViewById(R.id.emptyTextView);
    }

    private void setWidgetEvents() {

    }

    private void initialization() {
        db = new DatabaseHelper(this);
    }
    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpTaskList() {
        result = new ArrayList<Task>();
        result = db.getAllTask("");
        if(result.size()>0)
        {
            emptyTextView.setVisibility(View.GONE);
            taskListView.setVisibility(View.VISIBLE);
            adapter = new TaskAdapter(this,result);
            taskListView.setAdapter(adapter);
        }
        else
        {
            emptyTextView.setVisibility(View.VISIBLE);
            taskListView.setVisibility(View.GONE);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpTaskList();
    }



}
