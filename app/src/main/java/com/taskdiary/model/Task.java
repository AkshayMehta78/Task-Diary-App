package com.taskdiary.model;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 06/08/15.
 */
public class Task {
    String id;
    String title;
    String desc;
    String category;
    String priority;
    String type;
    String date;
    String completed;
    String deleted;

    ArrayList<Reminder> list;

    public ArrayList<Reminder> getList() {
        return list;
    }

    public void setList(ArrayList<Reminder> list) {
        this.list = list;
    }

    public String getContactIDs() {
        return contactIDs;
    }

    public void setContactIDs(String contactIDs) {
        this.contactIDs = contactIDs;
    }

    String contactIDs;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

}
