package com.taskdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
public class ViewCompletedTaskActivity extends AppCompatActivity{
    private View mView;
    private ListView taskListView;
    private ArrayList<Task> result;
    private DatabaseHelper db;
    private TextView emptyTextView;
    private TaskAdapter adapter;
    private Toolbar mToolbar;
    private TextView tvSort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewall_task);

        getWidgetReferences();
        setWidgetEvents();
        initialization();
        setupToolbar();
    //    setUpTaskList();

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewCompletedTaskActivity.this, ViewTaskDetails.class);
                i.putExtra(DatabaseHelper.KEY_ID, result.get(position).getId());
                startActivity(i);
            }
        });


        // Capture ListView item click
        taskListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = taskListView.getCheckedItemCount();
                adapter.toggleSelection(position);

                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = adapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Task selecteditem = adapter.getItem(selected.keyAt(i));
                                // Remove selected items following the ids
                                adapter.remove(selecteditem);
                                db.deleteTask(selecteditem.getId());
                            }
                        }
                        // Close CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_completedtab, menu);
                menu.findItem(R.id.incomplete).setVisible(false);
                menu.findItem(R.id.delete).setVisible(true);

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });

    }
    private void getWidgetReferences() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        taskListView = (ListView) findViewById(R.id.taskListView);
        emptyTextView = (TextView) findViewById(R.id.emptyTextView);
        tvSort = (TextView) findViewById(R.id.tvSort);
    }

    private void setWidgetEvents() {
    }

    private void initialization() {
        db = new DatabaseHelper(this);
        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

    }
    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.completed_task);
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
