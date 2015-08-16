package com.taskdiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.taskdiary.activity.R;
import com.taskdiary.activity.ViewTaskDetails;
import com.taskdiary.adapter.TaskAdapter;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 05/08/15.
 */
public class CompletedTaskFragment extends Fragment implements View.OnClickListener{
    private View mView;
    private ListView taskListView;
    private ArrayList<Task> result;
    private DatabaseHelper db;
    private TextView emptyTextView;
    private TaskAdapter adapter;
    private TextView tvSort;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.layout_tasklist, container, false);
        getWidgetReference();
        setWidgetEvents();
        initialization();

        setUpTaskList();

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
                    case R.id.incomplete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selectedIds = adapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selectedIds.size() - 1); i >= 0; i--) {
                            if (selectedIds.valueAt(i)) {
                                Task selecteditem = adapter.getItem(selectedIds.keyAt(i));
                                // Remove selected items following the ids
                                adapter.remove(selecteditem);
                                db.updateTask(selecteditem.getId(), Constant.INCOMPLETE, Constant.COMPLETE);
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

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ViewTaskDetails.class);
                i.putExtra(DatabaseHelper.KEY_ID, result.get(position).getId());
                getActivity().startActivity(i);
            }
        });

        return mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            setUpTaskList();
    }

    private void setUpTaskList() {
        result = new ArrayList<Task>();
        result = db.getAllTask(Constant.COMPLETE);
        if(result.size()>0)
        {
            emptyTextView.setVisibility(View.GONE);
            taskListView.setVisibility(View.VISIBLE);
            adapter = new TaskAdapter(getActivity(),result);
            taskListView.setAdapter(adapter);
        }
        else
        {
            emptyTextView.setVisibility(View.VISIBLE);
            taskListView.setVisibility(View.GONE);
        }
    }

    private void getWidgetReference() {
        taskListView = (ListView) mView.findViewById(R.id.taskListView);
        emptyTextView = (TextView) mView.findViewById(R.id.emptyTextView);
        tvSort = (TextView) mView.findViewById(R.id.tvSort);

    }

    private void setWidgetEvents() {
        tvSort.setOnClickListener(this);
    }

    private void initialization() {
        db=new DatabaseHelper(getActivity());
        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpTaskList();
    }

    @Override
    public void onClick(View v) {
        if(v==tvSort)
        {
            openCategoryDialog();
        }
    }

    private void openCategoryDialog() {
        String list[] = db.getAllCategories();
        new MaterialDialog.Builder(getActivity())
                .title(R.string.view_all)
                .items(list)
                .theme(Theme.LIGHT)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        setUpTaskListByCategory(text.toString());
                    }
                })
                .show();
    }


    private void setUpTaskListByCategory(String category) {
        result = new ArrayList<Task>();
        result = db.getAllTaskByCategory(Constant.COMPLETE,category);
        if(result.size()>0)
        {
            emptyTextView.setVisibility(View.GONE);
            taskListView.setVisibility(View.VISIBLE);
            adapter = new TaskAdapter(getActivity(),result);
            taskListView.setAdapter(adapter);
        }
        else
        {
            emptyTextView.setVisibility(View.VISIBLE);
            taskListView.setVisibility(View.GONE);
        }
    }
}
