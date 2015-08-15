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

import com.taskdiary.activity.R;
import com.taskdiary.activity.ViewTaskDetails;
import com.taskdiary.adapter.PendingTaskAdapter;
import com.taskdiary.adapter.TaskAdapter;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 05/08/15.
 */
public class PendingTaskFragment extends Fragment implements View.OnClickListener {
    private View mView;
    private ListView taskListView;
    private ArrayList<Task> result;
    private DatabaseHelper db;
    private TextView emptyTextView;
    private TaskAdapter adapter;
    private PendingTaskAdapter pAdapter;
    private TextView tvSort;

    private int flag = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.layout_tasklist, container, false);
        getWidgetReference();
        setWidgetEvents();
        initialization();

        setUpTaskList();
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ViewTaskDetails.class);
                i.putExtra(DatabaseHelper.KEY_ID, result.get(position).getId());
                getActivity().startActivity(i);
            }
        });


        // Capture ListView item click
        taskListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = taskListView.getCheckedItemCount();
                if(flag == 0)
                    adapter.toggleSelection(position);
                else
                    pAdapter.toggleSelection(position);
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");

            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.complete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected;
                        selected = adapter.getSelectedIds();

                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Task selecteditem = adapter.getItem(selected.keyAt(i));
                                adapter.remove(selecteditem);
                                db.updateTask(selecteditem.getId(), Constant.COMPLETE);
                            }
                        }
                        // Close CAB
                        mode.finish();
                        return true;
                    case R.id.share:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray Idsselected;
                        Idsselected = pAdapter.getSelectedIds();
                        String message="";
                        boolean flag = true;

                        int contactId = Integer.parseInt(pAdapter.getItem(Idsselected.keyAt(0)).getContactIDs());
                        // Captures all selected ids with a loop
                        for (int i = (Idsselected.size() - 1); i >= 0; i--) {
                            if (Idsselected.valueAt(i)) {
                                Task selecteditem = pAdapter.getItem(Idsselected.keyAt(i));
                                if(contactId == Integer.parseInt(selecteditem.getContactIDs()))
                                {
                                    message = message + "Title:\t"+selecteditem.getTitle()+"\nDescription:\t"+ selecteditem.getDesc()+"\n-------------------------------------------------\n";
                                }
                                else {
                                    Utils.showToast(getActivity(), "Please Select Task from same user");
                                    flag = false;
                                }
                            }
                        }
                        if(flag) {
                            OpenSharingIntent(message);
                            mode.finish();
                        }

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if(flag == 0)
                    mode.getMenuInflater().inflate(R.menu.menu_currenttab, menu);
                else
                    mode.getMenuInflater().inflate(R.menu.menu_pendingtab, menu);

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                if(flag==0)
                    adapter.removeSelection();
                else
                    pAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });



        return mView;
    }

    private void OpenSharingIntent(String message) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, "Pending task list");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "Share task"));
    }


    @Override
    public void onClick(View v) {
        if(v==tvSort)
        {
            taskListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            if(flag==0) {
                tvSort.setText("Sort by Task");
                setUpTaskByPerson();
            } else
            {
                tvSort.setText("Sort by Person");
                setUpTaskList();
            }
        }
    }

    private void setUpTaskByPerson() {
        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        flag = 1;
        if(adapter!=null)
            adapter.clear();

        result = new ArrayList<Task>();
        result = db.getAllTaskPersonWise(Constant.PENDING);
        if(result.size()>0)
        {
            emptyTextView.setVisibility(View.GONE);
            taskListView.setVisibility(View.VISIBLE);
            pAdapter = new PendingTaskAdapter(getActivity(),result);
            taskListView.setAdapter(pAdapter);
        }
        else
        {
            emptyTextView.setVisibility(View.VISIBLE);
            taskListView.setVisibility(View.GONE);
        }
    }


    private void setUpTaskList() {
        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        flag = 0;
        if(pAdapter!=null)
            pAdapter.clear();

        result = new ArrayList<Task>();
        result = db.getAllTask(Constant.PENDING);
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
        tvSort.setText("Sort by Person");
        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        //  setUpTaskList();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        try {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                setUpTaskList();
                tvSort.setText("Sort by Person");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
