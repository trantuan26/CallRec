package com.rt.callrec;

public class Audio {
    String Uri, fileName;
    boolean isPlaying = false;

    public Audio() {
    }

    public Audio(String uri, String fileName, boolean isPlaying) {
        Uri = uri;
        this.fileName = fileName;
        this.isPlaying = isPlaying;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing){
        this.isPlaying = playing;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
