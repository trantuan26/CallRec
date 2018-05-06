package com.rt.callrec;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


/**
 * Created by QNIT on 12/18/2016.
 */

public class RecFile extends Explorer implements Serializable {
    protected String
            duration, callAction, phoneNumber,
            format, contactName, dateCreate;
    private boolean isFile;

    public RecFile(Context context, String path) {
        // Vì cần tốc độ cho custom layout nên mọi thuộc tính cần phải được tính toán, định trị và lưu trữ vào list
        // Trong khi list show, không có bất kì thuộc tính nào cần tính toán lại hay cần truy cập sdcard
        super(path);
        if (super.isFile()) {
            completeDataMember(context);
        } else if (super.isDirectory()) {
            dateCreate = (String) DateFormat.format(context.getString(R.string.medium_date_format), new Date(this.lastModified()));
        }
    }

    public void completeDataMember(Context context) {
        isFile = true;
        String[] data = analyzeFileName(this.getName());
        this.callAction = data[3];
        this.phoneNumber = data[4];
        this.format = data[5];
        dateCreate = (String) DateFormat.format(context.getString(R.string.date_format), new Date(this.lastModified()));
        MediaPlayer player = new MediaPlayer();
        Log.d("L_isFile", isFile + "");
        
        try {
            player.setDataSource(this.getPath());
            player.prepare();
            duration = convertTime(player.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            player.release();
        }
    }

    public boolean isFile() {
        return isFile;
    }

    public RecFile() {
        super("");
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setCallAction(String callAction) {
        this.callAction = callAction;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    public Date getDateTimeCreate() {
        return new Date(this.lastModified());
    }

    public String getCallAction() {
        return callAction;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFormat() {
        return format;
    }

    public String getContactName() {
        return contactName;
    }

    public long getSize() {
        return this.length() / 1024;
    }

    public String getDateCreate(String format) {
        return (String) DateFormat.format(format, getDateTimeCreate());
    }

    public String getTimeCreate(String format) {
        return (String) DateFormat.format(format, getDateTimeCreate());
    }

    public static String[] analyzeFileName(String fileName) {
        String[] comp = fileName.split("_"),
                reString = new String[6];
        reString[0] = comp[0]; //Call or Comu
        reString[1] = comp[1]; //dateCreate
        reString[2] = comp[2]; //timeCreate
        reString[3] = comp[3]; //Call Action
        reString[4] = comp[4].substring(0, comp[4].indexOf(".")); //phoneNumber
        reString[5] = fileName.substring(fileName.lastIndexOf(".") + 1); //Format

        return reString;
    }

    public static String convertTime(long milliSecond) {
        String reString = "";

        int hour = (int) (milliSecond / (1000 * 60 * 60));
        int min = (int) ((milliSecond % (1000 * 60 * 60)) / (1000 * 60));
        int sec = (int) ((milliSecond % (1000 * 60 * 60)) % (1000 * 60) / (1000));
        if (hour > 0) reString = hour + ":";
        if (min < 10)
            reString += "0" + min;
        else
            reString += "" + min;
        if (sec < 10)
            reString += ":0" + sec;
        else
            reString += ":" + sec;
        return reString;
    }

    public static String getDateCreate(String fileName, @Nullable String post) {
        return fileName.split("_")[1] + "";
    }

    public static String getTimeCreate(String fileName, @Nullable String post) {
        return fileName.split("_")[2] + "";
    }

    public static String getCallAction(String fileName) {
        return fileName.split("_")[3] + "";
    }

    public static String getPhoneNumber(String fileName) {
        String phNumber = fileName.split("_")[4];
        return phNumber.substring(0, phNumber.lastIndexOf(".")) + "";
    }

    public static String getFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1) + "";
    }

    public static long getDateTimeCreate(String path) {
        return new File(path).lastModified();
    }

    public static long getSize(String path) {
        return new File(path).length() / 1024;
    }

    public String getTimeDuration(Context context, long lastModified) {
        String timeDuration = (String) DateFormat.format(context.getString(R.string.time_format), new Date(lastModified));
        long minDuration = (System.currentTimeMillis() - lastModified) / (1000 * 60);
        int day = (int) (minDuration / (60 * 24));
        if (day < 1) {
            int hours = (int) ((minDuration % (60 * 24)) / 60);
            if (hours < 1) {
                timeDuration = minDuration + context.getString(R.string.phut_truoc) + timeDuration;
            } else timeDuration = hours + context.getString(R.string.gio_truoc) + timeDuration;
        }
        return timeDuration;
    }
}
