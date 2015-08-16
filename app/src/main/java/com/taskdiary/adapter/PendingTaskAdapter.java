package com.taskdiary.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.taskdiary.activity.R;
import com.taskdiary.activity.ViewTaskDetails;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;
import com.taskdiary.utils.Utils;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 07/08/15.
 */
public class PendingTaskAdapter extends ArrayAdapter<Task> {

    private final Activity activity;
    private SparseBooleanArray mSelectedItemsIds;
    ArrayList<Task> result;
    ViewHolder holder;
    boolean flag;

    private static class ViewHolder {
        TextView tvTitle,viewTextView,contactNameTextView;
        View viewPriority;
        ImageView tickView;
    }

    public PendingTaskAdapter(Activity activity, ArrayList<Task> result) {
        super(activity, R.layout.pending_task_layout_row, result);
        this.activity = activity;
        mSelectedItemsIds = new SparseBooleanArray();
        this.result = result;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Task item = getItem(position);
        Task previous_item;
        if(position==0)
            previous_item = getItem(0);
        else
            previous_item = getItem(position-1);


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pending_task_layout_row, parent, false);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tickView = (ImageView) convertView.findViewById(R.id.tickView);
            holder.viewTextView = (TextView) convertView.findViewById(R.id.viewTextView);
            holder.viewPriority = convertView.findViewById(R.id.viewPriority);
            holder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactNameTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(item.getTitle());

        if(item.getPriority().equalsIgnoreCase(Constant.HIGH))
            holder.viewPriority.setBackgroundColor(activity.getResources().getColor(R.color.error_color));
        else if(item.getPriority().equalsIgnoreCase(Constant.MEDIUM))
            holder.viewPriority.setBackgroundColor(activity.getResources().getColor(R.color.orange));
        else
            holder.viewPriority.setBackgroundColor(activity.getResources().getColor(R.color.app_color));

        if((item.getContactIDs()!=null && item.getContactIDs()!=previous_item.getContactIDs())|| (item.getContactIDs()!=null && position==0))
            holder.contactNameTextView.setText(Utils.retrieveContactName(item.getContactIDs(),activity));
        else
            holder.contactNameTextView.setVisibility(View.GONE);

        return convertView;
    }

    @Override
    public void remove(Task object) {
        result.remove(object);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}