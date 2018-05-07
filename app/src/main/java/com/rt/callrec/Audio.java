package com.rt.callrec;


public class Audio {
    public String fileName;
   public String mUri;
   public String userID;


    public Audio() {
    }

    public Audio(String fileName, String mUri) {
        this.fileName = fileName;
        this.mUri = mUri;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }
}
