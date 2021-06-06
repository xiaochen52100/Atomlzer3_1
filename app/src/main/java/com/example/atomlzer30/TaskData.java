package com.example.atomlzer30;

public class TaskData {
    private double progess;
    private int lastTime;
    private int settingTime;

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public void setProgess(double progess) {
        this.progess = progess;
    }

    public void setSettingTime(int settingTime) {
        this.settingTime = settingTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public double getProgess() {
        return progess;
    }

    public int getSettingTime() {
        return settingTime;
    }
}
