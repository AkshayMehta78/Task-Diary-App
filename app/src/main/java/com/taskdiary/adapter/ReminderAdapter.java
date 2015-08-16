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
import com.taskdiary.model.Reminder;
import com.taskdiary.model.Task;
import com.taskdiary.utils.Constant;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by akshaymehta on 07/08/15.
 */
public class ReminderAdapter extends ArrayAdapter<Reminder> {

    private final Activity activity;
    private SparseBooleanArray mSelectedItemsIds;
    private ArrayList<Reminder> result;
    private ViewHolder holder;
    private int operation;

    private static class ViewHolder {
        TextView tvDate, tvTime, tvDelete;
    }

    public ReminderAdapter(Activity activity, ArrayList<Reminder> result, int operation) {
        super(activity, R.layout.new_reminder_layout_row, result);
        this.activity = activity;
        mSelectedItemsIds = new SparseBooleanArray();
        this.result = result;
        this.operation = operation;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Reminder item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.new_reminder_layout_row, parent, false);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvDelete = (TextView) convertView.findViewById(R.id.tvDelete);

//            if(operation==Constant.VIEW)
//                holder.tvDelete.setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            Date reminderDate =Constant.SDFDATEFORMAT.parse(item.getDate());
            String displayDate = Constant.SDFDATEFORMAT_DISPLAY.format(reminderDate);
            holder.tvDate.setText(displayDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvTime.setText(item.getTime());
        holder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
